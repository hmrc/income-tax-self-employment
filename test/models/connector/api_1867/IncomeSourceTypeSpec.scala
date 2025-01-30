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

import models.connector.common._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class IncomeSourceTypeSpec extends PlaySpec {

  "IncomeSourceType" should {

    "write UkProperty to JSON" in {
      val ukProperty: IncomeSourceType = UkProperty
      val json                         = Json.toJson(ukProperty)
      json mustBe JsString("02")
    }

    "write ForeignProperty to JSON" in {
      val foreignProperty: IncomeSourceType = ForeignProperty
      val json                              = Json.toJson(foreignProperty)
      json mustBe JsString("15")
    }

    "read JSON to UkProperty" in {
      val json   = JsString("02")
      val result = json.validate[IncomeSourceType]
      result mustBe JsSuccess(UkProperty)
    }

    "read JSON to ForeignProperty" in {
      val json   = JsString("15")
      val result = json.validate[IncomeSourceType]
      result mustBe JsSuccess(ForeignProperty)
    }

    "fail to read invalid JSON" in {
      val json   = JsString("invalid")
      val result = json.validate[IncomeSourceType]
      result mustBe a[JsError]
    }
  }
}
