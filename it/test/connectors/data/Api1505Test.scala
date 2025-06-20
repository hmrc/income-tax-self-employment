/*
 * Copyright 2025 HM Revenue & Customs
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

import models.connector.api_1505.{ClaimId, CreateLossClaimRequestBody, CreateLossClaimRequestBodyHip}
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1505Test extends CommonTestData {

  val downstreamUrl: String = s"/income-tax/claims-for-relief/$testNino"

  val api1171ResponseJson: String =
    s"""{
       |   "claimId": "1234568790ABCDE"
       |}
       |""".stripMargin

  val claimId = "1234568790ABCDE"

  val requestBody: CreateLossClaimRequestBody = CreateLossClaimRequestBody(
    incomeSourceId = "012345678912345",
    reliefClaimed = "CF",
    taxYear = "2020"
  )

  val hipRequestBody: CreateLossClaimRequestBodyHip = CreateLossClaimRequestBodyHip(
    incomeSourceId = "SJPR05893938418",
    reliefClaimed = "CF",
    taxYearClaimedFor = 2024
  )

  val badRequestResponseRaw: String =
    """
  {
    "code": "BAD_REQUEST",
    "message": "The request was invalid."
  }
  """

  val api1171Response: ClaimId = Json.parse(api1171ResponseJson).as[ClaimId]
}
