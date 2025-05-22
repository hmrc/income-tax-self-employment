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

import models.common.TaxYear.asTys
import models.connector.api_1786._
import play.api.libs.json.Json
import testdata.CommonTestData
import utils.BaseSpec.{taxYearEnd, taxYearStart}

trait Api1786Test extends CommonTestData {
  val downstreamUrl =
    s"/income-tax/${asTys(testTaxYear)}/$testNino/self-employments/$testBusinessId/periodic-summary-detail\\?from=$taxYearStart&to=$taxYearEnd"

  val successResponseRaw: String =
    """{
      |   "from": "2001-01-01",
      |   "to": "2001-01-01",
      |   "financials": {
      |      "deductions": {
      |         "adminCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "advertisingCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "badDebt": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "constructionIndustryScheme": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "costOfGoods": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "depreciation": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "financialCharges": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "interest": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "maintenanceCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "other": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "professionalFees": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "premisesRunningCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "staffCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "travelCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "businessEntertainmentCosts": {
      |            "amount": 200,
      |            "disallowableAmount": 200
      |         },
      |         "simplifiedExpenses": 666.66
      |      },
      |      "incomes": {
      |         "turnover": 200,
      |         "other": 200
      |      }
      |   }
      |}
      |""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
