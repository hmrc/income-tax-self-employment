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
  val lossId        = "1234568790ABCDE"
  val downstreamUrl = s"/individuals/losses/$nino/brought-forward-losses/$lossId/change-loss-amount"
  val requestBody = UpdateBroughtForwardLossRequestBody(
    lossAmount = BigDecimal(260)
  )
  val data = UpdateBroughtForwardLossRequestData(
    nino = nino,
    lossId = lossId,
    body = requestBody
  )

  val successResponseRaw: String =
    s"""{
      |  "businessId": "12345678912345",
      |  "typeOfLoss": "self-employment",
      |  "lossAmount": 260,
      |  "taxYearBroughtForwardFrom": "2020-21",
      |  "lastModified": "2022-11-05T11:56:27Z"
      |}
      |""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
