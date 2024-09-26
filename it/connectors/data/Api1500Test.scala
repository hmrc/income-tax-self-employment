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

import models.connector.api_1500._
import play.api.libs.json.Json
import utils.BaseSpec._

trait Api1500Test {
  val taxableEntityId = nino
  val downstreamUrl   = s"/income-tax/brought-forward-losses/$taxableEntityId"
  val requestBody = CreateBroughtForwardLossRequestBody(
    incomeSourceId = "SJPR05893938418",
    lossType = LossType.Income,
    broughtForwardLossAmount = BigDecimal(250),
    taxYearBroughtForwardFrom = 2024
  )
  val data = CreateBroughtForwardLossRequestData(
    taxableEntityId = taxableEntityId,
    body = requestBody
  )

  val successResponseRaw: String =
    s"""{
      |   "lossId": "123"
      |}
      |""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
