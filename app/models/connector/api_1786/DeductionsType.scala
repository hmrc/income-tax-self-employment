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

package models.connector.api_1786

import models.connector.api_1895
import models.connector.api_1894
import play.api.libs.json._

/** Represents the Swagger definition for deductionsType.
  * @param simplifiedExpenses
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  */
case class DeductionsType(
    adminCosts: Option[SelfEmploymentDeductionsDetailType],
    advertisingCosts: Option[SelfEmploymentDeductionsDetailType],
    badDebt: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    constructionIndustryScheme: Option[SelfEmploymentDeductionsDetailType],
    costOfGoods: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    depreciation: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    financialCharges: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    interest: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    maintenanceCosts: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    other: Option[SelfEmploymentDeductionsDetailType],
    professionalFees: Option[SelfEmploymentDeductionsDetailTypeAllowableNegDisPos],
    premisesRunningCosts: Option[SelfEmploymentDeductionsDetailTypePosNeg],
    staffCosts: Option[SelfEmploymentDeductionsDetailType],
    travelCosts: Option[SelfEmploymentDeductionsDetailType],
    businessEntertainmentCosts: Option[SelfEmploymentDeductionsDetailType],
    simplifiedExpenses: Option[BigDecimal]
) {
  def toApi1895: api_1895.request.Deductions = api_1895.request.Deductions(
    costOfGoods = costOfGoods.map(_.toApi1895),
    constructionIndustryScheme = constructionIndustryScheme.map(_.toApi1895),
    staffCosts = staffCosts.map(_.toApi1895),
    travelCosts = travelCosts.map(_.toApi1895),
    premisesRunningCosts = premisesRunningCosts.map(_.toApi1895),
    maintenanceCosts = maintenanceCosts.map(_.toApi1895),
    adminCosts = adminCosts.map(_.toApi1895),
    businessEntertainmentCosts = businessEntertainmentCosts.map(_.toApi1895),
    advertisingCosts = advertisingCosts.map(_.toApi1895),
    interest = interest.map(_.toApi1895),
    financialCharges = financialCharges.map(_.toApi1895),
    badDebt = badDebt.map(_.toApi1895),
    professionalFees = professionalFees.map(_.toApi1895),
    depreciation = depreciation.map(_.toApi1895),
    other = other.map(_.toApi1895),
    simplifiedExpenses = simplifiedExpenses
  )

  def toApi1894: api_1894.request.Deductions = api_1894.request.Deductions(
    costOfGoods = costOfGoods.map(_.toApi1894),
    constructionIndustryScheme = constructionIndustryScheme.map(_.toApi1894),
    staffCosts = staffCosts.map(_.toApi1894),
    travelCosts = travelCosts.map(_.toApi1894),
    premisesRunningCosts = premisesRunningCosts.map(_.toApi1894),
    maintenanceCosts = maintenanceCosts.map(_.toApi1894),
    adminCosts = adminCosts.map(_.toApi1894),
    businessEntertainmentCosts = businessEntertainmentCosts.map(_.toApi1894),
    advertisingCosts = advertisingCosts.map(_.toApi1894),
    interest = interest.map(_.toApi1894),
    financialCharges = financialCharges.map(_.toApi1894),
    badDebt = badDebt.map(_.toApi1894),
    professionalFees = professionalFees.map(_.toApi1894),
    depreciation = depreciation.map(_.toApi1894),
    other = other.map(_.toApi1894),
    simplifiedExpenses = simplifiedExpenses
  )
}

object DeductionsType {
  implicit lazy val deductionsTypeJsonFormat: Format[DeductionsType] = Json.format[DeductionsType]

  val empty: DeductionsType = DeductionsType(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
}
