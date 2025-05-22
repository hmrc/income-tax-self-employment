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
import testdata.CommonTestData

trait Api1500Test extends CommonTestData {
  val taxYearStr: String = testTaxYear.toYYYY_YY
  val downstreamUrl      = s"/individuals/losses/$testNino/brought-forward-losses/$taxYearStr"

  val requestBody: CreateBroughtForwardLossRequestBody = CreateBroughtForwardLossRequestBody(
    businessId = "SJPR05893938418",
    typeOfLoss = LossType.SelfEmployment,
    lossAmount = BigDecimal(250),
    taxYearBroughtForwardFrom = "2023-24"
  )

  val data: CreateBroughtForwardLossRequestData = CreateBroughtForwardLossRequestData(
    nino = testNino,
    taxYear = testTaxYear,
    body = requestBody
  )

  val successResponseRaw: String =
    s"""{
      |   "lossId": "1234568790ABCDE"
      |}
      |""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
