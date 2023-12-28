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

import cats.implicits.catsSyntaxEitherId
import gens.ExpensesJourneyAnswersGen.goodsToSellOrUseJourneyAnswersGen
import gens.ExpensesTailoringAnswersGen.{expensesTailoringIndividualCategoriesAnswersGen, expensesTailoringNoExpensesAnswersGen}
import gens.genOne
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import parsers.expenses.ExpensesResponseParser
import stubs.connectors.StubSelfEmploymentConnector
import stubs.connectors.StubSelfEmploymentConnector.api1786DeductionsSuccessResponse
import ExpensesResponseParser.goodsToSellOrUseParser
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

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

      val answers = genOne(expensesTailoringNoExpensesAnswersGen)
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save ExpensesTailoringIndividualCategoriesAnswers" should {
    "store data successfully" in new Test {
      override val connector = StubSelfEmploymentConnector()

      val answers = genOne(expensesTailoringIndividualCategoriesAnswersGen)
      val result  = underTest.persistAnswers(businessId, currTaxYear, mtditid, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save expenses journey answers" should {
    "store data successfully" in new Test {
      override val connector = StubSelfEmploymentConnector()

      val someExpensesAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result              = underTest.sendAnswers(journeyCtxWithNino, someExpensesAnswers).value.futureValue
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
}
