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

package gens

import models.frontend.income.{HowMuchTradingAllowance, IncomeJourneyAnswers, TradingAllowance}
import models.frontend.prepop.IncomePrepopAnswers
import org.scalacheck.Gen

object IncomeJourneyAnswersGen {
  private val tradingAllowanceGen: Gen[TradingAllowance]               = Gen.oneOf(TradingAllowance.values)
  private val howMuchTradingAllowanceGen: Gen[HowMuchTradingAllowance] = Gen.oneOf(HowMuchTradingAllowance.values)

  val incomePrepopAnswersGen: Gen[IncomePrepopAnswers] = for {
    turnoverIncome <- Gen.option(bigDecimalGen)
    otherIncome    <- Gen.option(bigDecimalGen)
  } yield IncomePrepopAnswers(turnoverIncome, otherIncome)

  val incomeJourneyAnswersGen: Gen[IncomeJourneyAnswers] = for {
    incomeNotCountedAsTurnover <- booleanGen
    nonTurnoverIncomeAmount    <- Gen.option(bigDecimalGen)
    turnoverIncomeAmount       <- bigDecimalGen
    anyOtherIncome             <- booleanGen
    otherIncomeAmount          <- Gen.option(bigDecimalGen)
    turnoverNotTaxable         <- Gen.option(booleanGen)
    notTaxableAmount           <- Gen.option(bigDecimalGen)
    tradingAllowance           <- tradingAllowanceGen
    howMuchTradingAllowance    <- Gen.option(howMuchTradingAllowanceGen)
    tradingAllowanceAmount     <- Gen.option(bigDecimalGen)
  } yield IncomeJourneyAnswers(
    incomeNotCountedAsTurnover,
    nonTurnoverIncomeAmount,
    turnoverIncomeAmount,
    anyOtherIncome,
    otherIncomeAmount,
    turnoverNotTaxable,
    notTaxableAmount,
    tradingAllowance,
    howMuchTradingAllowance,
    tradingAllowanceAmount
  )

}
