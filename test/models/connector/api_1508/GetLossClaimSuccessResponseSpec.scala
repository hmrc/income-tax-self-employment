/*
 * Copyright 2024 HM Revenue & Customs
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

package models.connector.api_1508

import data.TimeData
import models.connector.ClaimId
import models.connector.ReliefClaimType.CF
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, JsValue, Json}

import java.time.format.DateTimeFormatter

class GetLossClaimSuccessResponseSpec extends AnyWordSpec with Matchers with TimeData {


  "GetLossClaimSuccessResponse" must {

    val lossClaimSuccessResponse = GetLossClaimSuccessResponse(
      incomeSourceId = "012345678912345",
      reliefClaimed = CF,
      claimId = ClaimId("AAZZ1234567890A"),
      sequence = Option(2),
      submissionDate = testDateTime
    )

    "serialise to JSON" in {
      val expectedJson = Json.parse(s"""{
                           | "incomeSourceId": "012345678912345",
                           |"reliefClaimed": "CF",
                           |"claimId": "AAZZ1234567890A",
                           |"sequence": 2,
                           |"submissionDate": "${testDateTime.format(DateTimeFormatter.ISO_DATE_TIME)}"
                           |}
                           |""".stripMargin)
      Json.toJson(lossClaimSuccessResponse) shouldBe expectedJson
    }

    "de-serialise to GetLossClaimSuccessResponse" in {
      val json = Json.parse(s"""{
                               | "incomeSourceId": "012345678912345",
                               |"reliefClaimed": "CF",
                               |"taxYearClaimedFor": "2020",
                               |"claimId": "AAZZ1234567890A",
                               |"sequence": 2,
                               |"submissionDate": "${testDateTime.toString}"
                               |}
                               |""".stripMargin)

      json.as[GetLossClaimSuccessResponse] shouldBe lossClaimSuccessResponse
    }

    "throw exception on failing to read as json" in {
      val json: JsValue = Json.parse("""
                                       |{
                                       |}
                                       |""".stripMargin)

      intercept[JsResultException] {
        json.as[GetLossClaimSuccessResponse]
      }
    }

    "throw exception for an invalid json" in {
      val json = Json.parse(s"""{
                             | "incomeSourceId": "012345678912345",
                             |"reliefClaimed": "CF",
                             |"claimId": "AAZZ1234567890A",
                             |"sequence": "2",
                             |"submissionDate": "${testDateTime.toString}"
                             |}
                             |""".stripMargin)

      intercept[JsResultException] {
        json.as[GetLossClaimSuccessResponse]
      }
    }
  }
}
