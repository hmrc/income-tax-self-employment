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
    adminCosts = Option(SelfEmploymentDeductionsDetailType(Some(1.0), Some(2.0))),
    advertisingCosts = Option(SelfEmploymentDeductionsDetailType(Some(3.0), Some(4.0))),
    badDebt = Option(SelfEmploymentDeductionsDetailPosNegType(Some(5.0), Some(6.0))),
    constructionIndustryScheme = Option(SelfEmploymentDeductionsDetailType(Some(7.0), Some(8.0))),
    costOfGoods = Option(SelfEmploymentDeductionsDetailPosNegType(Some(9.0), Some(10.0))),
    depreciation = Option(SelfEmploymentDeductionsDetailPosNegType(Some(11.0), Some(12.0))),
    financialCharges = Option(SelfEmploymentDeductionsDetailPosNegType(Some(13.0), Some(14.0))),
    interest = Option(SelfEmploymentDeductionsDetailPosNegType(Some(15.0), Some(16.0))),
    maintenanceCosts = Option(SelfEmploymentDeductionsDetailPosNegType(Some(17.0), Some(18.0))),
    other = Option(SelfEmploymentDeductionsDetailType(Option(19.0), Option(20.0))),
    professionalFees = Option(SelfEmploymentDeductionsDetailAllowablePosNegType(Some(21.0), Some(22.0))),
    premisesRunningCosts = Option(SelfEmploymentDeductionsDetailPosNegType(Some(23.0), Some(24.0))),
    staffCosts = Option(SelfEmploymentDeductionsDetailType(Some(25.0), Some(26.0))),
    travelCosts = Option(SelfEmploymentDeductionsDetailType(Some(27.0), Some(28.0))),
    businessEntertainmentCosts = Option(SelfEmploymentDeductionsDetailType(Some(29.0), Some(30.0))),
    simplifiedExpenses = Option(31)
  )
}
