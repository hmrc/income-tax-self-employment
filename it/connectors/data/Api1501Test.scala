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

import models.connector.api_1501._
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1501Test extends CommonTestData {

  val downstreamUrl = s"/individuals/losses/$testNino/brought-forward-losses/$testBusinessId/change-loss-amount"

  val requestBody = UpdateBroughtForwardLossRequestBody(
    lossAmount = BigDecimal(260)
  )

  val data = UpdateBroughtForwardLossRequestData(
    nino = testNino,
    lossId = testBusinessId.value,
    body = requestBody
  )

  val api1501ResponseJson: String =
    s"""{
      |  "businessId": "${testBusinessId.value}",
      |  "typeOfLoss": "self-employment",
      |  "lossAmount": 260,
      |  "taxYearBroughtForwardFrom": "2020-21",
      |  "lastModified": "2022-11-05T11:56:27Z"
      |}
      |""".stripMargin

  val api1501Response: SuccessResponseSchema = Json.parse(api1501ResponseJson).as[SuccessResponseSchema]
}
