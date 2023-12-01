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
import gens.ExpensesTailoringAnswersGen.expensesTailoringAnswersGen
import gens.GoodsToSellOrUseJourneyAnswersGen.goodsToSellOrUseJourneyAnswersGen
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.connectors.StubSelfEmploymentBusinessConnector
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {
  val connector = StubSelfEmploymentBusinessConnector()
  val underTest = new ExpensesAnswersServiceImpl(connector)

  implicit val hc = HeaderCarrier()

  "save ExpensesTailoringAnswers" should {
    // TODO add check in SASS-6340
    "store data successfully" in {
      val answers = expensesTailoringAnswersGen.sample.get
      val result  = underTest.saveAnswers(businessId, currTaxYear, mtditid, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save GoodsToSellOrUseJourneyAnswers" should {
    "store data successfully" in {
      val answers = goodsToSellOrUseJourneyAnswersGen.sample.get
      val result  = underTest.saveAnswers(businessId, currTaxYear, mtditid, nino, answers).value.futureValue
      result shouldBe ().asRight
    }
  }
}