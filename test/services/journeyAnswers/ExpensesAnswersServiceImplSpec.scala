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

import cats.implicits._
import gens.ExpensesJourneyAnswersGen._
import gens.ExpensesTailoringAnswersGen.expensesTailoringIndividualCategoriesAnswersGen
import gens.genOne
import models.common.JourneyName.ExpensesTailoring
import models.common.{JourneyName, JourneyStatus}
import models.connector.Api1786ExpensesResponseParser.goodsToSellOrUseParser
import models.connector.api_1895.request._
import models.database.JourneyAnswers
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb, WorkplaceRunningCostsDb}
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.tailoring.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.{AsOneTotalAnswers, NoExpensesAnswers}
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import services.journeyAnswers.ExpensesAnswersServiceImplSpec._
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExpensesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {

  trait Test {
    val connector: StubIFSConnector = StubIFSConnector()

    val repo           = StubJourneyAnswersRepository()
    lazy val underTest = new ExpensesAnswersServiceImpl(connector, repo)

    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

  "save answers" should {
    "saveTailoringAnswers" in new Test {
      val answers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result  = underTest.saveTailoringAnswers(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveOfficeSuppliesAnswers" in new Test {
      val answers = genOne(officeSuppliesJourneyAnswersGen)
      val result  = underTest.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveGoodsToSell" in new Test {
      val answers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result  = underTest.saveGoodsToSell(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveRepairsAndMaintenance" in new Test {
      val answers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
      val result  = underTest.saveRepairsAndMaintenance(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveWorkplaceRunningCosts" in new Test {
      val answers = genOne(workplaceRunningCostsJourneyAnswersGen)
      val result  = underTest.saveWorkplaceRunningCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveAdvertisingOrMarketing" in new Test {
      val answers = genOne(advertisingOrMarketingJourneyAnswersGen)
      val result  = underTest.saveAdvertisingOrMarketing(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveEntertainmentCosts" in new Test {
      val answers = genOne(entertainmentJourneyAnswersGen)
      val result  = underTest.saveEntertainmentCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveStaffCosts" in new Test {
      val answers = genOne(staffCostsJourneyAnswersGen)
      val result  = underTest.saveStaffCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveConstructionIndustrySubcontractors" in new Test {
      val answers = genOne(constructionJourneyAnswersGen)
      val result  = underTest.saveConstructionIndustrySubcontractors(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveProfessionalFees" in new Test {
      val answers = genOne(professionalFeesJourneyAnswersGen)
      val result  = underTest.saveProfessionalFees(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveFinancialCharges" in new Test {
      val answers = genOne(financialChargesJourneyAnswersGen)
      val result  = underTest.saveFinancialCharges(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveBadDebts" in new Test {
      val answers = genOne(irrecoverableDebtsJourneyAnswersGen)
      val result  = underTest.saveBadDebts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveDepreciationCosts" in new Test {
      val answers = genOne(depreciationCostsJourneyAnswersGen)
      val result  = underTest.saveDepreciationCosts(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveOtherExpenses" in new Test {
      val answers = genOne(otherExpensesJourneyAnswersGen)
      val result  = underTest.saveOtherExpenses(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

    "saveInterests" in new Test {
      val answers = genOne(interestJourneyAnswersGen)
      val result  = underTest.saveInterests(journeyCtxWithNino, answers).value.futureValue
      result shouldBe ().asRight
    }

  }

  "save ExpensesTailoringNoExpensesAnswers" should {
    "store data successfully" in new Test {
      val answers = NoExpensesAnswers
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save ExpensesTailoringIndividualCategoriesAnswers" should {
    "store data successfully" in new Test {
      val answers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save expenses journey answers" should {
    "store data successfully" in new Test {

      val someExpensesAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result              = underTest.saveGoodsToSell(journeyCtxWithNino, someExpensesAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }
  "get journey answers" in new Test {
    override val connector =
      StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))

    val result = underTest.getAnswers(journeyCtxWithNino)(goodsToSellOrUseParser, hc).value.futureValue

    val expectedResult = GoodsToSellOrUseJourneyAnswers(100.0, Some(100.0))

    result shouldBe expectedResult.asRight
  }

  "getExpensesTailoringAnswers" should {
    "return None when there are no answers" in new Test {

      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return NoExpensesAnswers" in new Test {

      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(ExpensesCategoriesDb(NoExpenses)).as[JsObject])
        .some)
      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe NoExpensesAnswers.some.asRight
    }

    "return AsOneTotalAnswers" in new Test {
      override val connector = StubIFSConnector(
        getPeriodicSummaryDetailResult = Future.successful(
          api1786DeductionsSuccessResponse
            .copy(financials = api1786DeductionsSuccessResponse.financials
              .copy(deductions = api1786DeductionsSuccessResponse.financials.deductions.map(_.copy(simplifiedExpenses = BigDecimal("10.5").some))))
            .asRight)
      )
      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(ExpensesCategoriesDb(TotalAmount)).as[JsObject])
        .some)
      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe AsOneTotalAnswers(BigDecimal("10.5")).some.asRight
    }

    "return ExpensesTailoringIndividualCategoriesAnswers" in new Test {
      val answers = genOne(expensesTailoringIndividualCategoriesAnswersGen)

      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(answers).as[JsObject])
        .some)
      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe answers.some.asRight
    }

  }

  "getGoodsToSellOrUseAnswers" should {
    "return None when there are no answers" in new Test {

      val result = underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return GoodsToSellOrUseAnswers when they exist" in new Test {
      override val connector =
        StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo = StubJourneyAnswersRepository(getAnswer = goodsToSellOrUseJourneyAnswers
        .copy(data = Json.toJson(TaxiMinicabOrRoadHaulageDb(true)).as[JsObject])
        .some)
      val result = underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe GoodsToSellOrUseAnswers(true, BigDecimal("100.0"), Some(BigDecimal("100.0"))).some.asRight
    }
  }

  "getWorkplaceRunningCostsAnswers" should {
    "return None when there are no answers" in new Test {

      val result = underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return WorkplaceRunningCostsAnswers when they exist" in new Test {
      override val connector =
        StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo = StubJourneyAnswersRepository(getAnswer = workplaceRunningCostsJourneyAnswers
        .copy(data = Json.toJson(workplaceRunningCostsDb).as[JsObject])
        .some)
      val result = underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe WorkplaceRunningCostsAnswers(workplaceRunningCostsDb, api1786DeductionsSuccessResponse).some.asRight
    }
  }

  "deleteSimplifiedExpensesAnswers" should {
    def buildPeriodData(deductions: Option[Deductions]): AmendSEPeriodSummaryRequestData =
      AmendSEPeriodSummaryRequestData(taxYear, nino, businessId, AmendSEPeriodSummaryRequestBody(Some(Incomes(Some(100.00), None, None)), deductions))
    "remove Tailoring DB answers and delete SimplifiedAmount API answer" in new Test {
      val existingPeriodData = buildPeriodData(Some(DeductionsTestData.sample))

      override val connector = StubIFSConnector()
      connector.amendSEPeriodSummaryResultData = Some(existingPeriodData)
      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers.some)
      repo.lastUpsertedAnswer = Some(Json.toJson(ExpensesCategoriesDb(IndividualCategories)))

      underTest.deleteSimplifiedExpensesAnswers(journeyCtxWithNino).value.map { result =>
        result shouldBe ().asRight
        connector.amendSEPeriodSummaryResultData shouldBe None
        repo.lastUpsertedAnswer shouldBe None
      }
    }
  }
}

object ExpensesAnswersServiceImplSpec {
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

}
