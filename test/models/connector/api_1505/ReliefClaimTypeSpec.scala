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
import play.api.libs.json._

class ReliefClaimTypeSpec extends AnyWordSpec with Matchers {

  "ReliefClaimType" when {

    "converting to and from JSON" should {

      "successfully convert to JSON" in {
        val reliefClaimType: ReliefClaimType = ReliefClaimType.CF
        val json: JsValue                    = Json.toJson(reliefClaimType)

        json.as[String] shouldBe "CF"
      }

      "successfully convert from JSON" in {
        val json: JsValue = Json.parse("""
                                         |"CF"
                                         |""".stripMargin)

        val reliefClaimType = json.as[ReliefClaimType]

        reliefClaimType shouldBe ReliefClaimType.CF
      }
    }

    "handling invalid JSON" should {

      "fail to convert from JSON with incorrect values" in {
        val json: JsValue = Json.parse("""
                                         |"invalid"
                                         |""".stripMargin)

        intercept[JsResultException] {
          json.as[ReliefClaimType]
        }
      }

      "ignore additional unexpected fields when reading from JSON" in {
        val json: JsValue = Json.parse("""
                                         |{
                                         |  "type": "CF",
                                         |  "unexpectedField": "unexpectedValue"
                                         |}
                                         |""".stripMargin)

        val reliefClaimType = (json \ "type").as[ReliefClaimType]

        reliefClaimType shouldBe ReliefClaimType.CF
      }
    }
  }
}
