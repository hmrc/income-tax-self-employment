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

package models.connector.api_1505

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, JsValue, Json}

class ClaimIdSpec extends AnyWordSpec with Matchers {

  "CreateLossClaimSuccessResponse" when {

    "converting to and from JSON" should {

      "successfully convert to JSON" in {
        val response      = ClaimId("claimId123")
        val json: JsValue = Json.toJson(response)

        (json \ "claimId").as[String] shouldBe "claimId123"
      }

      "successfully convert from JSON" in {
        val json: JsValue = Json.parse("""
            |{
            |  "claimId": "claimId123"
            |}
            |""".stripMargin)

        val response = json.as[ClaimId]

        response.claimId shouldBe "claimId123"
      }
    }

    "handling invalid JSON" should {

      "fail to convert from JSON with missing fields" in {
        val json: JsValue = Json.parse("""
            |{
            |}
            |""".stripMargin)

        intercept[JsResultException] {
          json.as[ClaimId]
        }
      }

      "fail to convert from JSON with incorrect field types" in {
        val json: JsValue = Json.parse("""
            |{
            |  "value": 123
            |}
            |""".stripMargin)

        intercept[JsResultException] {
          json.as[ClaimId]
        }
      }

      "ignore additional unexpected fields when reading from JSON" in {
        val json: JsValue = Json.parse("""
            |{
            |  "claimId": "claimId123",
            |  "unexpectedField": "unexpectedValue"
            |}
            |""".stripMargin)

        val request = json.as[ClaimId]

        request.claimId shouldBe "claimId123"
      }

    }
  }
}
