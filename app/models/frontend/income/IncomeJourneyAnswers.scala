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

import models.connector.api_1803
import models.connector.api_1786
import models.database.income.IncomeStorageAnswers
import play.api.libs.json.{Json, OFormat}

case class IncomeJourneyAnswers(incomeNotCountedAsTurnover: Boolean,
                                nonTurnoverIncomeAmount: Option[BigDecimal], // amend, create
                                turnoverIncomeAmount: BigDecimal, // amend, create
                                anyOtherIncome: Boolean,
                                otherIncomeAmount: Option[BigDecimal], // upsert
                                turnoverNotTaxable: Option[Boolean],
                                notTaxableAmount: Option[BigDecimal], // upsert
                                tradingAllowance: TradingAllowance,
                                howMuchTradingAllowance: Option[HowMuchTradingAllowance],
                                tradingAllowanceAmount: Option[BigDecimal]) // upsert

object IncomeJourneyAnswers {
  implicit val formats: OFormat[IncomeJourneyAnswers] = Json.format[IncomeJourneyAnswers]

  def apply(journeyAnswers: IncomeStorageAnswers,
            periodicSummaryDetails: api_1786.SuccessResponseSchema,
            annualSummaries: api_1803.SuccessResponseSchema): IncomeJourneyAnswers =
    IncomeJourneyAnswers(
      incomeNotCountedAsTurnover = journeyAnswers.incomeNotCountedAsTurnover,
      anyOtherIncome = journeyAnswers.anyOtherIncome,
      turnoverNotTaxable = journeyAnswers.turnoverNotTaxable,
      tradingAllowance = journeyAnswers.tradingAllowance,
      howMuchTradingAllowance = journeyAnswers.howMuchTradingAllowance,
      nonTurnoverIncomeAmount = periodicSummaryDetails.financials.incomes.flatMap(_.other),
      turnoverIncomeAmount = periodicSummaryDetails.financials.incomes.flatMap(_.turnover).getOrElse(BigDecimal(0)), // TODO What if it's None?
      otherIncomeAmount = annualSummaries.annualAdjustments.flatMap(_.outstandingBusinessIncome),
      notTaxableAmount = annualSummaries.annualAdjustments.flatMap(_.includedNonTaxableProfits),
      tradingAllowanceAmount = annualSummaries.annualAllowances.flatMap(
        _.annualInvestmentAllowance
      ) // TODO should tradingAllowanceAmount have 'if (tradingAllowance == declareExpenses) None else ...'
    )
}
