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

package models.frontend.income

import models.connector.api_1786.IncomesType
import models.connector.{api_1786, api_1803}
import models.database.income.IncomeStorageAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.BaseSpec.{currTaxYearEnd, currTaxYearStart}

class IncomeJourneyAnswersSpec extends AnyWordSpec with Matchers {

  "apply" should {
    "convert storage answers, periodic details and annual summaries to journey answers" in {
      val incomeStorageAnswers = IncomeStorageAnswers(
        incomeNotCountedAsTurnover = true,
        anyOtherIncome = true,
        turnoverNotTaxable = Some(true),
        tradingAllowance = TradingAllowance.DeclareExpenses,
        howMuchTradingAllowance = None
      )
      val periodicSummaryDetails =
        api_1786.SuccessResponseSchema(
          currTaxYearStart,
          currTaxYearEnd,
          api_1786.FinancialsType(None, Some(IncomesType(Some(100.00), Some(50.00), None))))

      val annualSummaries = api_1803.SuccessResponseSchema(None, None, None)

      val expectedResult = IncomeJourneyAnswers(
        incomeNotCountedAsTurnover = true,
        nonTurnoverIncomeAmount = Some(50.00),
        turnoverIncomeAmount = 100.00,
        anyOtherIncome = true,
        otherIncomeAmount = None,
        turnoverNotTaxable = Some(true),
        notTaxableAmount = None,
        tradingAllowance = TradingAllowance.DeclareExpenses,
        howMuchTradingAllowance = None,
        tradingAllowanceAmount = None
      )

      IncomeJourneyAnswers.apply(incomeStorageAnswers, periodicSummaryDetails, annualSummaries) shouldBe expectedResult
    }
  }

}
