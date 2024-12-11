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

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Reads, Writes}

sealed trait IncomeSourceType {
  val value: String
}

case object UkProperty extends IncomeSourceType {
  val value = "02"
}

case object ForeignProperty extends IncomeSourceType {
  val value = "15"
}

object IncomeSourceType {

  implicit val format: Format[IncomeSourceType] = Format(
    {
      case JsString(UkProperty.value)      => JsSuccess(UkProperty)
      case JsString(ForeignProperty.value) => JsSuccess(ForeignProperty)
      case invalid                         => JsError(s"Invalid income source type: $invalid")
    },
    incomeSourceType => JsString(incomeSourceType.value)
  )

}
