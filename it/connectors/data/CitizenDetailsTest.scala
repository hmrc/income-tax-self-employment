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

import models.connector.citizen_details._
import play.api.libs.json.Json
import testdata.CommonTestData

trait CitizenDetailsTest extends CommonTestData {
  val downstreamUrl = s"/citizen-details/nino/$testNino"

  val successResponseRaw: String =
    s"""{
      |   "name": {
      |      "current": {
      |         "firstName": "Mike",
      |         "lastName": "Wazowski"
      |      },
      |      "previous": [
      |         {
      |            "firstName": "Jess",
      |            "lastName": "Smith"
      |         }
      |      ]
      |   },
      |   "ids": {
      |      "nino": "nino"
      |   },
      |   "dateOfBirth": "30071997"
      |}""".stripMargin

  val successResponse: SuccessResponseSchema = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
