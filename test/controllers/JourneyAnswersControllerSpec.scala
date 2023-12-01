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

package controllers

import controllers.ControllerBehaviours.buildRequest
import gens.ExpensesTailoringAnswersGen.expensesTailoringAnswersGen
import gens.GoodsToSellOrUseJourneyAnswersGen._
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.NO_CONTENT
import stubs.services.{StubExpensesAnswersService, StubIncomeAnswersService}
import utils.BaseSpec._

class JourneyAnswersControllerSpec extends ControllerBehaviours with ScalaCheckPropertyChecks {
  val underTest = new JourneyAnswersController(
    auth = mockAuthorisedAction,
    cc = stubControllerComponents,
    incomeService = StubIncomeAnswersService(),
    expensesService = StubExpensesAnswersService()
  )

  "JourneyAnswersController" should {
    s"return a $NO_CONTENT when saveIncomeAnswers" in forAll(incomeJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveIncomeAnswers(currTaxYear, businessId)
      )
    }

    s"return a $NO_CONTENT when saveExpensesTailoringAnswers" in forAll(expensesTailoringAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveExpensesTailoringAnswers(currTaxYear, businessId)
      )
    }

    s"return a $NO_CONTENT when saveGoodsToSellOrUse" in forAll(goodsToSellOrUseJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveGoodsToSellOrUse(currTaxYear, businessId, nino)
      )
    }
  }
}
