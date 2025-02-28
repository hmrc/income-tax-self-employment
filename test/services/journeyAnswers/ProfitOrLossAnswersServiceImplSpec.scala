/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services.journeyAnswers

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.HipConnector
import mocks.services.MockReliefClaimsService
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.connector.ReliefClaimType
import models.connector.api_1501.UpdateBroughtForwardLossRequestBody
import models.connector.api_1505.ClaimId
import models.connector.api_1802.request._
import models.connector.common.ReliefClaim
import models.database.adjustments.ProfitOrLossDb
import models.domain.ApiResultT
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.{CarryItForward, DeductFromOtherTypes}
import models.frontend.adjustments._
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{reset, times, verify, when}
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.times
import org.mockito.MockitoSugar.{verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.connectors.StubIFSConnector._
import stubs.connectors.{StubIFSBusinessDetailsConnector, StubIFSConnector}
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.BaseSpec.{businessId, hc, journeyCtxWithNino}
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.{businessId, currTaxYear, hc, journeyCtxWithNino, testDateTime}
import utils.EitherTTestOps.convertScalaFuture

import java.time.LocalDate
import scala.concurrent.ExecutionContext
import java.lang.reflect.Method
import scala.concurrent.ExecutionContext.Implicits.global

class ProfitOrLossAnswersServiceImplSpec extends AnyWordSpecLike with TableDrivenPropertyChecks with Matchers with BeforeAndAfterEach {

  val downstreamError: SingleDownstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serviceUnavailable)
  val notFoundError: SingleDownstreamError   = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)

  val testDate: LocalDate   = LocalDate.of(2025, 1, 5)
  val testClaimId1: ClaimId = ClaimId("claimId1")
  val testClaimId2: ClaimId = ClaimId("claimId2")

  override def afterEach(): Unit = {
    super.afterEach()
    reset(MockReliefClaimsService.mockInstance)
  }

  def testReliefClaim(claimId: ClaimId, claimType: ReliefClaimType): ReliefClaim =
    ReliefClaim(businessId.value, None, claimType, "2025", claimId.value, None, testDate)

  def expectedAnnualSummariesData(adjustments: Option[AnnualAdjustments],
                                  allowances: Option[AnnualAllowances]): CreateAmendSEAnnualSubmissionRequestData =
    CreateAmendSEAnnualSubmissionRequestData(
      journeyCtxWithNino.taxYear,
      journeyCtxWithNino.nino,
      journeyCtxWithNino.businessId,
      CreateAmendSEAnnualSubmissionRequestBody(
        adjustments,
        allowances,
        None
      )
    )

  def testProfitOrLossAnswers(doWithLoss: WhatDoYouWantToDoWithLoss*): ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
    goodsAndServicesForYourOwnUse = true,
    goodsAndServicesAmount = Option(BigDecimal(100)),
    claimLossRelief = Option(true),
    whatDoYouWantToDoWithLoss = Option(doWithLoss),
    carryLossForward = Option(true),
    previousUnusedLosses = true,
    unusedLossAmount = Option(BigDecimal(200)),
    whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
  )

  "Saving ProfitOrLoss answers" must {
    val unusedLossAmount: BigDecimal = 400
    val yesBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        Option(200),
        Option(true),
        None,
        Option(true),
        previousUnusedLosses = true,
        Option(unusedLossAmount),
        Option(WhichYearIsLossReported.Year2018to2019)
      )

    val noBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse = false, None, Option(false), None, None, previousUnusedLosses = false, None, None)
    val emptyBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse = false, None, None, None, None, previousUnusedLosses = false, None, None)

    "successfully save data when answers are true" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val allowancesData: AnnualAllowances = AnnualAllowances(None, None, None, Option(5000), None, None, None, None, None, None, Option(5000), None)
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))
      val result: Either[ServiceError, Unit] = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Option(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "successfully save data when answers are true (with WhatDoYouWantToDoWithLoss answer)" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward)(List(testClaimId1))

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers.copy(whatDoYouWantToDoWithLoss = Option(Seq(CarryItForward)))
      val allowancesData: AnnualAllowances    = AnnualAllowances(None, None, None, Option(5000), None, None, None, None, None, None, Option(5000), None)
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))

      val result: Either[ServiceError, Unit] = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Option(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Option(Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true,
          claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "successfully save data when answers are false" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      val answers: ProfitOrLossJourneyAnswers = noBroughtForwardLossAnswers
      val allowancesData: AnnualAllowances = AnnualAllowances(None, None, None, Option(5000), None, None, None, None, None, None, Option(5000), None)
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData = expectedAnnualSummariesData(None, Option(allowancesData))

      val result: Either[ServiceError, Unit] = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Option(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = Option(false), previousUnusedLosses = false))))
    }

    "successfully create and save answers if no existing answers" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803EmptyResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(None)), None)
      val result: Either[ServiceError, Unit] = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Option(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "return left when getAnnualSummaries returns left" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = downstreamError.asLeft
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == downstreamError.asLeft)
    }

    "return left when createAmendSEAnnualSubmission returns left" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          createAmendSEAnnualSubmissionResult = downstreamError.asLeft
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == downstreamError.asLeft)
    }

    "return left when upsertAnswers returns left" in new StubbedService {
      override val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository(upsertDataField = downstreamError.asLeft)

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == downstreamError.asLeft)
    }

    "successfully update brought forward loss answers when provided by user and API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          getBroughtForwardLossResult = api1502SuccessResponse.asRight,
          updateBroughtForwardLossResult = api1501SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.updateBroughtForwardLossResult === api1501SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "successfully create brought forward loss answers when provided by user and get returns NOT_FOUND from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = downstreamError.asLeft,
          createBroughtForwardLossResult = api1500SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.createBroughtForwardLossResult === api1500SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "successfully create brought forward loss answers when provided by user and list returns NOT_FOUND from API" in new StubbedService {
      val downstreamError: SingleDownstreamError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = downstreamError.asLeft,
          createBroughtForwardLossResult = api1500SuccessResponse.asRight
        )

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.createBroughtForwardLossResult === api1500SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))))
    }

    "successfully delete brought forward loss answers when none provided by user and some from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = api1502SuccessResponse.asRight
        )
      when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(EitherT.rightT(Right(())))
     /* when(mockHipConnector.deleteBroughtForwardLoss(eqTo(nino), eqTo(taxYear), eqTo(lossId))(any(), any()))
        .thenReturn(EitherT.rightT(Right(())))*/

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()

      val answers: ProfitOrLossJourneyAnswers = emptyBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = None, previousUnusedLosses = false))))
    }

    "do nothing when none provided by user and get returns NOT_FOUND from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = notFoundError.asLeft
        )
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      val answers: ProfitOrLossJourneyAnswers = emptyBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == ().asRight)
      assert(
        repository.lastUpsertedAnswer === Option(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = None, previousUnusedLosses = false))))
    }

    "return error without updating AnnualSummaries, BFL or DB data when AnnualSummaries API returns a ServiceError" in new StubbedService {
      override val ifsConnector = new StubIFSConnector(getAnnualSummariesResult = downstreamError.asLeft)

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result === downstreamError.asLeft)
      assert(ifsConnector.upsertDisclosuresSubmissionData === None)
      assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      assert(repository.lastUpsertedAnswer === None)
    }

    "return error without updating BFL or DB data when BroughtForwardLoss API returns a ServiceError" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = downstreamError.asLeft)

      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val result: Either[ServiceError, Unit]  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == downstreamError.asLeft)
      assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      assert(repository.lastUpsertedAnswer === None)
    }
  }

  "storeReliefClaimAnswers" when {
    "the user is answering the WhatDoYouWantToDoWithLoss question for the first time" must {
      "call the ReliefClaimService create method once, with a single selection" in new StubbedService {
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(Nil)
        MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward)(List(testClaimId1))

        val result: Either[ServiceError, Unit] =
          service.storeReliefClaimAnswers(journeyCtxWithNino, testProfitOrLossAnswers(CarryItForward)).value.futureValue

        result shouldBe Right(())

        verify(MockReliefClaimsService.mockInstance, times(1)).createReliefClaims(journeyCtxWithNino, Seq(CarryItForward))
        verify(MockReliefClaimsService.mockInstance, times(0)).updateReliefClaims(any[JourneyContextWithNino], any[ List[ReliefClaim]], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier])
        verify(MockReliefClaimsService.mockInstance, times(0)).deleteReliefClaims(any[JourneyContextWithNino], any[ List[ReliefClaim]])(any[HeaderCarrier])
      }

      "call the ReliefClaimService create method once, with both selections" in new StubbedService {
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(Nil)
        MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward, DeductFromOtherTypes)(List(testClaimId1))

        val result: Either[ServiceError, Unit] = service
          .storeReliefClaimAnswers(
            ctx = journeyCtxWithNino,
            submittedAnswers = testProfitOrLossAnswers(CarryItForward, DeductFromOtherTypes)
          )
          .value
          .futureValue

        result shouldBe Right(())
        verify(MockReliefClaimsService.mockInstance, times(1)).createReliefClaims(journeyCtxWithNino, Seq(CarryItForward, DeductFromOtherTypes))
        verify(MockReliefClaimsService.mockInstance, times(0)).updateReliefClaims(any[JourneyContextWithNino], any[ List[ReliefClaim]], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier])
        verify(MockReliefClaimsService.mockInstance, times(0)).deleteReliefClaims(any[JourneyContextWithNino], any[ List[ReliefClaim]])(any[HeaderCarrier])
      }

      "Do nothing if the user selects no options" in new StubbedService {
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(Nil)
        MockReliefClaimsService.createReliefClaims(journeyCtxWithNino)(Nil)

        val result: Either[ServiceError, Unit] = service
          .storeReliefClaimAnswers(
            ctx = journeyCtxWithNino,
            submittedAnswers = testProfitOrLossAnswers()
          )
          .value
          .futureValue

        result shouldBe Right(())
        verify(MockReliefClaimsService.mockInstance, times(0)).createReliefClaims(any[JourneyContextWithNino], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier], any[ExecutionContext])
        verify(MockReliefClaimsService.mockInstance, times(0)).updateReliefClaims(any[JourneyContextWithNino], any[List[ReliefClaim]], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier])
        verify(MockReliefClaimsService.mockInstance, times(0)).deleteReliefClaims(any[JourneyContextWithNino], any[List[ReliefClaim]])(any[HeaderCarrier])
      }
    }

    "the user is updating their answer to the WhatDoYouWantToDoWithLoss question" must {
      "add a 2nd selection when the user previously selected 1" in new StubbedService {
        val oldAnswers: List[ReliefClaim] = List(testReliefClaim(testClaimId1, ReliefClaimType.CF))
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(oldAnswers)
        MockReliefClaimsService.updateReliefClaims(journeyCtxWithNino, oldAnswers, CarryItForward, DeductFromOtherTypes)(
          returnValue = UpdateReliefClaimsResponse(Seq(DeductFromOtherTypes), Nil)
        )

        val result: Either[ServiceError, Unit] = await(
          service
            .storeReliefClaimAnswers(
              ctx = journeyCtxWithNino,
              submittedAnswers = testProfitOrLossAnswers(CarryItForward, DeductFromOtherTypes)
            )
            .value)

        result shouldBe Right(())

        verify(MockReliefClaimsService.mockInstance, times(1)).updateReliefClaims(
          journeyCtxWithNino,
          oldAnswers,
          Seq(CarryItForward, DeductFromOtherTypes))
        verify(MockReliefClaimsService.mockInstance, times(0)).createReliefClaims(any[JourneyContextWithNino], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier], any[ExecutionContext])
        verify(MockReliefClaimsService.mockInstance, times(0)).deleteReliefClaims(any[JourneyContextWithNino], any[List[ReliefClaim]])(any[HeaderCarrier])
      }
      "Remove the 2nd selection when the user previously selected both" in new StubbedService {
        val oldAnswers: List[ReliefClaim] =
          List(testReliefClaim(testClaimId1, ReliefClaimType.CF), testReliefClaim(testClaimId2, ReliefClaimType.CSGI))
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(oldAnswers)
        MockReliefClaimsService.updateReliefClaims(journeyCtxWithNino, oldAnswers, CarryItForward)(
          returnValue = UpdateReliefClaimsResponse(Seq(DeductFromOtherTypes), Nil)
        )

        val result: Either[ServiceError, Unit] = await(
          service
            .storeReliefClaimAnswers(
              ctx = journeyCtxWithNino,
              submittedAnswers = testProfitOrLossAnswers(CarryItForward)
            )
            .value)

        result shouldBe Right(())

        verify(MockReliefClaimsService.mockInstance, times(1)).updateReliefClaims(journeyCtxWithNino, oldAnswers, Seq(CarryItForward))
        verify(MockReliefClaimsService.mockInstance, times(0)).createReliefClaims(any[JourneyContextWithNino], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier], any[ExecutionContext])
        verify(MockReliefClaimsService.mockInstance, times(0)).deleteReliefClaims(any[JourneyContextWithNino], any[List[ReliefClaim]])(any[HeaderCarrier])
      }
      "Remove both options if the user deselects both" in new StubbedService {
        val oldAnswers: List[ReliefClaim] =
          List(testReliefClaim(testClaimId1, ReliefClaimType.CF), testReliefClaim(testClaimId2, ReliefClaimType.CSGI))
        MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(oldAnswers)
        MockReliefClaimsService.deleteReliefClaims(journeyCtxWithNino, oldAnswers)

        val result: Either[ServiceError, Unit] = await(
          service
            .storeReliefClaimAnswers(
              ctx = journeyCtxWithNino,
              submittedAnswers = testProfitOrLossAnswers()
            )
            .value)

        result shouldBe Right(())

        verify(MockReliefClaimsService.mockInstance, times(0)).createReliefClaims(any[JourneyContextWithNino], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier], any[ExecutionContext])
        verify(MockReliefClaimsService.mockInstance, times(0)).updateReliefClaims(any[JourneyContextWithNino], any[List[ReliefClaim]], any[Seq[WhatDoYouWantToDoWithLoss]])(any[HeaderCarrier])
        verify(MockReliefClaimsService.mockInstance, times(1)).deleteReliefClaims(eqTo(journeyCtxWithNino), eqTo(oldAnswers))(any[HeaderCarrier])
      }
    }

    "create a new loss claim when there is no existing data and submission data is provided" in new StubbedService {
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(returnValue = Nil)
      MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward)(returnValue = List(api1505SuccessResponse))

      val result: Either[ServiceError, Unit] = service
        .storeReliefClaimAnswers(
          journeyCtxWithNino,
          testProfitOrLossAnswers(CarryItForward)
        )
        .value
        .futureValue

      result shouldBe Right(())
    }

    "do nothing when there is no existing data and no submission data" in new StubbedService {
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(returnValue = Nil)
      MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward)(returnValue = Nil)

      val result: Either[ServiceError, Unit] = service
        .storeReliefClaimAnswers(
          journeyCtxWithNino,
          testProfitOrLossAnswers(CarryItForward)
        )
        .value
        .futureValue

      result shouldBe Right(())
    }
  }

  "storeBroughtForwardLossAnswers" should {
    val unusedLossAmount: BigDecimal = 400
    val yesBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        Option(200),
        Option(true),
        None,
        Option(true),
        previousUnusedLosses = true,
        Option(unusedLossAmount),
        Option(WhichYearIsLossReported.Year2018to2019)
      )
    val noBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = true,
      Option(200),
      Option(false),
      None,
      None,
      previousUnusedLosses = false,
      None,
      None)

    "return an empty success response" when {
      "given a valid submissions to create a new BroughtForwardLoss data" in new StubbedService {
        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Option(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }

      "given a valid submissions to update existing BroughtForwardLoss data with a different amount" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Option(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }

      "given a valid submissions to update existing BroughtForwardLoss data with a different year and amount" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(EitherT.rightT(Right(())))

        val result: Either[ServiceError, Unit] = service
          .storeBroughtForwardLossAnswers(
            journeyCtxWithNino,
            yesBroughtForwardLossAnswers.copy(whichYearIsLossReported = Option(WhichYearIsLossReported.Year2019to2020)))
          .value
          .futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Option(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }

      "given a valid submissions to delete existing BroughtForwardLoss data when user submits 'No' answers" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
                  .thenReturn(EitherT.rightT(()))

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "given a valid submissions to delete existing BroughtForwardLoss data when user submits 'No' answers" when {
        "hip integration feature flag is enabled" in new StubbedService {

          when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(EitherT.rightT(()))

          override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
            StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

          val result: Either[ServiceError, Unit] =
            service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

          assert(result == ().asRight)
          assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
          verify(mockHipConnector, times(1)).deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "user submits 'No' answers and there is no existing BroughtForwardLoss data to delete" in new StubbedService {
        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
    }

    "return a ServiceError" when {
      "the BusinessDetailsConnector .listBroughtForwardLosses returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = downstreamError.asLeft)

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "the BusinessDetailsConnector .createBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(createBroughtForwardLossResult = downstreamError.asLeft)

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "the BusinessDetailsConnector .updateBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          updateBroughtForwardLossResult = downstreamError.asLeft)

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "the BusinessDetailsConnector .updateBroughtForwardLossYear returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(EitherT.leftT(downstreamError))

        val result: Either[ServiceError, Unit] = service
          .storeBroughtForwardLossAnswers(
            journeyCtxWithNino,
            yesBroughtForwardLossAnswers.copy(whichYearIsLossReported = Option(WhichYearIsLossReported.Year2019to2020)))
          .value
          .futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "the BusinessDetailsConnector .deleteBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(EitherT.leftT(downstreamError))


        val res: ApiResultT[Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers)

        val result: Either[ServiceError, Unit] = res.value.futureValue
        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }

      "the BusinessDetailsConnector .deleteBroughtForwardLoss returns an error from downstream" when {
        "hip integration feature switch is enabled" in new StubbedService {

          when(mockHipConnector.deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext]))
            .thenReturn(EitherT.leftT(downstreamError))

          override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
            StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

          val result: Either[ServiceError, Unit] =
            service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

          assert(result === downstreamError.asLeft)
          assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)

          verify(mockHipConnector, times(1)).deleteBroughtForwardLoss(any[Nino], any[TaxYear], any[String])(any[HeaderCarrier], any[ExecutionContext])
        }
      }

      "the BusinessDetailsConnector .storeBroughtForwardLossAnswers returns a notFoundError error from getLossByBusinessId" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = notFoundError.asLeft)

        val result: Either[ServiceError, Unit] =
          service.storeBroughtForwardLossAnswers(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result === Right(()))
      }
    }
  }
}

trait StubbedService {
  val lossId                     = "lossId123"
  val mockAppConfig: AppConfig   = mock[AppConfig]
  val mockHttpClient: HttpClient = mock[HttpClient]

  val ifsConnector: StubIFSConnector                               = new StubIFSConnector()
  val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val mockHipConnector: HipConnector                               = mock[HipConnector]
  val repository: StubJourneyAnswersRepository                     = StubJourneyAnswersRepository()

  def service: ProfitOrLossAnswersServiceImpl =
    new ProfitOrLossAnswersServiceImpl(
      ifsConnector = ifsConnector,
      ifsBusinessDetailsConnector = ifsBusinessDetailsConnector,
      hipConnector = mockHipConnector,
      MockReliefClaimsService.mockInstance,
      repository = repository
    )
}
