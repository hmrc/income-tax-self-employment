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

import cats.data.EitherT
import cats.implicits._
import controllers.ControllerBehaviours.{buildRequest, buildRequestNoContent}
import gens.ExpensesJourneyAnswersGen._
import gens.ExpensesTailoringAnswersGen._
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import gens.genOne
import models.common.JourneyContextWithNino
import models.error.ServiceError
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import parsers.expenses.ExpensesResponseParser
import play.api.http.Status._
import play.api.libs.json.Json
import services.journeyAnswers.ExpensesAnswersService
import stubs.services.{StubExpensesAnswersService, StubIncomeAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import scala.concurrent.Future

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

  "getIncomeAnswers" should {
    s"return $NO_CONTENT if there is no answers" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.getIncomeAnswers(currTaxYear, businessId, nino)
      )
    }

    s"return answers" in {
      val answers = genOne(incomeJourneyAnswersGen)
      val underTest = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        incomeService = StubIncomeAnswersService(getAnswersRes = Some(answers).asRight),
        expensesService = StubExpensesAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getIncomeAnswers(currTaxYear, businessId, nino)
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

  "saveExpensesTailoringTotalAmountAnswers" should {
    s"return a $NO_CONTENT when successful" in forAll(expensesTailoringTotalAmountAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveExpensesTailoringTotalAmountAnswers(currTaxYear, businessId, nino)
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
    "getGoodsToSellOrUseAnswers" should {
      s"return a $OK and answers as json when successful" in new GetExpensesTest {
        val someAnswers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)

        (expensesService
          .getAnswers(_: JourneyContextWithNino)(_: ExpensesResponseParser[GoodsToSellOrUseJourneyAnswers], _: HeaderCarrier))
          .expects(*, *, *)
          .returns(EitherT.right[ServiceError](Future.successful(someAnswers)))

        behave like testRoute(
          request = buildRequestNoContent,
          expectedStatus = OK,
          expectedBody = Json.stringify(Json.toJson(someAnswers)),
          methodBlock = () => controller.getGoodsToSellOrUseAnswers(currTaxYear, businessId, nino)
        )
      }
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

  trait GetExpensesTest {
    val expensesService: ExpensesAnswersService = mock[ExpensesAnswersService]

    val controller = new JourneyAnswersController(
      auth = mockAuthorisedAction,
      cc = stubControllerComponents,
      incomeService = StubIncomeAnswersService(),
      expensesService = expensesService
    )
  }
}
