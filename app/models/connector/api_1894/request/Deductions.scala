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

package models.connector.api_1894.request

import play.api.libs.json._

/** Represents the Swagger definition for deductionsType.
  * @param simplifiedExpenses
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  */
case class Deductions(
    costOfGoods: Option[SelfEmploymentDeductionsDetailPosNegType],
    constructionIndustryScheme: Option[SelfEmploymentDeductionsDetailType],
    staffCosts: Option[SelfEmploymentDeductionsDetailType],
    travelCosts: Option[SelfEmploymentDeductionsDetailType],
    premisesRunningCosts: Option[SelfEmploymentDeductionsDetailPosNegType],
    maintenanceCosts: Option[SelfEmploymentDeductionsDetailPosNegType],
    adminCosts: Option[SelfEmploymentDeductionsDetailType],
    businessEntertainmentCosts: Option[SelfEmploymentDeductionsDetailType],
    advertisingCosts: Option[SelfEmploymentDeductionsDetailType],
    interest: Option[SelfEmploymentDeductionsDetailPosNegType],
    financialCharges: Option[SelfEmploymentDeductionsDetailPosNegType],
    badDebt: Option[SelfEmploymentDeductionsDetailPosNegType],
    professionalFees: Option[SelfEmploymentDeductionsDetailAllowablePosNegType],
    depreciation: Option[SelfEmploymentDeductionsDetailPosNegType],
    other: Option[SelfEmploymentDeductionsDetailType],
    simplifiedExpenses: Option[BigDecimal]
)

object Deductions {
  implicit lazy val deductionsTypeJsonFormat: Format[Deductions] = Json.format[Deductions]

  val empty: Deductions = Deductions(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
}
