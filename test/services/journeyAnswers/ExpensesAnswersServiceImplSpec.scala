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
import gens.ExpensesJourneyAnswersGen.goodsToSellOrUseJourneyAnswersGen
import gens.ExpensesTailoringAnswersGen.expensesTailoringIndividualCategoriesAnswersGen
import gens.genOne
import models.common.JourneyName.ExpensesTailoring
import models.common.{JourneyName, JourneyStatus}
import models.connector.Api1786ExpensesResponseParser.goodsToSellOrUseParser
import models.database.JourneyAnswers
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb, WorkplaceRunningCostsDb}
import models.frontend.expenses.goodsToSellOrUse.TaxiMinicabOrRoadHaulage.Yes
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.tailoring.ExpensesTailoring.{NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.{AsOneTotalAnswers, NoExpensesAnswers}
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import services.journeyAnswers.ExpensesAnswersServiceImplSpec._
import stubs.connectors.StubSelfEmploymentConnector
import stubs.connectors.StubSelfEmploymentConnector.api1786DeductionsSuccessResponse
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExpensesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {

  trait Test {
    val connector: StubSelfEmploymentConnector

    val repo           = StubJourneyAnswersRepository()
    lazy val underTest = new ExpensesAnswersServiceImpl(connector, repo)

    implicit val hc = HeaderCarrier()
  }

  "save ExpensesTailoringNoExpensesAnswers" should {
    "store data successfully" in new Test {
      override val connector = StubSelfEmploymentConnector()

      val answers = NoExpensesAnswers
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save ExpensesTailoringIndividualCategoriesAnswers" should {
    "store data successfully" in new Test {
      override val connector = StubSelfEmploymentConnector()

      val answers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, ExpensesTailoring, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save expenses journey answers" should {
    "store data successfully" in new Test {
      override val connector = StubSelfEmploymentConnector()

      val someExpensesAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result              = underTest.saveAnswers(journeyCtxWithNino, someExpensesAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }
  "get journey answers" in new Test {
    override val connector =
      StubSelfEmploymentConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))

    val result = underTest.getAnswers(journeyCtxWithNino)(goodsToSellOrUseParser, hc).value.futureValue

    val expectedResult = GoodsToSellOrUseJourneyAnswers(100.0, Some(100.0))

    result shouldBe expectedResult.asRight
  }

  "getExpensesTailoringAnswers" should {
    "return None when there are no answers" in new Test {
      override val connector = StubSelfEmploymentConnector()
      val result             = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return NoExpensesAnswers" in new Test {
      override val connector = StubSelfEmploymentConnector()
      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(ExpensesCategoriesDb(NoExpenses)).as[JsObject])
        .some)
      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe NoExpensesAnswers.some.asRight
    }

    "return AsOneTotalAnswers" in new Test {
      override val connector = StubSelfEmploymentConnector(
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

      override val connector = StubSelfEmploymentConnector()
      override val repo = StubJourneyAnswersRepository(getAnswer = tailoringJourneyAnswers
        .copy(data = Json.toJson(answers).as[JsObject])
        .some)
      val result = underTest.getExpensesTailoringAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe answers.some.asRight
    }

  }

  "getGoodsToSellOrUseAnswers" should {
    "return None when there are no answers" in new Test {
      override val connector = StubSelfEmploymentConnector()
      val result             = underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return GoodsToSellOrUseAnswers when they exist" in new Test {
      override val connector =
        StubSelfEmploymentConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo = StubJourneyAnswersRepository(getAnswer = goodsToSellOrUseJourneyAnswers
        .copy(data = Json.toJson(TaxiMinicabOrRoadHaulageDb(Yes)).as[JsObject])
        .some)
      val result = underTest.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe GoodsToSellOrUseAnswers(Yes, BigDecimal("100.0"), Some(BigDecimal("100.0"))).some.asRight
    }
  }

  "getWorkplaceRunningCostsAnswers" should {
    "return None when there are no answers" in new Test {
      override val connector = StubSelfEmploymentConnector()
      val result             = underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe None.asRight
    }

    "return WorkplaceRunningCostsAnswers when they exist" in new Test {
      override val connector =
        StubSelfEmploymentConnector(getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight))
      override val repo = StubJourneyAnswersRepository(getAnswer = workplaceRunningCostsJourneyAnswers
        .copy(data = Json.toJson(workplaceRunningCostsDb).as[JsObject])
        .some)
      val result = underTest.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(hc).value.futureValue
      result shouldBe WorkplaceRunningCostsAnswers(workplaceRunningCostsDb, api1786DeductionsSuccessResponse).some.asRight
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
