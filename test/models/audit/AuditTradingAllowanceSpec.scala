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

package models.audit

import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.BusinessId
import models.common.TaxYear.asTys
import models.frontend.income.HowMuchTradingAllowance.{LessThan, Maximum}
import models.frontend.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import utils.BaseSpec
import utils.BaseSpec.{journeyCtxWithNino, nino}

class AuditTradingAllowanceSpec extends BaseSpec with Matchers with OptionValues {

  val years: String = asTys(journeyCtxWithNino.taxYear)

  "AuditTradingAllowance" should {
    "convert the user answers to Trading allowance  when DeclareExpenses is selected" in {
      val incomeJourneyAnswers = incomeJourneyAnswersGen.sample.value.copy(tradingAllowance = DeclareExpenses)

      val result = AuditTradingAllowance.apply(journeyCtxWithNino, Option("tradingName"), incomeJourneyAnswers)
      result shouldBe AuditTradingAllowance(
        nino,
        BusinessId("SJPR05893938418"),
        Option("tradingName"),
        years,
        isTradingIncomeAllowanceBeingUsed = false,
        None,
        None)
    }

    "convert the user answers to Trading allowance  when UseTradingAllowance is selected and howMuchTradingAllowance is selected as 'Maximum'" in {
      val incomeJourneyAnswers =
        incomeJourneyAnswersGen.sample.value.copy(tradingAllowance = UseTradingAllowance, howMuchTradingAllowance = Option(Maximum))

      val result = AuditTradingAllowance.apply(journeyCtxWithNino, None, incomeJourneyAnswers)
      result shouldBe AuditTradingAllowance(
        nino,
        BusinessId("SJPR05893938418"),
        None,
        years,
        isTradingIncomeAllowanceBeingUsed = true,
        Option(true),
        None)
    }

    "convert the user answers to Trading allowance  when UseTradingAllowance is selected and howMuchTradingAllowance is selected as 'LessThan'" in {
      val incomeJourneyAnswers = incomeJourneyAnswersGen.sample.value.copy(
        tradingAllowance = UseTradingAllowance,
        howMuchTradingAllowance = Option(LessThan),
        tradingAllowanceAmount = Option(BigDecimal(20.00)))

      val result = AuditTradingAllowance.apply(journeyCtxWithNino, None, incomeJourneyAnswers)
      result shouldBe AuditTradingAllowance(
        nino,
        BusinessId("SJPR05893938418"),
        None,
        years,
        isTradingIncomeAllowanceBeingUsed = true,
        Option(false),
        Option(BigDecimal(20.00)))
    }

    "serialise to json" in {

      val tradingAllowance =
        AuditTradingAllowance(
          nino,
          BusinessId("SJPR05893938418"),
          Option("BusinessName"),
          years,
          isTradingIncomeAllowanceBeingUsed = true,
          Option(true),
          None)

      Json
        .toJson(tradingAllowance) shouldBe Json.parse(
        """{"nino":"nino","businessId":"SJPR05893938418","businessName":"BusinessName","taxYear":"25-26","isTradingIncomeAllowanceBeingUsed":true,"isMaximumTradingIncomeAllowanceBeingUsed":true}""")

    }
  }
}
