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

package models.api

import models.api.PeriodicSummaries.Financials
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class PeriodicSummaries(
  from: LocalDate,
  to: LocalDate,
  financials: Financials
)

object PeriodicSummaries {
  implicit val periodicSummariesFormat: OFormat[PeriodicSummaries] = Json.format[PeriodicSummaries]
  
  case class Financials(
    incomes: Incomes,
    deductions: Deductions
  )
  object Financials {
    implicit val financialsFormat: OFormat[Financials] = Json.format[Financials]
  }
  case class Incomes(
    turnover: BigDecimal,
    other: BigDecimal
  )
  object Incomes {
    implicit val incomesFormat: OFormat[Incomes] = Json.format[Incomes]
  }
  case class Deductions(
    costOfGoods: Deduction,
    constructionIndustryScheme: Deduction,
    staffCosts: Deduction,
    travelCosts: Deduction,
    premisesRunningCosts: Deduction,
    maintenanceCosts: Deduction,
    adminCosts: Deduction,
    advertisingCosts: Deduction,
    interest: Deduction,
    financialCharges: Deduction,
    badDebt: Deduction,
    professionalFees: Deduction,
    depreciation: Deduction,
    other: Deduction,
    simplifiedExpenses: BigDecimal
  )
  object Deductions {
    implicit val deductionsFormat: OFormat[Deductions] = Json.format[Deductions]
  }
  case class Deduction(
    amount: BigDecimal,   //TODO would like a money type => int.[d][d]
    disallowableAmount: BigDecimal
  )
  object Deduction {
    implicit val deductionFormat: OFormat[Deduction] = Json.format[Deduction]
  }
}
