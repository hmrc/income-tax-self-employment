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

package models.error

import play.api.libs.json._

trait ErrorType {
  val errorCode: String
}

object ErrorType {
  case object DownstreamErrorCode extends ErrorType {
    val errorCode = "DOWNSTREAM_ERROR_CODE"
  }

  case object DomainErrorCode extends ErrorType {
    val errorCode = "DOMAIN_ERROR_CODE"
  }

  implicit val formats: Format[ErrorType] =
    Format(
      Reads {
        case JsString("DOWNSTREAM_ERROR_CODE") => JsSuccess(DownstreamErrorCode)
        case JsString("DOMAIN_ERROR_CODE")     => JsSuccess(DomainErrorCode)
        case jsValue: JsValue                  => JsError(s"ErrorType $jsValue is not one of supported [DOWNSTREAM_ERROR_CODE, DOMAIN_ERROR_CODE]")
      },
      Writes { errType: ErrorType => JsString(errType.errorCode) }
    )
}
