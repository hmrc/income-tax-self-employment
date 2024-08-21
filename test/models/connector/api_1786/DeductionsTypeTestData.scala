/*
 * Copyright 2024 HM Revenue & Customs
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

object DeductionsTypeTestData {
  val sample: DeductionsType = DeductionsType(
    adminCosts = Some(SelfEmploymentDeductionsDetailType(1.0, Some(2.0))),
    advertisingCosts = Some(SelfEmploymentDeductionsDetailType(3.0, Some(4.0))),
    badDebt = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(5.0), Some(6.0))),
    constructionIndustryScheme = Some(SelfEmploymentDeductionsDetailType(7.0, Some(8.0))),
    costOfGoods = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(9.0), Some(10.0))),
    depreciation = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(11.0), Some(12.0))),
    financialCharges = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(13.0), Some(14.0))),
    interest = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(15.0), Some(16.0))),
    maintenanceCosts = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(17.0), Some(18.0))),
    other = Some(SelfEmploymentDeductionsDetailType(19.0, Some(20.0))),
    professionalFees = Some(SelfEmploymentDeductionsDetailTypeAllowableNegDisPos(21.0, Some(22.0))),
    premisesRunningCosts = Some(SelfEmploymentDeductionsDetailTypePosNeg(Some(23.0), Some(24.0))),
    staffCosts = Some(SelfEmploymentDeductionsDetailType(25.0, Some(26.0))),
    travelCosts = Some(SelfEmploymentDeductionsDetailType(27.0, Some(28.0))),
    businessEntertainmentCosts = Some(SelfEmploymentDeductionsDetailType(29.0, Some(30.0))),
    simplifiedExpenses = Some(31)
  )
}
