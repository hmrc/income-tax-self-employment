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

import models.connector.api_1802.request.{AnnualAdjustments, AnnualAllowances}
import models.connector.{api_1786, api_1803}
import models.database.income.IncomeStorageAnswers
import models.frontend.FrontendAnswers
import play.api.libs.json.{Json, OFormat}

case class IncomeJourneyAnswers(incomeNotCountedAsTurnover: Boolean,
                                nonTurnoverIncomeAmount: Option[BigDecimal],
                                turnoverIncomeAmount: BigDecimal,
                                anyOtherIncome: Boolean,
                                otherIncomeAmount: Option[BigDecimal],
                                turnoverNotTaxable: Option[Boolean],
                                notTaxableAmount: Option[BigDecimal],
                                tradingAllowance: TradingAllowance,
                                howMuchTradingAllowance: Option[HowMuchTradingAllowance],
                                tradingAllowanceAmount: Option[BigDecimal])
    extends FrontendAnswers[IncomeStorageAnswers] {

  def toDbModel: Option[IncomeStorageAnswers] = Option(IncomeStorageAnswers(
    incomeNotCountedAsTurnover,
    anyOtherIncome,
    turnoverNotTaxable,
    tradingAllowance,
    howMuchTradingAllowance
  ))

  override def toDownStreamAnnualAdjustments(current: Option[AnnualAdjustments]): AnnualAdjustments =
    current.getOrElse(AnnualAdjustments.empty).copy(includedNonTaxableProfits = notTaxableAmount, outstandingBusinessIncome = otherIncomeAmount)

  override def toDownStreamAnnualAllowances(current: Option[AnnualAllowances]): AnnualAllowances =
    current.getOrElse(AnnualAllowances.empty).copy(tradingIncomeAllowance = tradingAllowanceAmount)
}

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
        _.tradingIncomeAllowance
      ) // TODO should tradingAllowanceAmount have 'if (tradingAllowance == declareExpenses) None else ...'
    )
}
