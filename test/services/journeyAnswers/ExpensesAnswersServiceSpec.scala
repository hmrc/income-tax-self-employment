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

import cats.implicits._
import config.AppConfig
import data.IFSConnectorTestData.{api1786DeductionsSuccessResponse, api1786EmptySuccessResponse}
import data.TimeData
import data.api1802.AnnualAllowancesData
import gens.ExpensesTailoringAnswersGen.expensesTailoringIndividualCategoriesAnswersGen
import gens.PrepopJourneyAnswersGen.annualAdjustmentsTypeGen
import gens.genOne
import mocks.connectors.MockIFSConnector
import mocks.repositories.MockJourneyAnswersRepository
import models.common.JourneyName._
import models.common.{JourneyName, JourneyStatus}
import models.connector.Api1786ExpensesResponseParser.goodsToSellOrUseParser
import models.connector.api_1786.SuccessResponseSchema
import models.connector.api_1802.request._
import models.connector.api_1895.request._
import models.database.JourneyAnswers
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb, WorkplaceRunningCostsDb}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.tailoring.ExpensesTailoring.{NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.{AsOneTotalAnswers, ExpensesTailoringIndividualCategoriesAnswers, NoExpensesAnswers}
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import org.scalatest.OneInstancePerTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.{JsObject, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import services.journeyAnswers.expenses.ExpensesAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.MongoSupport
import utils.BaseSpec._

import java.time.Clock
import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesAnswersServiceSpec extends AnyWordSpec
  with Matchers
  with MongoSupport
  with TimeData
  with DefaultAwaitTimeout
  with MockIFSConnector
  with MockJourneyAnswersRepository
  with OneInstancePerTest {

  val underTest                     = new ExpensesAnswersService(mockIFSConnector, mockJourneyAnswersRepository)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val clock: Clock             = mock[Clock]

  val tailoringJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.ExpensesTailoring,
    JourneyStatus.Completed,
    JsObject.empty,
    testInstant,
    testInstant,
    testInstant
  )

  val goodsToSellOrUseJourneyAnswers: JourneyAnswers      = tailoringJourneyAnswers.copy(journey = JourneyName.GoodsToSellOrUse)
  val workplaceRunningCostsJourneyAnswers: JourneyAnswers = tailoringJourneyAnswers.copy(journey = JourneyName.WorkplaceRunningCosts)

  val workplaceRunningCostsDb: WorkplaceRunningCostsDb = WorkplaceRunningCostsDb(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    wfbpExpensesAreDisallowable = false,
    None,
    None,
    None,
    None,
    None
  )

  val emptyAmendRequest: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(
    currTaxYear,
    nino,
    businessId,
    AmendSEPeriodSummaryRequestBody(None, None)
  )

  val downstreamError: Either[SingleDownstreamError, Nothing] =
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serverError).asLeft

  def buildPeriodData(deductions: Option[Deductions]): AmendSEPeriodSummaryRequestData =
    AmendSEPeriodSummaryRequestData(currTaxYear, nino, businessId, AmendSEPeriodSummaryRequestBody(Some(Incomes(Some(100.00), None, None)), deductions))


  "save ExpensesTailoringNoExpensesAnswers" should {
    "store data successfully" in {
      val answers: ExpensesTailoringAnswers = NoExpensesAnswers

      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Json.toJson(answers))

      val result: Either[ServiceError, Unit] = await(underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value)

      result shouldBe ().asRight
    }
  }

  "save ExpensesTailoringIndividualCategoriesAnswers" should {
    "store data successfully" in {
      val answers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)

      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Json.toJson(answers))

      val result: Either[ServiceError, Unit] = await(underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value)

      result shouldBe ().asRight
    }
  }

  "get journey answers" in {
    IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786DeductionsSuccessResponse.asRight)

    val result: Either[ServiceError, GoodsToSellOrUseJourneyAnswers] = await(underTest.getAnswers(journeyCtxWithNino)(goodsToSellOrUseParser, hc).value)

    val expectedResult: GoodsToSellOrUseJourneyAnswers = GoodsToSellOrUseJourneyAnswers(100.0, Option(BigDecimal(100.0)))

    result shouldBe expectedResult.asRight
  }

  "getExpensesTailoringAnswers" should {
    "return None when there are no answers" in {
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(ExpensesTailoring))(None)

      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] = await(underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe None.asRight
    }

    "return NoExpensesAnswers" in {
      val answers: JourneyAnswers = tailoringJourneyAnswers.copy(data = Json.toJson(ExpensesCategoriesDb(NoExpenses)).as[JsObject])

      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(ExpensesTailoring))(Some(answers))

      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] = await(underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe NoExpensesAnswers.some.asRight
    }

    "return AsOneTotalAnswers" in {
      val api1786Res: SuccessResponseSchema = api1786DeductionsSuccessResponse
        .copy(financials = api1786DeductionsSuccessResponse.financials.copy(
          deductions = api1786DeductionsSuccessResponse.financials.deductions.map(_.copy(simplifiedExpenses = BigDecimal("10.5").some))
        ))
      val answers: JourneyAnswers = tailoringJourneyAnswers.copy(data = Json.toJson(ExpensesCategoriesDb(TotalAmount)).as[JsObject])

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786Res.asRight)
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(ExpensesTailoring))(Some(answers))

      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] = await(underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe AsOneTotalAnswers(BigDecimal("10.5")).some.asRight
    }

    "return ExpensesTailoringIndividualCategoriesAnswers" in {
      val categoriesAnswers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val answers: JourneyAnswers = tailoringJourneyAnswers.copy(data = Json.toJson(categoriesAnswers).as[JsObject])

      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(ExpensesTailoring))(Some(answers))

      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] = await(underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe categoriesAnswers.some.asRight
    }

  }

  "save answers" should {
    "saveTailoringAnswers" in {
      val answers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val amendRequest = AmendSEPeriodSummaryRequestData(currTaxYear, nino, businessId, AmendSEPeriodSummaryRequestBody(None, None))

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(amendRequest)(().asRight)
      JourneyAnswersRepositoryMock.upsertAnswers(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Json.toJson(answers))

      val result: Either[ServiceError, Unit] = await(underTest.saveTailoringAnswers(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }
  }

  "getGoodsToSellOrUseAnswers" should {
    "return None when there are no answers" in {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(GoodsToSellOrUse))(None)

      val result: Either[ServiceError, Option[GoodsToSellOrUseAnswers]] = await(underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe None.asRight
    }

    "return GoodsToSellOrUseAnswers when they exist" in {
      val answers: JourneyAnswers = goodsToSellOrUseJourneyAnswers.copy(data = Json.toJson(TaxiMinicabOrRoadHaulageDb(true)).as[JsObject])

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786DeductionsSuccessResponse.asRight)
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(GoodsToSellOrUse))(Some(answers))

      val result: Either[ServiceError, Option[GoodsToSellOrUseAnswers]] = await(underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe GoodsToSellOrUseAnswers(taxiMinicabOrRoadHaulage = true, BigDecimal("100.0"), Option(BigDecimal("100.0"))).some.asRight
    }

  }

  "getWorkplaceRunningCostsAnswers" should {
    "return None when there are no answers" in {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(WorkplaceRunningCosts))(None)

      val result: Either[ServiceError, Option[WorkplaceRunningCostsAnswers]] = await(underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe None.asRight
    }

    "return WorkplaceRunningCostsAnswers when they exist" in {
      val answers: JourneyAnswers = workplaceRunningCostsJourneyAnswers.copy(data = Json.toJson(workplaceRunningCostsDb).as[JsObject])

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786DeductionsSuccessResponse.asRight)
      JourneyAnswersRepositoryMock.get(journeyCtxWithNino.toJourneyContext(WorkplaceRunningCosts))(Some(answers))

      val result: Either[ServiceError, Option[WorkplaceRunningCostsAnswers]] = await(underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value)

      result shouldBe WorkplaceRunningCostsAnswers(workplaceRunningCostsDb, api1786DeductionsSuccessResponse).some.asRight
    }
  }

  "deleteSimplifiedExpensesAnswers" should {
    "delete Tailoring DB answers and SimplifiedAmount API answer" in {
      val deductions: Deductions = Deductions.empty.copy(
        costOfGoods = Some(SelfEmploymentDeductionsDetailPosNegType(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some)),
        premisesRunningCosts = Some(SelfEmploymentDeductionsDetailPosNegType(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some))
      )
      val amendRequestBody: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(None, Some(deductions))
      val existingPeriodData: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(currTaxYear, nino, businessId, amendRequestBody)

      IFSConnectorMock.amendSEPeriodSummary(existingPeriodData)(().asRight)
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786DeductionsSuccessResponse.asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(ExpensesTailoring))

      val result: Either[ServiceError, Unit] = await(underTest.deleteSimplifiedExpensesAnswers(journeyCtxWithNino).value)

      result shouldBe ().asRight
    }
  }

  "clearExpensesAndCapitalAllowancesData" must {

    val notFoundError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)

    "delete any expenses and capital allowances from DB and APIs" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(notFoundError.asLeft)
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(notFoundError.asLeft)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Some("expenses-"))
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(CapitalAllowancesTailoring), Some("capital-allowances-"))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value)

      result shouldBe ().asRight
    }

    "return an error from downstream" when {
      "connector fails to update the PeriodicSummaries API" in new Test2 {
        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
        IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(downstreamError)
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(notFoundError.asLeft)

        val result: Either[ServiceError, Unit] = await(underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value)

        result shouldBe downstreamError
      }

      "connector fails to update the AnnualSummaries API" in new Test2 {
        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
        IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(downstreamError)

        val result: Either[ServiceError, Unit] = await(underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value)

        result shouldBe downstreamError
      }

      "connector fails to update the repository database" in {
        val error = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serverError)

        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(notFoundError.asLeft)
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(notFoundError.asLeft)
        JourneyAnswersRepositoryMock.deleteOneOrMoreJourneysError(journeyCtxWithNino.toJourneyContext(ExpensesTailoring), Some("expenses-"))(error)

        val result: Either[ServiceError, Unit] = await(underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value)

        result shouldBe downstreamError
      }
    }
  }

  "clearExpensesData" must {
    "delete all staff costs expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(StaffCosts))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, StaffCosts).value)

      result shouldBe ().asRight
    }

    "delete all professional fees expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(ProfessionalFees))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, ProfessionalFees).value)

      result shouldBe ().asRight
    }

    "delete all OfficeSupplies expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(OfficeSupplies))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, OfficeSupplies).value)

      result shouldBe ().asRight
    }

    "delete all Construction expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(Construction))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, Construction).value)

      result shouldBe ().asRight
    }

    "delete all OtherExpenses expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(OtherExpenses))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, OtherExpenses).value)

      result shouldBe ().asRight
    }

    "delete all financial charges expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(FinancialCharges))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, FinancialCharges).value)

      result shouldBe ().asRight
    }

    "delete all IrrecoverableDebts expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(IrrecoverableDebts))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, IrrecoverableDebts).value)

      result shouldBe ().asRight
    }

    "delete all AdvertisingOrMarketing expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(AdvertisingOrMarketing))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, AdvertisingOrMarketing).value)

      result shouldBe ().asRight
    }

    "delete all GoodsToSellOrUse expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(GoodsToSellOrUse))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, GoodsToSellOrUse).value)

      result shouldBe ().asRight
    }

    "delete all WorkplaceRunningCosts expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(WorkplaceRunningCosts))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, WorkplaceRunningCosts).value)

      result shouldBe ().asRight
    }

    "delete all RepairsAndMaintenanceCosts expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(RepairsAndMaintenanceCosts))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, RepairsAndMaintenanceCosts).value)

      result shouldBe ().asRight
    }

    "delete all interest on banks and other expenses API answer" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneys(journeyCtxWithNino.toJourneyContext(Interest))

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, Interest).value)

      result shouldBe ().asRight
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(downstreamError)

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, StaffCosts).value)

      result shouldBe downstreamError
    }

    "connector fails to update the repository database" in {
      val error: SingleDownstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serverError)

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)
      IFSConnectorMock.amendSEPeriodSummary(emptyAmendRequest)(().asRight)
      JourneyAnswersRepositoryMock.deleteOneOrMoreJourneysError(journeyCtxWithNino.toJourneyContext(StaffCosts))(error)

      val result: Either[ServiceError, Unit] = await(underTest.clearExpensesData(journeyCtxWithNino, StaffCosts).value)

      result shouldBe downstreamError
    }

    "clearSpecificExpensesData" in {
      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, OfficeSupplies).adminCosts shouldBe None
      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, GoodsToSellOrUse).costOfGoods shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, RepairsAndMaintenanceCosts)
        .maintenanceCosts shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, WorkplaceRunningCosts)
        .premisesRunningCosts shouldBe None

      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, StaffCosts).staffCosts shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, AdvertisingOrMarketing)
        .advertisingCosts shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, Construction)
        .constructionIndustryScheme shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, ProfessionalFees)
        .professionalFees shouldBe None

      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, IrrecoverableDebts).badDebt shouldBe None
      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, OtherExpenses).other shouldBe None
      underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, Interest).interest shouldBe None

      underTest
        .clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, FinancialCharges)
        .financialCharges shouldBe None
    }
  }

  trait Test2 extends TimeData {
    (() => clock.instant).expects().returning(testInstant).anyNumberOfTimes()

    val testDeductions: Deductions = Deductions.empty.copy(
      costOfGoods = Some(SelfEmploymentDeductionsDetailPosNegType(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some)),
      premisesRunningCosts = Some(SelfEmploymentDeductionsDetailPosNegType(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some))
    )

    def preparePeriodData(): Unit = {
      def existingPeriodData = buildPeriodData(Option(testDeductions))
      IFSConnectorMock.amendSEPeriodSummary(existingPeriodData)(().asRight)
    }

    def prepareAnnualSummariesData(): Unit = {
      val adjustments   = genOne(annualAdjustmentsTypeGen).toApi1802AnnualAdjustments
      val allowances    = AnnualAllowancesData.example
      val nonFinancials = AnnualNonFinancials(exemptFromPayingClass4Nics = true, Option("002"))
      val existingAnnualSummariesData = CreateAmendSEAnnualSubmissionRequestData(
        taxYear,
        nino,
        businessId,
        CreateAmendSEAnnualSubmissionRequestBody(adjustments.some, allowances.some, nonFinancials.some))

      IFSConnectorMock.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, Some(existingAnnualSummariesData.body))()
    }

}

object ExpensesAnswersServiceSpec {
  val mockAppConfig: AppConfig = mock[AppConfig]

  val tailoringJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.ExpensesTailoring,
    JourneyStatus.Completed,
    JsObject.empty,
    testInstant,
    testInstant,
    testInstant
  )
  val goodsToSellOrUseJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.GoodsToSellOrUse,
    JourneyStatus.Completed,
    JsObject.empty,
    testInstant,
    testInstant,
    testInstant
  )
  val workplaceRunningCostsJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.WorkplaceRunningCosts,
    JourneyStatus.Completed,
    JsObject.empty,
    testInstant,
    testInstant,
    testInstant
  )
  val workplaceRunningCostsDb: WorkplaceRunningCostsDb = WorkplaceRunningCostsDb(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    wfbpExpensesAreDisallowable = false,
    None,
    None,
    None,
    None,
    None
  )

  val downstreamError: Either[SingleDownstreamError, Nothing] =
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serverError).asLeft

  def buildPeriodData(deductions: Option[Deductions]): AmendSEPeriodSummaryRequestData =
    AmendSEPeriodSummaryRequestData(
      taxYear,
      nino,
      businessId,
      AmendSEPeriodSummaryRequestBody(Option(Incomes(Option(BigDecimal(100.00)), None, None)), deductions))
}}
