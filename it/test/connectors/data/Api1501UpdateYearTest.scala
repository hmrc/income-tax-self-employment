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

import models.connector.api_1500.{CreateBroughtForwardLossRequestBody, LossType, SuccessResponseSchema}
import models.connector.api_1501._
import play.api.libs.json.Json
import testdata.CommonTestData
import utils.BaseSpec.{businessId, nino, _}

trait Api1501UpdateYearTest extends CommonTestData {

  val taxYearStr: String = testTaxYear.toYYYY_YY
  val downstreamCreateUrl = s"/individuals/losses/$testNino/brought-forward-losses/$taxYearStr"
  val downstreamDeleteUrl = s"/individuals/losses/$testNino/brought-forward-losses/$businessId"

  val requestBody: CreateBroughtForwardLossRequestBody = CreateBroughtForwardLossRequestBody(
    businessId = testBusinessId.value,
    typeOfLoss = LossType.SelfEmployment,
    lossAmount = BigDecimal(250),
    taxYearBroughtForwardFrom = "2023-24"
  )

  val data: UpdateBroughtForwardLossYear = UpdateBroughtForwardLossYear(
    nino = nino,
    lossId = testBusinessId.value,
    taxYear = taxYear,
    body = requestBody
  )

  val api1171ResponseJson: String =
    s"""{
       |   "lossId": "${testBusinessId.value}"
       |}
       |""".stripMargin

  val api1171Response: SuccessResponseSchema = Json.parse(api1171ResponseJson).as[SuccessResponseSchema]
}
