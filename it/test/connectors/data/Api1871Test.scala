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
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1871Test extends CommonTestData {
  val downstreamUrl = s"/income-tax/income-sources/${asTys(testTaxYear)}/$testNino/$testBusinessId/self-employment/biss"

  val api1871ResponseJson: String =
    s"""{
      |   "incomeSourceId": "${testBusinessId.value}",
      |   "totalIncome": 200,
      |   "totalExpenses": 200,
      |   "netProfit": 200,
      |   "netLoss": 200,
      |   "totalAdditions": 200,
      |   "totalDeductions": 200,
      |   "accountingAdjustments": 200,
      |   "taxableProfit": 200,
      |   "taxableLoss": 200
      |}
      |""".stripMargin

  val api1871Response: BusinessIncomeSourcesSummaryResponse = Json.parse(api1871ResponseJson).as[BusinessIncomeSourcesSummaryResponse]
}
