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

package models.connector.api_1894.request

object DeductionsTestData {
  val sample: Deductions = Deductions(
    adminCosts = Some(SelfEmploymentDeductionsDetailType(Some(1.0), Some(2.0))),
    advertisingCosts = Some(SelfEmploymentDeductionsDetailType(Some(3.0), Some(4.0))),
    badDebt = Some(SelfEmploymentDeductionsDetailPosNegType(Some(5.0), Some(6.0))),
    constructionIndustryScheme = Some(SelfEmploymentDeductionsDetailType(Some(7.0), Some(8.0))),
    costOfGoods = Some(SelfEmploymentDeductionsDetailPosNegType(Some(9.0), Some(10.0))),
    depreciation = Some(SelfEmploymentDeductionsDetailPosNegType(Some(11.0), Some(12.0))),
    financialCharges = Some(SelfEmploymentDeductionsDetailPosNegType(Some(13.0), Some(14.0))),
    interest = Some(SelfEmploymentDeductionsDetailPosNegType(Some(15.0), Some(16.0))),
    maintenanceCosts = Some(SelfEmploymentDeductionsDetailPosNegType(Some(17.0), Some(18.0))),
    other = Some(SelfEmploymentDeductionsDetailType(Some(19.0), Some(20.0))),
    professionalFees = Some(SelfEmploymentDeductionsDetailAllowablePosNegType(Some(21.0), Some(22.0))),
    premisesRunningCosts = Some(SelfEmploymentDeductionsDetailPosNegType(Some(23.0), Some(24.0))),
    staffCosts = Some(SelfEmploymentDeductionsDetailType(Some(25.0), Some(26.0))),
    travelCosts = Some(SelfEmploymentDeductionsDetailType(Some(27.0), Some(28.0))),
    businessEntertainmentCosts = Some(SelfEmploymentDeductionsDetailType(Some(29.0), Some(30.0))),
    simplifiedExpenses = Some(31)
  )
}
