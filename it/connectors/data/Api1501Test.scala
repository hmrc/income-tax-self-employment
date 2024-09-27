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
import utils.BaseSpec._

trait Api1501Test {
  val taxableEntityId = nino
  val lossId          = businessId
  val downstreamUrl   = s"/income-tax/brought-forward-losses/$taxableEntityId/$lossId"
  val requestBody = UpdateBroughtForwardLossRequestBody(
    updatedBroughtForwardLossAmount = BigDecimal(260)
  )
  val data = UpdateBroughtForwardLossRequestData(
    taxableEntityId = taxableEntityId,
    lossId = lossId,
    body = requestBody
  )

  val successResponseRaw: String =
    s"""{
      |   "incomeSourceId": "12345678912345",
      |   "lossType": "INCOME",
      |   "broughtForwardLossAmount": 260,
      |   "taxYear": "2020",
      |   "lossId": "123",
      |   "submissionDate": "2020-07-13T12:13:48.763Z"
      |}
      |""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}