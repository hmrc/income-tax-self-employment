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

package connectors.data

import models.common.TaxYear.{asTys, endDate, startDate}
import models.connector.api_1895.request._
import play.api.libs.json.{JsValue, Json}
import testdata.CommonTestData


trait Api1895Test extends CommonTestData {

  val downstreamSuccessResponse: String = Json.stringify(Json.obj("periodId" -> "someId"))

  val requestBody: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(Some(Incomes(Some(100.00), None, None)), None)

  val data: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(testTaxYear, testNino, testBusinessId, requestBody)

  val downstreamUrl =
    s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId.value}/periodic-summaries\\?from=${startDate(
      data.taxYear)}&to=${endDate(data.taxYear)}"

  val api1895RequestJson: JsValue = Json.toJson(
    AmendSEPeriodSummaryRequestBody(
      incomes =
        Some(Incomes(
          turnover = Some(100.00),
          other = None,
          taxTakenOffTradingIncome = None
        )),
      deductions =
    Some(Deductions(
      costOfGoods = None,
      constructionIndustryScheme = None,
      staffCosts = None,
      travelCosts = Some(SelfEmploymentDeductionsDetailType(
        amount = 200.00,
        disallowableAmount = Some(100.00),
      )),
      premisesRunningCosts = None,
      maintenanceCosts = None,
      adminCosts = None,
      businessEntertainmentCosts = None,
      advertisingCosts = None,
      interest = None,
      financialCharges = None,
      badDebt = None,
      professionalFees = None,
      depreciation = None,
      other = None,
      simplifiedExpenses = None
    )))

  )
}
