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
import gens.ExpensesTailoringAnswersGen.expensesTailoringAnswersGen
import models.common.JourneyContextWithNino
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.connectors.StubSelfEmploymentBusinessConnector
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global

class ExpensesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {
  val connector = StubSelfEmploymentBusinessConnector()
  val repo      = StubJourneyAnswersRepository()
  val underTest = new ExpensesAnswersServiceImpl(connector, repo)

  implicit val hc = HeaderCarrier()

  "save ExpensesTailoringAnswers" should {
    "store data successfully" in {
      val answers = expensesTailoringAnswersGen.sample.get
      val result  = underTest.saveAnswers(businessId, currTaxYear, mtditid, answers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "save expenses journey answers" should {
    "store data successfully" in {
      val ctx                 = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)
      val someExpensesAnswers = goodsToSellOrUseJourneyAnswersGen.sample.get
      val result              = underTest.saveAnswers(ctx, someExpensesAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }
}
