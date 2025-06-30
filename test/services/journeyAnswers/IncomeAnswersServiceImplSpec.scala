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

import builders.BusinessDataBuilder
import cats.implicits._
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import mocks.connectors.MockIFSConnector
import mocks.repositories.MockJourneyAnswersRepository
import mocks.services.MockBusinessService
import models.common.JourneyName.{ExpensesTailoring, Income}
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1802.request.{AnnualAllowances, CreateAmendSEAnnualSubmissionRequestBody}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType, IncomesType}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Incomes}
import models.database.JourneyAnswers
import models.database.income.IncomeStorageAnswers
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.error.ServiceError.InvalidJsonFormatError
import models.frontend.income.IncomeJourneyAnswers
import org.mockito.Mockito.reset
import org.mockito.matchers.MacroBasedMatchers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.AuditService
import data.IFSConnectorTestData._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class IncomeAnswersServiceImplSpec extends AnyWordSpecLike
  with Matchers
  with MacroBasedMatchers
  with MockJourneyAnswersRepository
  with MockIFSConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockAuditService = mock[AuditService]
  val service = new IncomeAnswersServiceImpl(
    mockJourneyAnswersRepository,
    mockIFSConnector,
    mockAuditService,
    MockBusinessService.mockInstance
  )

  val brokenJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.Income,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )

  val sampleIncomeJourneyAnswersData: IncomeJourneyAnswers = gens.genOne(incomeJourneyAnswersGen)

  val sampleIncomeJourneyAnswers: JourneyAnswers = brokenJourneyAnswers.copy(
    data = Json.toJson(sampleIncomeJourneyAnswersData).as[JsObject]
  )

  "getAnswers" should {
    "return empty answers if there is no answers submitted" in {
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(Income))(None)

      await(service.getAnswers(journeyCtxWithNino).value) shouldBe None.asRight
    }

    "return error if cannot read IncomeJourneyAnswers" in {
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(Income))(Some(brokenJourneyAnswers))

      val result: Either[ServiceError, Option[IncomeJourneyAnswers]] = await(service.getAnswers(journeyCtxWithNino).value)
      val error: ServiceError = result.left.value

      error shouldBe a[InvalidJsonFormatError]
    }

    "return IncomeJourneyAnswers" in {
      val incomeStorageAnswers: IncomeStorageAnswers                 = sampleIncomeJourneyAnswersData.toDbModel.get

      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(Income))(Some(sampleIncomeJourneyAnswers))
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803EmptyResponse.asRight)

      val result: Either[ServiceError, Option[IncomeJourneyAnswers]] = await(service.getAnswers(journeyCtxWithNino).value)

      result.value shouldBe Option(
        IncomeJourneyAnswers(
          incomeStorageAnswers.incomeNotCountedAsTurnover,
          None,
          BigDecimal("0"),
          incomeStorageAnswers.anyOtherIncome,
          None,
          incomeStorageAnswers.turnoverNotTaxable,
          None,
          incomeStorageAnswers.tradingAllowance,
          incomeStorageAnswers.howMuchTradingAllowance,
          None
        ))
    }
  }

  "saving income answers" when {
    val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get

    val createPeriodRequest = CreateSEPeriodSummaryRequestData(
      currTaxYear,
      businessId,
      nino,
      CreateSEPeriodSummaryRequestBody(
        from = startDate(journeyCtxWithNino.taxYear),
        to = endDate(journeyCtxWithNino.taxYear),
        financials = Some(
          FinancialsType(
            incomes = Some(IncomesType(
              turnover = answers.turnoverIncomeAmount.some,
              other = answers.nonTurnoverIncomeAmount
            )),
            deductions = None
          )
        )
      )
    )

    val createPeriodSummaryRequest = CreateAmendSEAnnualSubmissionRequestBody(
      Some(answers.toDownStreamAnnualAdjustments(None)),
      Some(answers.toDownStreamAnnualAllowances(None)),
      None
    )

    "no period summary or annual submission data exists" must {

      "successfully store data and create the period summary" in {
        val createAnnualSummaryRequest = CreateAmendSEAnnualSubmissionRequestBody(
          Some(answers.toDownStreamAnnualAdjustments(None)),
          Some(answers.toDownStreamAnnualAllowances(None)),
          None
        )

        MockBusinessService.getBusiness(businessId, mtditid, nino)(BusinessDataBuilder.aBusiness)
        IFSConnectorMock.listSEPeriodSummary(journeyCtxWithNino)(api1965EmptyResponse.asRight)
        IFSConnectorMock.createSEPeriodSummary(createPeriodRequest)(().asRight)
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound).asLeft)
        IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(createAnnualSummaryRequest))(().asRight)
        JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(Income), Json.toJson(answers.toDbModel.get))
        JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Some("expenses-"))

        val ctx: JourneyContextWithNino = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        await(service.saveAnswers(ctx, answers).value) shouldBe ().asRight
      }
    }

    "prior submission data exists" must {
      "successfully store data and amend the period summary" in {
        val createAnnualSummaryRequest = CreateAmendSEAnnualSubmissionRequestBody(
          Some(answers.toDownStreamAnnualAdjustments(None)),
          Some(answers.toDownStreamAnnualAllowances(Some(AnnualAllowances.empty.copy(
            zeroEmissionGoodsVehicleAllowance = Some(BigDecimal("5000")),
            zeroEmissionsCarAllowance = Some(BigDecimal("5000")))))),
          None
        )

        MockBusinessService.getBusiness(businessId, mtditid, nino)(BusinessDataBuilder.aBusiness)
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803SuccessResponse.asRight)
        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786DeductionsSuccessResponse.asRight)
        IFSConnectorMock.listSEPeriodSummary(journeyCtxWithNino)(api1965MatchedResponse.asRight)
        IFSConnectorMock.amendSEPeriodSummaryAny(().asRight)
        IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(createAnnualSummaryRequest))(().asRight)
        JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(Income), Json.toJson(answers.toDbModel.get))

        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        await(service.saveAnswers(ctx, answers).value) shouldBe ().asRight
      }
    }

  }

}

