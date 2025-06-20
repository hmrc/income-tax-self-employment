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

import models.connector.api_1870._
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1870Test extends CommonTestData {
  val currentTaxYear: String = testTaxYear.toYYYY_YY
  val downstreamUrl          = s"/individuals/losses/$testNino/brought-forward-losses/tax-year/$currentTaxYear"

  val api1870ResponseJson: String =
    s"""{
       |  "losses": [
       |   {
       |      "lossId": "1234568790ABCDE",
       |      "businessId": "${testBusinessId.value}",
       |      "typeOfLoss": "self-employment",
       |      "lossAmount": 260,
       |      "taxYearBroughtForwardFrom": "2020-21",
       |      "lastModified": "2020-07-13T12:13:48.763Z"
       |   }
       |   ]
       |}
       |""".stripMargin

  val api1870Response: SuccessResponseSchema = Json.parse(api1870ResponseJson).as[SuccessResponseSchema]
}
