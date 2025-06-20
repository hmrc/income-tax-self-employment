/*
 * Copyright 2025 HM Revenue & Customs
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

import data.CommonTestData


object AmendSEPeriodSummaryRequestBodyTestData extends CommonTestData {

  val sample: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(
    incomes = Some(Incomes(Some(1.0), Some(2.0), Some(3.0))),
    deductions = Some(DeductionsTestData.sample)
  )

  val deductionsSample: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(
    incomes = None,
    deductions = Some(DeductionsTestData.sample)
  )

  val dataSample: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(
    testTaxYear,
    testNino,
    testBusinessId,
    sample
  )

}
