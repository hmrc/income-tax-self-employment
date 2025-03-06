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

package models.connector.api_1867

import models.connector.ReliefClaimType
import models.connector.ReliefClaimType.{CF, CSGI}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class ReliefClaimTypeSpec extends PlaySpec {

  "ReliefClaimType" when {

    "handling valid JSON" should {

      "write and read CarryForward correctly" in {
        val json = Json.toJson(CF: ReliefClaimType)
        json mustBe JsString("CF")
        json.validate[ReliefClaimType] mustBe JsSuccess(CF)
      }

      "write and read CarrySideways correctly" in {
        val json = Json.toJson(CSGI: ReliefClaimType)
        json mustBe JsString("CSGI")
        json.validate[ReliefClaimType] mustBe JsSuccess(CSGI)
      }

      "return an error for invalid ReliefClaimType" in {
        val json = JsString("INVALID")
        json.validate[ReliefClaimType] mustBe a[JsError]
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
