/*
 * Copyright 2023 HM Revenue & Customs
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
import models.common.JourneyContextWithNino
import models.connector.api_1500.LossType
import models.connector.api_1501.UpdateBroughtForwardLossRequestBody
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.connector.api_1802.request._
import models.connector.api_1870
import models.connector.api_1870.LossData
import models.database.adjustments.ProfitOrLossDb
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.Json
import stubs.connectors.StubIFSConnector._
import stubs.connectors.{StubIFSBusinessDetailsConnector, StubIFSConnector}
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.{businessId, currTaxYear, hc, journeyCtxWithNino}
import utils.EitherTTestOps.convertScalaFuture

import java.lang.reflect.Method
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProfitOrLossAnswersServiceImplSpec extends AnyWordSpecLike with TableDrivenPropertyChecks with Matchers {

  val downstreamError: SingleDownstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serviceUnavailable)
  val notFoundError: SingleDownstreamError   = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)

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

  "Saving ProfitOrLoss answers" must {
    val unusedLossAmount: BigDecimal = 400
    val yesBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        Some(200),
        Some(true),
        None,
        Some(true),
        previousUnusedLosses = true,
        Some(unusedLossAmount),
        Some(WhichYearIsLossReported.Year2018to2019)
      )
    val noBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse = false, None, Some(false), None, None, previousUnusedLosses = false, None, None)
    val emptyBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse = false, None, None, None, None, previousUnusedLosses = false, None, None)

    "successfully save data when answers are true" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )

      val answers        = yesBroughtForwardLossAnswers
      val allowancesData = AnnualAllowances(None, None, None, Some(5000), None, None, None, None, None, None, Some(5000), None)
      val expectedAnnualSummariesAnswers =
        expectedAnnualSummariesData(Some(answers.toDownStreamAnnualAdjustments(Some(AnnualAdjustments.empty))), Some(allowancesData))
      val result = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))))
    }
    "successfully save data when answers are false" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )
      val answers                        = noBroughtForwardLossAnswers
      val allowancesData                 = AnnualAllowances(None, None, None, Some(5000), None, None, None, None, None, None, Some(5000), None)
      val expectedAnnualSummariesAnswers = expectedAnnualSummariesData(None, Some(allowancesData))
      val result                         = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = Some(false), previousUnusedLosses = false))))
    }
    "successfully create and save answers if no existing answers" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803EmptyResponse.asRight
        )
      val answers                        = yesBroughtForwardLossAnswers
      val expectedAnnualSummariesAnswers = expectedAnnualSummariesData(Some(answers.toDownStreamAnnualAdjustments(None)), None)
      val result                         = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))))
    }
    "return left when getAnnualSummaries returns left" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = downstreamError.asLeft
        )
      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == downstreamError.asLeft)
    }
    "return left when createAmendSEAnnualSubmission returns left" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          createAmendSEAnnualSubmissionResult = downstreamError.asLeft
        )
      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == downstreamError.asLeft)
    }
    "return left when upsertAnswers returns left" in new StubbedService {
      override val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository(upsertDataField = downstreamError.asLeft)
      val answers                                           = yesBroughtForwardLossAnswers
      val result                                            = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == downstreamError.asLeft)
    }
    "successfully update brought forward loss answers when provided by user and API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          getBroughtForwardLossResult = api1502SuccessResponse.asRight,
          updateBroughtForwardLossResult = api1501SuccessResponse.asRight
        )
      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.updateBroughtForwardLossResult === api1501SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))))
    }
    "successfully create brought forward loss answers when provided by user and get returns NOT_FOUND from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = downstreamError.asLeft,
          createBroughtForwardLossResult = api1500SuccessResponse.asRight
        )
      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.createBroughtForwardLossResult === api1500SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))))
    }
    "successfully create brought forward loss answers when provided by user and list returns NOT_FOUND from API" in new StubbedService {
      val downstreamError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = downstreamError.asLeft,
          createBroughtForwardLossResult = api1500SuccessResponse.asRight
        )
      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.createBroughtForwardLossResult === api1500SuccessResponse.asRight)
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))))
    }
    "successfully delete brought forward loss answers when none provided by user and some from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = api1502SuccessResponse.asRight,
          deleteBroughtForwardLossResult = Right(())
        )
      val answers = emptyBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(ifsBusinessDetailsConnector.deleteBroughtForwardLossResult === Right(()))
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = None, previousUnusedLosses = false))))
    }
    "do nothing when none provided by user and get returns NOT_FOUND from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = notFoundError.asLeft
        )
      val answers = emptyBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue
      assert(result == ().asRight)
      assert(
        repository.lastUpsertedAnswer === Some(
          Json.toJson(ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, claimLossRelief = None, previousUnusedLosses = false))))
    }
    "return error without updating AnnualSummaries, BFL or DB data when AnnualSummaries API returns a ServiceError" in new StubbedService {
      override val ifsConnector = new StubIFSConnector(getAnnualSummariesResult = downstreamError.asLeft)

      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result === downstreamError.asLeft)
      assert(ifsConnector.upsertDisclosuresSubmissionData === None)
      assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      assert(repository.lastUpsertedAnswer === None)
    }
    "return error without updating BFL or DB data when BroughtForwardLoss API returns a ServiceError" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = downstreamError.asLeft)

      val answers = yesBroughtForwardLossAnswers
      val result  = service.saveProfitOrLoss(journeyCtxWithNino, answers).value.futureValue

      assert(result == downstreamError.asLeft)
      assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      assert(repository.lastUpsertedAnswer === None)
    }
  }

  "createUpdateOrDeleteBroughtForwardLoss" should {
    val unusedLossAmount: BigDecimal = 400
    val yesBroughtForwardLossAnswers =
      ProfitOrLossJourneyAnswers(
        true,
        Some(200),
        Some(true),
        None,
        Some(true),
        true,
        Some(unusedLossAmount),
        Some(WhichYearIsLossReported.Year2018to2019))
    val noBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers(true, Some(200), Some(false), None, None, false, None, None)

    "return an empty success response" when {
      "given a valid submissions to create a new BroughtForwardLoss data" in new StubbedService {
        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Some(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }
      "given a valid submissions to update existing BroughtForwardLoss data with a different amount" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Some(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }
      "given a valid submissions to update existing BroughtForwardLoss data with a different year and amount" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        val result = service
          .createUpdateOrDeleteBroughtForwardLoss(
            journeyCtxWithNino,
            yesBroughtForwardLossAnswers.copy(whichYearIsLossReported = Some(WhichYearIsLossReported.Year2019to2020)))
          .value
          .futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === Some(UpdateBroughtForwardLossRequestBody(unusedLossAmount)))
      }
      "given a valid submissions to delete existing BroughtForwardLoss data when user submits 'No' answers" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = api1870SuccessResponse.asRight)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
      "user submits 'No' answers and there is no existing BroughtForwardLoss data to delete" in new StubbedService {
        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
    }
    "return a ServiceError" when {
      "the BusinessDetailsConnector .listBroughtForwardLosses returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = downstreamError.asLeft)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
      "the BusinessDetailsConnector .createBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(createBroughtForwardLossResult = downstreamError.asLeft)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
      "the BusinessDetailsConnector .updateBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          updateBroughtForwardLossResult = downstreamError.asLeft)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, yesBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
      "the BusinessDetailsConnector .updateBroughtForwardLossYear returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          updateBroughtForwardLossYearResult = downstreamError.asLeft)

        val result = service
          .createUpdateOrDeleteBroughtForwardLoss(
            journeyCtxWithNino,
            yesBroughtForwardLossAnswers.copy(whichYearIsLossReported = Some(WhichYearIsLossReported.Year2019to2020)))
          .value
          .futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
      "the BusinessDetailsConnector .deleteBroughtForwardLoss returns an error from downstream" in new StubbedService {
        override val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
          listBroughtForwardLossesResult = api1870SuccessResponse.asRight,
          deleteBroughtForwardLossResult = downstreamError.asLeft)

        val result = service.createUpdateOrDeleteBroughtForwardLoss(journeyCtxWithNino, noBroughtForwardLossAnswers).value.futureValue

        assert(result === downstreamError.asLeft)
        assert(ifsBusinessDetailsConnector.updatedBroughtForwardLossData === None)
      }
    }
  }

  "handling loss claims" should {
    "create a new loss claim when there is no existing data and submission data is provided" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          createLossClaimResult = api1505SuccessResponse.asRight
        )

      val submissionData: CreateLossClaimRequestBody = CreateLossClaimRequestBody("SJPR05893938418", "CF", currTaxYear.toString)

      val method: Method = service.getClass.getDeclaredMethod(
        "handleLossClaim",
        classOf[JourneyContextWithNino],
        classOf[Option[Unit]],
        classOf[Option[CreateLossClaimRequestBody]],
        classOf[HeaderCarrier])
      method.setAccessible(true)

      val result: Either[ServiceError, Unit] = method
        .invoke(service, journeyCtxWithNino, None, Some(submissionData), hc)
        .asInstanceOf[EitherT[Future, ServiceError, Unit]]
        .value
        .futureValue

      assert(result == Right(()))

      val createLossClaimResult: Either[ServiceError, CreateLossClaimSuccessResponse] =
        ifsConnector.createLossClaim(journeyCtxWithNino, submissionData).value.futureValue
      assert(createLossClaimResult == Right(api1505SuccessResponse))
    }

    "do nothing when there is no existing data and no submission data" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          createLossClaimResult = api1505SuccessResponse.asRight
        )

      val method: Method = service.getClass.getDeclaredMethod(
        "handleLossClaim",
        classOf[JourneyContextWithNino],
        classOf[Option[Unit]],
        classOf[Option[CreateLossClaimRequestBody]],
        classOf[HeaderCarrier])
      method.setAccessible(true)

      val result: Either[ServiceError, Unit] = method
        .invoke(service, journeyCtxWithNino, None, None, hc)
        .asInstanceOf[EitherT[Future, ServiceError, Unit]]
        .value
        .futureValue

      assert(result == Right(()))
    }
  }

  val listWithNoMatchingIds: List[LossData] = List(
    LossData("11111", "wwwwwww", LossType.SelfEmployment, 400, "2022-23", LocalDateTime.now),
    LossData("22222", "xxxxxxx", LossType.SelfEmployment, 400, "2022-23", LocalDateTime.now),
    LossData("33333", "zzzzzzz", LossType.SelfEmployment, 500, "2021-22", LocalDateTime.now)
  )
  val singleLossData      = LossData("99999", businessId.value, LossType.SelfEmployment, 999, "2022-23", LocalDateTime.now)
  val listWithAMatchingId = listWithNoMatchingIds.appended(singleLossData)
  val getLossByBusinessIdTestCases = Table(
    ("testDescription", "connectorResponse", "expectedResult"),
    ("None when connector returns an empty list", api1870EmptyResponse.asRight, None.asRight),
    ("None when business ID does not match any loss data items", api_1870.SuccessResponseSchema(listWithNoMatchingIds).asRight, None.asRight),
    ("None when connector returns a NotFound error", notFoundError.asLeft, None.asRight),
    ("Some(lossData) business ID matches a loss data", api_1870.SuccessResponseSchema(listWithAMatchingId).asRight, Some(singleLossData).asRight),
    ("an error from downstream", downstreamError.asLeft, downstreamError.asLeft)
  )

  "getLossByBusinessId" should {
    forAll(getLossByBusinessIdTestCases) { (testDescription, connectorResponse, expectedResult) =>
      s"return $testDescription" in new StubbedService {
        override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listBroughtForwardLossesResult = connectorResponse)
        val result: Either[ServiceError, Option[LossData]] = service.getBroughtForwardLossByBusinessId(journeyCtxWithNino).value.futureValue

        assert(result == expectedResult)
      }
    }
  }
}
trait StubbedService {
  val ifsConnector: StubIFSConnector                               = new StubIFSConnector()
  val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val repository: StubJourneyAnswersRepository                     = StubJourneyAnswersRepository()

  def service: ProfitOrLossAnswersServiceImpl =
    new ProfitOrLossAnswersServiceImpl(ifsConnector, ifsBusinessDetailsConnector, repository)
}
