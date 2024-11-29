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

package models.connector.api_1867

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class ReliefClaimTypeSpec extends PlaySpec {

  "ReliefClaimType" when {

    "handling valid JSON" should {

      "write and read CarryForward correctly" in {
        val json = Json.toJson(CarryForward: ReliefClaimType)
        json mustBe JsString("CF")
        json.validate[ReliefClaimType] mustBe JsSuccess(CarryForward)
      }

      "write and read CarrySideways correctly" in {
        val json = Json.toJson(CarrySideways: ReliefClaimType)
        json mustBe JsString("CSGI")
        json.validate[ReliefClaimType] mustBe JsSuccess(CarrySideways)
      }

      "write and read CarryForwardsToCarrySideways correctly" in {
        val json = Json.toJson(CarryForwardsToCarrySideways: ReliefClaimType)
        json mustBe JsString("CFCSGI")
        json.validate[ReliefClaimType] mustBe JsSuccess(CarryForwardsToCarrySideways)
      }

      "write and read CarrySidewaysFHL correctly" in {
        val json = Json.toJson(CarrySidewaysFHL: ReliefClaimType)
        json mustBe JsString("CSFHL")
        json.validate[ReliefClaimType] mustBe JsSuccess(CarrySidewaysFHL)
      }

      "return an error for invalid ReliefClaimType" in {
        val json = JsString("INVALID")
        json.validate[ReliefClaimType] mustBe a[JsError]
      }

      "identify CarryForward as SelfEmploymentClaim" in {
        CarryForward mustBe a[SelfEmploymentClaim]
      }

      "identify CarrySideways as SelfEmploymentClaim" in {
        CarrySideways mustBe a[SelfEmploymentClaim]
      }

      "identify CarryForwardsToCarrySideways as PropertyClaim" in {
        CarryForwardsToCarrySideways mustBe a[PropertyClaim]
      }

      "identify CarrySidewaysFHL as PropertyClaim" in {
        CarrySidewaysFHL mustBe a[PropertyClaim]
      }
    }

    "handling invalid json" should {

      "fail to read an invalid ReliefClaimType" in {
        val json = JsString("INVALID")
        json.validate[ReliefClaimType] mustBe a[JsError]
      }

      "fail to read an empty string as ReliefClaimType" in {
        val json = JsString("")
        json.validate[ReliefClaimType] mustBe a[JsError]
      }

      "fail to read a non-string JSON value as ReliefClaimType" in {
        val json = JsNumber(123)
        json.validate[ReliefClaimType] mustBe a[JsError]
      }
    }
  }
}