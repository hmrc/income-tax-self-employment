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

package models.connector.api_1895.request

import models.connector.api_1786.DeductionsType
import play.api.libs.json.{Format, Json}

case class Deductions(costOfGoods: Option[SelfEmploymentDeductionsDetailPosNegType],
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
                      simplifiedExpenses: Option[BigDecimal])

object Deductions {
  implicit lazy val deductionsTypeJsonFormat: Format[Deductions] = Json.format[Deductions]

  def fromApi1786(source: DeductionsType): Deductions =
    Deductions(
      costOfGoods = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.costOfGoods),
      constructionIndustryScheme = SelfEmploymentDeductionsDetailType.fromApi1786(source.constructionIndustryScheme),
      staffCosts = SelfEmploymentDeductionsDetailType.fromApi1786(source.staffCosts),
      travelCosts = SelfEmploymentDeductionsDetailType.fromApi1786(source.travelCosts),
      premisesRunningCosts = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.premisesRunningCosts),
      maintenanceCosts = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.maintenanceCosts),
      adminCosts = SelfEmploymentDeductionsDetailType.fromApi1786(source.adminCosts),
      businessEntertainmentCosts = SelfEmploymentDeductionsDetailType.fromApi1786(source.businessEntertainmentCosts),
      advertisingCosts = SelfEmploymentDeductionsDetailType.fromApi1786(source.advertisingCosts),
      interest = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.interest),
      financialCharges = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.financialCharges),
      badDebt = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.badDebt),
      professionalFees = SelfEmploymentDeductionsDetailAllowablePosNegType.fromApi1786(source.professionalFees),
      depreciation = SelfEmploymentDeductionsDetailPosNegType.fromApi1786(source.depreciation),
      other = SelfEmploymentDeductionsDetailType.fromApi1786(source.other),
      simplifiedExpenses = source.simplifiedExpenses
    )
}
