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
import gens.ExpensesJourneyAnswersGen._
import gens.ExpensesTailoringAnswersGen.{expensesTailoringIndividualCategoriesAnswersGen, expensesTailoringNoExpensesAnswersGen}
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

  "saveIncomeAnswers" should {
    s"return a $NO_CONTENT when successful" in forAll(incomeJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveIncomeAnswers(currTaxYear, businessId, nino)
      )
    }
  }

  "saveExpensesTailoringNoExpensesAnswers" should {
    s"return a $NO_CONTENT when successful" in forAll(expensesTailoringNoExpensesAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveExpensesTailoringNoExpensesAnswers(currTaxYear, businessId)
      )
    }
  }

  "saveExpensesTailoringIndividualCategoriesAnswers" should {
    s"return a $NO_CONTENT when successful" in forAll(expensesTailoringIndividualCategoriesAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveExpensesTailoringIndividualCategoriesAnswers(currTaxYear, businessId)
      )
    }
  }

  "saveGoodsToSellOrUse" should {
    s"return a $NO_CONTENT when successful" in forAll(goodsToSellOrUseJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveGoodsToSellOrUse(currTaxYear, businessId, nino)
      )
    }
  }
  "saveOfficeSupplies" should {
    s"return a $NO_CONTENT when successful" in forAll(officeSuppliesJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveOfficeSupplies(currTaxYear, businessId, nino)
      )
    }
  }
  "saveRepairsAndMaintenanceCosts" should {
    s"return a $NO_CONTENT when successful" in forAll(repairsAndMaintenanceCostsJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveRepairsAndMaintenanceCosts(currTaxYear, businessId, nino)
      )
    }
  }
  "saveStaffCosts" should {
    s"return a $NO_CONTENT when successful" in forAll(staffCostsJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveStaffCosts(currTaxYear, businessId, nino)
      )
    }
  }
  "saveEntertainmentCosts" should {
    s"return a $NO_CONTENT when successful" in forAll(entertainmentCostsJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveEntertainmentCosts(currTaxYear, businessId, nino)
      )
    }
  }
}
