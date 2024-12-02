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

import models.connector.api_2085.ListOfIncomeSources
import play.api.libs.json.{JsObject, Json}
import utils.BaseSpec._

trait Api2085Test {
  val downstreamUrl = s"/income-tax/income-sources/$nino"

  val successResponseRaw: String =
    s"""{
       |  "selfEmployments": [
       |    {
       |      "incomeSourceId": "FHL000000000100",
       |      "incomeSourceName": "UK Property",
       |      "commencementDate": "2018-01-01",
       |      "accountingPeriodStartDate": "2024-04-06",
       |      "accountingPeriodEndDate": "2025-04-05",
       |      "accountingType": "CASH",
       |      "latency": {
       |        "latencyEndDate": "2025-04-05",
       |        "taxYear1": "23-24",
       |        "optedOutOfLatencyYear1": true,
       |        "taxYear2": "24-25",
       |        "optedOutOfLatencyYear2": true
       |      },
       |      "incomeSourceType": "02"
       |    }]
       |}
       |""".stripMargin

  val failedResponse: String = """{
      | "code": "INVALID_TAXABLE_ENTITY_ID"
      | "message":"Submission has not passed validation. Invalid parameter taxableEntityId."
      |""".stripMargin

  val successResponse: ListOfIncomeSources = Json.parse(successResponseRaw).as[ListOfIncomeSources]
}
