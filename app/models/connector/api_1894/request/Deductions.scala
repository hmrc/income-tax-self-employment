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
import models.connector.api_1895
import models.connector.api_1895.request

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
) {
  def isEmpty: Boolean = this == Deductions.empty

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
}

object Deductions {
  implicit lazy val deductionsTypeJsonFormat: Format[Deductions] = Json.format[Deductions]

  val empty: Deductions = Deductions(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
}
