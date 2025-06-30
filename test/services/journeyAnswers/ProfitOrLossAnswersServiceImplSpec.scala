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

import cats.implicits.catsSyntaxEitherId
import data.IFSConnectorTestData._
import mocks.connectors.{MockBroughtForwardLossConnector, MockIFSBusinessDetailsConnector, MockIFSConnector}
import mocks.repositories.MockJourneyAnswersRepository
import mocks.services.MockReliefClaimsService
import models.common.JourneyName.ProfitOrLoss
import models.common.TaxYear
import models.connector.ReliefClaimType
import models.connector.api_1500.{CreateBroughtForwardLossRequestBody, CreateBroughtForwardLossRequestData, LossType}
import models.connector.api_1501.{UpdateBroughtForwardLossRequestBody, UpdateBroughtForwardLossRequestData}
import models.connector.api_1505.ClaimId
import models.connector.api_1802.request._
import models.connector.common.ReliefClaim
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.CarryItForward
import models.frontend.adjustments._
import org.mockito.MockitoSugar.reset
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, EitherValues}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import utils.BaseSpec.{businessId, currTaxYear, hc, journeyCtxWithNino, nino}
import utils.EitherTTestOps.convertScalaFuture

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class ProfitOrLossAnswersServiceImplSpec
    extends AnyWordSpecLike
    with TableDrivenPropertyChecks
    with Matchers
    with BeforeAndAfterEach
    with EitherValues
    with MockJourneyAnswersRepository
    with MockIFSConnector
    with MockIFSBusinessDetailsConnector
    with MockBroughtForwardLossConnector {

  val lossId                     = "lossId123"

  def service: ProfitOrLossAnswersServiceImpl =
    new ProfitOrLossAnswersServiceImpl(
      ifsConnector = mockIFSConnector,
      ifsBusinessDetailsConnector = mockIFSBusinessDetailsConnector,
      broughtForwardLossConnector = mockBroughtForwardLossConnector,
      reliefClaimsService = MockReliefClaimsService.mockInstance,
      repository = mockJourneyAnswersRepository
    )

  val downstreamError: SingleDownstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serviceUnavailable)
  val notFoundError: SingleDownstreamError   = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)

  val testDate: LocalDateTime = LocalDateTime.of(2025, 1, 5, 0, 0)
  val testClaimId1: ClaimId   = ClaimId("claimId1")
  val testClaimId2: ClaimId   = ClaimId("claimId2")

  override def afterEach(): Unit = {
    super.afterEach()
    reset(MockReliefClaimsService.mockInstance)
  }

  def testReliefClaim(claimId: ClaimId, claimType: ReliefClaimType): ReliefClaim =
    ReliefClaim(businessId.value, None, claimType, "2025", claimId.claimId, None, testDate)

  def expectedAnnualSummariesData(adjustments: Option[AnnualAdjustments],
                                  allowances: Option[AnnualAllowances]): CreateAmendSEAnnualSubmissionRequestData =
    CreateAmendSEAnnualSubmissionRequestData(
      journeyCtxWithNino.taxYear,
      journeyCtxWithNino.nino,
      journeyCtxWithNino.businessId,
      CreateAmendSEAnnualSubmissionRequestBody(
        annualAdjustments = adjustments,
        annualAllowances = allowances,
        annualNonFinancials = None
      )
    )

  def testProfitOrLossAnswers(doWithLoss: WhatDoYouWantToDoWithLoss*): ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
    goodsAndServicesForYourOwnUse = true,
    goodsAndServicesAmount = Option(BigDecimal(100)),
    claimLossRelief = Option(true),
    whatDoYouWantToDoWithLoss = Option(doWithLoss),
    carryLossForward = None,
    previousUnusedLosses = true,
    unusedLossAmount = Option(BigDecimal(200)),
    whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
  )

  "Saving ProfitOrLoss answers" must {
    val unusedLossAmount: BigDecimal = 400

    val yesBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = true,
      goodsAndServicesAmount = Option(BigDecimal(200)),
      claimLossRelief = Option(true),
      whatDoYouWantToDoWithLoss = None,
      carryLossForward = None,
      previousUnusedLosses = true,
      unusedLossAmount = Option(unusedLossAmount),
      whichYearIsLossReported = Option(WhichYearIsLossReported.Year2018to2019)
    )

    val noBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = false,
      goodsAndServicesAmount = None,
      claimLossRelief = Option(false),
      whatDoYouWantToDoWithLoss = None,
      carryLossForward = None,
      previousUnusedLosses = false,
      unusedLossAmount = None,
      whichYearIsLossReported = None
    )

    val emptyBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = false,
      goodsAndServicesAmount = None,
      claimLossRelief = None,
      whatDoYouWantToDoWithLoss = None,
      carryLossForward = None,
      previousUnusedLosses = false,
      unusedLossAmount = None,
      whichYearIsLossReported = None
    )

    val allowancesData: AnnualAllowances = AnnualAllowances(
      annualInvestmentAllowance = None,
      capitalAllowanceMainPool = None,
      capitalAllowanceSpecialRatePool = None,
      zeroEmissionGoodsVehicleAllowance = Option(BigDecimal(5000)),
      businessPremisesRenovationAllowance = None, enhanceCapitalAllowance = None,
      allowanceOnSales = None, capitalAllowanceSingleAssetPool = None,
      structuredBuildingAllowance = None,
      enhancedStructuredBuildingAllowance = None,
      zeroEmissionsCarAllowance = Option(BigDecimal(5000)),
      tradingIncomeAllowance = None
    )

    val createBfLossRequestBody = CreateBroughtForwardLossRequestBody(
      taxYearBroughtForwardFrom = "2018-19",
      typeOfLoss = LossType.SelfEmployment,
      businessId = businessId.value,
      lossAmount = 400
    )

    "successfully save data when answers are true" in {
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData = expectedAnnualSummariesData(
        adjustments = Option(yesBroughtForwardLossAnswers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))),
        allowances = Option(allowancesData)
      )

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500EmptyResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(yesBroughtForwardLossAnswers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value)

      assert(result == ().asRight)
    }

    "successfully save data when answers are true (with WhatDoYouWantToDoWithLoss answer)" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers.copy(whatDoYouWantToDoWithLoss = Option(Seq(CarryItForward)))
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500EmptyResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      MockReliefClaimsService.createReliefClaims(journeyCtxWithNino, CarryItForward)(List(testClaimId1))
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

    "successfully save data when answers are false" in {
      val answers: ProfitOrLossJourneyAnswers = noBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData = expectedAnnualSummariesData(None, Option(allowancesData))

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
      //assert(ifsConnector.upsertAnnualSummariesSubmissionData === Option(expectedAnnualSummariesAnswers))
    }

    "successfully create and save answers if no existing answers" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(None)), None)

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803EmptyResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500EmptyResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

    "return left when getAnnualSummaries returns left" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(downstreamError.asLeft)

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == downstreamError.asLeft)
    }

    "return left when createAmendSEAnnualSubmission returns left" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(None)), None)

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803EmptyResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))(downstreamError.asLeft)

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == downstreamError.asLeft)
    }

    "return left when upsertAnswers returns left" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(None)), None)

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803EmptyResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500EmptyResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswersFailure(
        ctx = journeyCtxWithNino.toJourneyContext(ProfitOrLoss),
        newData = Json.toJson(yesBroughtForwardLossAnswers.toDbAnswers)
      )(downstreamError)

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == downstreamError.asLeft)
    }

    "successfully update brought forward loss answers when provided by user and API" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))
      val updateBFLData = UpdateBroughtForwardLossRequestData(
        nino,
        lossId = "5678",
        body = UpdateBroughtForwardLossRequestBody(lossAmount = unusedLossAmount)
      )

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870SuccessResponse.asRight)
      IFSBusinessDetailsConnectorMock.updateBroughtForwardLoss(updateBFLData)(api1501SuccessResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

    "successfully create brought forward loss answers when provided by user and get returns NOT_FOUND from API" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500SuccessResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

    "successfully create brought forward loss answers when provided by user and list returns NOT_FOUND from API" in {
      val answers: ProfitOrLossJourneyAnswers = yesBroughtForwardLossAnswers
      val downstreamError: SingleDownstreamError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData =
        expectedAnnualSummariesData(Option(answers.toDownStreamAnnualAdjustments(Option(AnnualAdjustments.empty))), Option(allowancesData))

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(downstreamError.asLeft)
      IFSBusinessDetailsConnectorMock.createBroughtForwardLoss(
        data = CreateBroughtForwardLossRequestData(nino, currTaxYear, createBfLossRequestBody)
      )(api1500SuccessResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(answers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

    "successfully delete brought forward loss answers when none provided by user and some from API" in {
      val answers: ProfitOrLossJourneyAnswers = emptyBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers: CreateAmendSEAnnualSubmissionRequestData = expectedAnnualSummariesData(None, Some(allowancesData))

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(expectedAnnualSummariesAnswers.body))()
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870SuccessResponse.asRight)
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)()
      BroughtForwardLossConnectorMock.deleteBroughtForwardLosses(nino, TaxYear(2019), lossId = "5678")(().asRight)
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ProfitOrLoss), Json.toJson(emptyBroughtForwardLossAnswers.toDbAnswers))

      val result: Either[ServiceError, Unit] = await(service.saveProfitOrLoss(journeyCtxWithNino, answers).value)

      assert(result == ().asRight)
    }

  }

  "getProfitOrLoss" must {
    "must return ProfitOrLossJourneyAnswers when the API's returns data" in {
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(List(testReliefClaim(testClaimId1, ReliefClaimType.CF)))
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(api1870EmptyResponse.asRight)
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponseWithAAType.asRight)

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] = service.getProfitOrLoss(journeyCtxWithNino).value.futureValue

      val expectedResult: Option[ProfitOrLossJourneyAnswers] =
        Option(
          ProfitOrLossJourneyAnswers(
            goodsAndServicesForYourOwnUse = true,
            goodsAndServicesAmount = Option(BigDecimal(200)),
            claimLossRelief = Option(true),
            whatDoYouWantToDoWithLoss = Option(List(CarryItForward)),
            carryLossForward = Option(true),
            previousUnusedLosses = false,
            unusedLossAmount = None,
            whichYearIsLossReported = None
          ))

      assert(result.value === expectedResult)
    }

    "must return None when the API's returns no data" in {
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(Nil)
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(notFoundError.asLeft)
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(notFoundError.asLeft)

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] = service.getProfitOrLoss(journeyCtxWithNino).value.futureValue

      assert(result.value === None)
    }

    "must return error when the downstream api 'getAllReliefClaims' fails to get the data" in {
      MockReliefClaimsService.getAllReliefClaimsFailure(journeyCtxWithNino)(downstreamError)

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] = service.getProfitOrLoss(journeyCtxWithNino).value.futureValue

      assert(result === downstreamError.asLeft)
    }

    "must return error when the downstream api 'listBroughtForwardLossesResult' fails to get the data" in {
      MockReliefClaimsService.getAllReliefClaims(journeyCtxWithNino)(List(testReliefClaim(testClaimId1, ReliefClaimType.CF)))
      IFSBusinessDetailsConnectorMock.getListOfBroughtForwardLosses(nino, currTaxYear)(downstreamError.asLeft)

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] = service.getProfitOrLoss(journeyCtxWithNino).value.futureValue

      assert(result === downstreamError.asLeft)
    }

  }

}

