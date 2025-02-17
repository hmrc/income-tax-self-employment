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
import cats.implicits._
import config.AppConfig
import data.api1802.AnnualAllowancesData
import gens.ExpensesJourneyAnswersGen._
import gens.ExpensesTailoringAnswersGen.expensesTailoringIndividualCategoriesAnswersGen
import gens.PrepopJourneyAnswersGen.annualAdjustmentsTypeGen
import gens.genOne
import models.common.JourneyName.{
  AdvertisingOrMarketing,
  ExpensesTailoring,
  GoodsToSellOrUse,
  IrrecoverableDebts,
  OfficeSupplies,
  ProfessionalFees,
  RepairsAndMaintenanceCosts,
  StaffCosts,
  WorkplaceRunningCosts
}
import models.common.{JourneyName, JourneyStatus}
import models.connector.Api1786ExpensesResponseParser.goodsToSellOrUseParser
import models.connector.api_1802.request.{
  AnnualAllowances,
  AnnualNonFinancials,
  CreateAmendSEAnnualSubmissionRequestBody,
  CreateAmendSEAnnualSubmissionRequestData
}
import models.connector.api_1895.request._
import models.database.JourneyAnswers
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb, WorkplaceRunningCostsDb}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.{AsOneTotalAnswers, NoExpensesAnswers}
import models.frontend.expenses.workplaceRunningCosts.{WorkplaceRunningCostsAnswers, WorkplaceRunningCostsJourneyAnswers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import repositories.MongoJourneyAnswersRepository
import services.journeyAnswers.ExpensesAnswersServiceImplSpec._
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.test.MongoSupport
import utils.BaseSpec._
import utils.EitherTTestOps.EitherTExtensions
import utils.TestClock

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExpensesAnswersServiceImplSpec extends AnyWordSpec with Matchers with MongoSupport {

  "save answers" should {
    "saveTailoringAnswers" in new Test {
      val answers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result: Either[ServiceError, Unit] = underTest.saveTailoringAnswers(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveOfficeSuppliesAnswers" in new Test {
      val answers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]    = underTest.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveGoodsToSell" in new Test {
      val answers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = underTest.saveGoodsToSell(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveRepairsAndMaintenance" in new Test {
      val answers: RepairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                = underTest.saveRepairsAndMaintenance(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveWorkplaceRunningCosts" in new Test {
      val answers: WorkplaceRunningCostsJourneyAnswers = genOne(workplaceRunningCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]           = underTest.saveWorkplaceRunningCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveAdvertisingOrMarketing" in new Test {
      val answers: AdvertisingOrMarketingJourneyAnswers = genOne(advertisingOrMarketingJourneyAnswersGen)
      val result: Either[ServiceError, Unit]            = underTest.saveAdvertisingOrMarketing(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveEntertainmentCosts" in new Test {
      val answers: EntertainmentJourneyAnswers = genOne(entertainmentJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = underTest.saveEntertainmentCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveStaffCosts" in new Test {
      val answers: StaffCostsJourneyAnswers  = genOne(staffCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = underTest.saveStaffCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveConstructionIndustrySubcontractors" in new Test {
      val answers: ConstructionJourneyAnswers = genOne(constructionJourneyAnswersGen)
      val result: Either[ServiceError, Unit]  = underTest.saveConstructionIndustrySubcontractors(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveProfessionalFees" in new Test {
      val answers: ProfessionalFeesJourneyAnswers = genOne(professionalFeesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = underTest.saveProfessionalFees(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveFinancialCharges" in new Test {
      val answers: FinancialChargesJourneyAnswers = genOne(financialChargesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = underTest.saveFinancialCharges(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveBadDebts" in new Test {
      val answers: IrrecoverableDebtsJourneyAnswers = genOne(irrecoverableDebtsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]        = underTest.saveBadDebts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveDepreciationCosts" in new Test {
      val answers: DepreciationCostsJourneyAnswers = genOne(depreciationCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]       = underTest.saveDepreciationCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveOtherExpenses" in new Test {
      val answers: OtherExpensesJourneyAnswers = genOne(otherExpensesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = underTest.saveOtherExpenses(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveInterests" in new Test {
      val answers: InterestJourneyAnswers    = genOne(interestJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = underTest.saveInterests(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

  }

  "save ExpensesTailoringNoExpensesAnswers" should {
    "store data successfully" in new Test {
      val answers: ExpensesTailoringAnswers = NoExpensesAnswers
      val result: Either[ServiceError, Unit] =
        underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save ExpensesTailoringIndividualCategoriesAnswers" should {
    "store data successfully" in new Test {
      val answers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result: Either[ServiceError, Unit] =
        underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save expenses journey answers" should {
    "store data successfully" in new Test {

      val someExpensesAnswers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                  = underTest.saveGoodsToSell(journeyCtxWithNino, someExpensesAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "get journey answers" in new Test {
    override val connector: StubIFSConnector =
      StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))

    val result: Either[ServiceError, GoodsToSellOrUseJourneyAnswers] =
      underTest.getAnswers(journeyCtxWithNino)(goodsToSellOrUseParser, hc).value.futureValue

    val expectedResult: GoodsToSellOrUseJourneyAnswers = GoodsToSellOrUseJourneyAnswers(100.0, Some(100.0))

    result shouldBe expectedResult.asRight
  }

  "getExpensesTailoringAnswers" should {
    "return None when there are no answers" in new Test {

      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] =
        underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return NoExpensesAnswers" in new Test {

      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(ExpensesCategoriesDb(NoExpenses)).as[JsObject])
        .some)
      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] =
        underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe NoExpensesAnswers.some.asRight
    }

    "return AsOneTotalAnswers" in new Test {
      override val connector: StubIFSConnector = StubIFSConnector(
        getPeriodicSummaryDetailResult = Future.successful(
          api1786DeductionsSuccessResponse
            .copy(financials = api1786DeductionsSuccessResponse.financials
              .copy(deductions = api1786DeductionsSuccessResponse.financials.deductions.map(_.copy(simplifiedExpenses = BigDecimal("10.5").some))))
            .asRight)
      )
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(ExpensesCategoriesDb(TotalAmount)).as[JsObject])
        .some)
      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] =
        underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe AsOneTotalAnswers(BigDecimal("10.5")).some.asRight
    }

    "return ExpensesTailoringIndividualCategoriesAnswers" in new Test {
      val answers: ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers = genOne(expensesTailoringIndividualCategoriesAnswersGen)

      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(answers).as[JsObject])
        .some)
      val result: Either[ServiceError, Option[ExpensesTailoringAnswers]] =
        underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe answers.some.asRight
    }

  }

  "getGoodsToSellOrUseAnswers" should {
    "return None when there are no answers" in new Test {

      val result: Either[ServiceError, Option[GoodsToSellOrUseAnswers]] =
        underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return GoodsToSellOrUseAnswers when they exist" in new Test {
      override val connector: StubIFSConnector =
        StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = goodsToSellOrUseJourneyAnswers
        .copy(data = Json.toJson(TaxiMinicabOrRoadHaulageDb(true)).as[JsObject])
        .some)
      val result: Either[ServiceError, Option[GoodsToSellOrUseAnswers]] =
        underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe GoodsToSellOrUseAnswers(taxiMinicabOrRoadHaulage = true, BigDecimal("100.0"), Option(BigDecimal("100.0"))).some.asRight
    }
  }

  "getWorkplaceRunningCostsAnswers" should {
    "return None when there are no answers" in new Test {

      val result: Either[ServiceError, Option[WorkplaceRunningCostsAnswers]] =
        underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return WorkplaceRunningCostsAnswers when they exist" in new Test {
      override val connector: StubIFSConnector =
        StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = workplaceRunningCostsJourneyAnswers
        .copy(data = Json.toJson(workplaceRunningCostsDb).as[JsObject])
        .some)
      val result: Either[ServiceError, Option[WorkplaceRunningCostsAnswers]] =
        underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe WorkplaceRunningCostsAnswers(workplaceRunningCostsDb, api1786DeductionsSuccessResponse).some.asRight
    }
  }

  "deleteSimplifiedExpensesAnswers" should {
    "delete Tailoring DB answers and SimplifiedAmount API answer" in new Test {
      val existingPeriodData: AmendSEPeriodSummaryRequestData = buildPeriodData(Some(DeductionsTestData.sample))

      override val connector: StubIFSConnector = StubIFSConnector()
      connector.amendSEPeriodSummaryResultData = Some(existingPeriodData)
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers.some)
      repo.lastUpsertedAnswer = Some(Json.toJson(ExpensesCategoriesDb(IndividualCategories)))

      val result: Either[ServiceError, Unit] = underTest.deleteSimplifiedExpensesAnswers(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight
      val apiResult: Option[AmendSEPeriodSummaryRequestBody] = connector.amendSEPeriodSummaryResultData.flatMap(_.body.returnNoneIfEmpty)
      apiResult shouldBe None
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearExpensesAndCapitalAllowancesData" must {
    "delete any expenses and capital allowances from DB and APIs" in new Test2 {
      prepareData()
      val result: Either[ServiceError, Unit] = underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      periodicApiResult shouldBe None
      annualApiResult shouldBe None

      incomeResult should not be None
      expensesTailoringResult shouldBe None
      goodsToSellOrUseResult shouldBe None
      capitalAllowancesResult shouldBe None
      zeroEmissionCarsResult shouldBe None
    }

    "return an error from downstream" when {
      "connector fails to update the PeriodicSummaries API" in new Test2 {
        override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
        prepareData()
        val result: Either[ServiceError, Unit] = underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value.futureValue

        result shouldBe downstreamError

        periodicApiResult should not be None
        annualApiResult shouldBe None
      }
      "connector fails to update the AnnualSummaries API" in new Test2 {
        override lazy val connector: StubIFSConnector = new StubIFSConnector(getAnnualSummariesResult = downstreamError)
        prepareData()
        val result: Either[ServiceError, Unit] = underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value.futureValue

        result shouldBe downstreamError

        periodicApiResult shouldBe None
        annualApiResult should not be None
      }
      "connector fails to update the repository database" in new Test {
        override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

        val result: Either[ServiceError, Unit] = underTest.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino).value.futureValue

        result shouldBe downstreamError
        repo.lastUpsertedAnswer shouldBe None
      }
    }
  }

  "clearOfficeSuppliesExpensesData" must {
    "delete all office supplies expenses API answer" in new Test {

      val existingPeriodData: AmendSEPeriodSummaryRequestData = buildPeriodData(Option(DeductionsTestData.sample))

      override val connector: StubIFSConnector = StubIFSConnector()
      connector.amendSEPeriodSummaryResultData = Option(existingPeriodData)
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers.some)
      repo.lastUpsertedAnswer = Option(Json.toJson(ExpensesCategoriesDb(IndividualCategories)))

      val result: Either[ServiceError, Unit] = underTest.clearOfficeSuppliesExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight
      val apiResult: Option[AmendSEPeriodSummaryRequestBody] = connector.amendSEPeriodSummaryResultData.flatMap(_.body.returnNoneIfEmpty)
      apiResult shouldBe None
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearRepairsAndMaintenanceExpensesData" must {
    "delete all repairs and maintenance expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearRepairsAndMaintenanceExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      repairsAndMaintenanceCosts shouldBe None
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
      val result: Either[ServiceError, Unit]        = underTest.clearRepairsAndMaintenanceExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearRepairsAndMaintenanceExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearAdvertisingOrMarketingExpensesData" must {
    "delete all advertising or marketing expenses API answer" in new Test {

      val existingPeriodData: AmendSEPeriodSummaryRequestData = buildPeriodData(Option(DeductionsTestData.sample))

      override val connector: StubIFSConnector = StubIFSConnector()
      connector.amendSEPeriodSummaryResultData = Option(existingPeriodData)
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers.some)
      repo.lastUpsertedAnswer = Option(Json.toJson(ExpensesCategoriesDb(IndividualCategories)))

      val result: Either[ServiceError, Unit] = underTest.clearAdvertisingOrMarketingExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight
      val apiResult: Option[AmendSEPeriodSummaryRequestBody] = connector.amendSEPeriodSummaryResultData.flatMap(_.body.returnNoneIfEmpty)
      apiResult shouldBe None
      repo.lastUpsertedAnswer shouldBe None

    }
  }

  "clearGoodsToSellOrUseExpensesData" must {
    "delete all goods to sell/use expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearGoodsToSellOrUseExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      goodsToSellOrUseResult shouldBe None
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
      val result: Either[ServiceError, Unit]        = underTest.clearGoodsToSellOrUseExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError

      periodicApiResult shouldBe None
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearGoodsToSellOrUseExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearWorkplaceRunningCostsExpensesData" must {
    "delete workplace running costs expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearWorkplaceRunningCostsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      workplaceRunningCostsResult shouldBe None
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearWorkplaceRunningCostsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearStaffCostsExpensesData" must {
    "delete all staff costs expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearStaffCostsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      staffCosts shouldBe None
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
      val result: Either[ServiceError, Unit]        = underTest.clearStaffCostsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearStaffCostsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearProfessionalFeesExpensesData" must {
    "delete all Professional fees expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearProfessionalFeesExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      professionalFees shouldBe None
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
      val result: Either[ServiceError, Unit]        = underTest.clearProfessionalFeesExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearProfessionalFeesExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearIrrecoverableDebtsExpensesData" must {
    "delete all Irrecoverable debts expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearIrrecoverableDebtsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      irrecoverableDebts shouldBe None
    }

    "connector fails to update the PeriodSummary API" in new Test2 {
      override lazy val connector: StubIFSConnector = new StubIFSConnector(amendSEPeriodSummaryResult = downstreamError)
      val result: Either[ServiceError, Unit]        = underTest.clearIrrecoverableDebtsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearIrrecoverableDebtsExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  "clearSpecificExpensesData" in new Test {
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
    underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, ProfessionalFees).professionalFees shouldBe None
    underTest.clearSpecificExpensesData(models.connector.api_1894.request.DeductionsTestData.sample, IrrecoverableDebts).badDebt shouldBe None
  }

  "clearConstructionExpensesData" must {
    "delete construction industry subcontractor expenses API answer" in new Test2 {
      prepareData()

      val result: Either[ServiceError, Unit] = underTest.clearConstructionExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe ().asRight

      incomeResult should not be None
      constructionCostsResult shouldBe None
    }

    "connector fails to update the repository database" in new Test {
      override val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(deleteOneOrMoreJourneys = downstreamError)

      val result: Either[ServiceError, Unit] = underTest.clearConstructionExpensesData(journeyCtxWithNino).value.futureValue

      result shouldBe downstreamError
      repo.lastUpsertedAnswer shouldBe None
    }
  }

  trait Test {
    val connector: StubIFSConnector = new StubIFSConnector()

    val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository()
    lazy val underTest                     = new ExpensesAnswersServiceImpl(connector, repo)

    implicit val hc: HeaderCarrier = HeaderCarrier()
  }
  trait Test2 {
    lazy val connector: StubIFSConnector          = new StubIFSConnector()
    val repository: MongoJourneyAnswersRepository = new MongoJourneyAnswersRepository(mongoComponent, mockAppConfig, clock)

    lazy val underTest = new ExpensesAnswersServiceImpl(connector, repository)

    implicit val hc: HeaderCarrier = HeaderCarrier()

    def prepareDatabase(): EitherT[Future, ServiceError, Unit] = for {
      _ <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(expensesTailoringCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(officeSuppliesCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(goodsToSellOrUseCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(repairsAndMaintenanceCostsCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(staffCostsCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(workplaceRunningCostsCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(capitalAllowancesTailoringCtx, Json.obj("field" -> "value"))
      _ <- repository.upsertAnswers(zeroEmissionCarsCtx, Json.obj("field" -> "value"))
    } yield ()

    def preparePeriodData(): Unit = {
      def existingPeriodData = buildPeriodData(Some(DeductionsTestData.sample))
      connector.amendSEPeriodSummaryResultData = Some(existingPeriodData)
    }

    def prepareAnnualSummariesData(): Unit = {
      val adjustments   = genOne(annualAdjustmentsTypeGen).toApi1802AnnualAdjustments
      val allowances    = AnnualAllowancesData.example
      val nonFinancials = AnnualNonFinancials(true, Some("002"))
      val existingAnnualSummariesData = CreateAmendSEAnnualSubmissionRequestData(
        taxYear,
        nino,
        businessId,
        CreateAmendSEAnnualSubmissionRequestBody(adjustments.some, allowances.some, nonFinancials.some))
      connector.upsertAnnualSummariesSubmissionData = Some(existingAnnualSummariesData)
    }

    def prepareData(): Either[ServiceError, Unit] = {
      preparePeriodData()
      prepareAnnualSummariesData()
      prepareDatabase().value.futureValue
    }

    lazy val periodicApiResult: Option[Deductions]     = connector.amendSEPeriodSummaryResultData.flatMap(_.body.deductions)
    lazy val annualApiResult: Option[AnnualAllowances] = connector.upsertAnnualSummariesSubmissionData.flatMap(_.body.annualAllowances)
    lazy val (
      incomeResult,
      expensesTailoringResult,
      officeSuppliesResult,
      goodsToSellOrUseResult,
      workplaceRunningCostsResult,
      repairsAndMaintenanceCosts,
      staffCosts,
      constructionCostsResult,
      professionalFees,
      irrecoverableDebts,
      capitalAllowancesResult,
      zeroEmissionCarsResult) =
      (for {
        income                     <- repository.get(incomeCtx)
        expensesTailoring          <- repository.get(expensesTailoringCtx)
        officeSupplies             <- repository.get(officeSuppliesCtx)
        goodsToSellOrUse           <- repository.get(goodsToSellOrUseCtx)
        workplaceRunningCosts      <- repository.get(workplaceRunningCostsCtx)
        repairsAndMaintenanceCosts <- repository.get(repairsAndMaintenanceCostsCtx)
        staffCosts                 <- repository.get(staffCostsCtx)
        professionalFees           <- repository.get(professionalFeesCtx)
        irrecoverableDebts         <- repository.get(irrecoverableDebtsExpensesCtx)
        constructionCosts          <- repository.get(constructionCostsCtx)
        capitalAllowancesTailoring <- repository.get(capitalAllowancesTailoringCtx)
        zeroEmissionCars           <- repository.get(zeroEmissionCarsCtx)
      } yield (
        income,
        expensesTailoring,
        officeSupplies,
        goodsToSellOrUse,
        workplaceRunningCosts,
        repairsAndMaintenanceCosts,
        staffCosts,
        professionalFees,
        irrecoverableDebts,
        constructionCosts,
        capitalAllowancesTailoring,
        zeroEmissionCars)).rightValue
  }
}

object ExpensesAnswersServiceImplSpec {
  val clock: TestClock         = mkClock(mkNow())
  val mockAppConfig: AppConfig = mock[AppConfig]

  val tailoringJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.ExpensesTailoring,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )
  val goodsToSellOrUseJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.GoodsToSellOrUse,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )
  val workplaceRunningCostsJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.WorkplaceRunningCosts,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
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
    false,
    None,
    None,
    None,
    None,
    None
  )

  val downstreamError: Either[SingleDownstreamError, Nothing] =
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serverError).asLeft

  def buildPeriodData(deductions: Option[Deductions]): AmendSEPeriodSummaryRequestData =
    AmendSEPeriodSummaryRequestData(taxYear, nino, businessId, AmendSEPeriodSummaryRequestBody(Some(Incomes(Some(100.00), None, None)), deductions))
}
