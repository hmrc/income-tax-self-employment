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

package models.connector.api_1505

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsResultException, JsValue, Json}

class CreateLossClaimRequestBodySpec extends AnyWordSpec with Matchers {

  "CreateLossClaimRequestBody" when {

    "handling valid JSON" should {

      "write to JSON" in {
        val request       = CreateLossClaimRequestBody("sourceId123", "reliefType", 2023)
        val json: JsValue = Json.toJson(request)

        (json \ "incomeSourceId").as[String] shouldBe "sourceId123"
        (json \ "reliefClaimed").as[String] shouldBe "reliefType"
        (json \ "taxYear").as[Int] shouldBe 2023
      }

      "read from JSON" in {
        val json: JsValue = Json.parse("""
            |{
            |  "incomeSourceId": "sourceId123",
            |  "reliefClaimed": "reliefType",
            |  "taxYear": 2023
            |}
            |""".stripMargin)

        val request = json.as[CreateLossClaimRequestBody]

        request.incomeSourceId shouldBe "sourceId123"
        request.reliefClaimed shouldBe "reliefType"
        request.taxYear shouldBe 2023
      }
    }

    "handling invalid JSON" should {

      "fail to write from JSON with missing fields" in {
        val json: JsValue = Json.parse("""
            |{
            |  "incomeSourceId": "sourceId123",
            |  "reliefClaimed": "reliefType"
            |}
            |""".stripMargin)

        intercept[JsResultException] {
          json.as[CreateLossClaimRequestBody]
        }
      }

      "throw an exception when accessing a non-existent field" in {
        val request       = CreateLossClaimRequestBody("sourceId123", "reliefType", 2023)
        val json: JsValue = Json.toJson(request)

        intercept[JsResultException] {
          (json \ "taxYear22").as[String]
        }
      }

      "fail to read from JSON with incorrect field types" in {
        val json: JsValue = Json.parse("""
            |{
            |  "incomeSourceId": "sourceId123",
            |  "reliefClaimed": "reliefType",
            |  "taxYear": "notAnInt"
            |}
            |""".stripMargin)

        intercept[JsResultException] {
          json.as[CreateLossClaimRequestBody]
        }
      }

      "ignore additional unexpected fields when reading from JSON" in {
        val json: JsValue = Json.parse("""
            |{
            |  "incomeSourceId": "sourceId123",
            |  "reliefClaimed": "reliefType",
            |  "taxYear": 2023,
            |  "unexpectedField": "unexpectedValue"
            |}
            |""".stripMargin)

        val request = json.as[CreateLossClaimRequestBody]

        request.incomeSourceId shouldBe "sourceId123"
        request.reliefClaimed shouldBe "reliefType"
        request.taxYear shouldBe 2023
      }
    }
  }

}
