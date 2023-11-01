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

package models.journeys.income

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}

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

sealed abstract class TradingAllowance(override val entryName: String) extends EnumEntry

object TradingAllowance extends Enum[TradingAllowance] with PlayJsonEnum[TradingAllowance] {

  case object UseTradingAllowance extends TradingAllowance("useTradingAllowance")
  case object DeclareExpenses     extends TradingAllowance("declareExpenses")

  override def values: IndexedSeq[TradingAllowance] = findValues
}

sealed abstract class HowMuchTradingAllowance(override val entryName: String) extends EnumEntry

object HowMuchTradingAllowance extends Enum[HowMuchTradingAllowance] with PlayJsonEnum[HowMuchTradingAllowance] {
  case object Maximum  extends HowMuchTradingAllowance("maximum")
  case object LessThan extends HowMuchTradingAllowance("lessThan")

  override def values: IndexedSeq[HowMuchTradingAllowance] = findValues
}
