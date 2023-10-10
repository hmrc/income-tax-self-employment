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

import models.error.ErrorBody.{ApiErrorBody, ApiErrorsBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json._

trait StatusError extends ServiceError {
  val msg: String = ""

  def status: Int
  def toMdtpError: StatusError
  def body: ErrorBody
}

object StatusError {
  implicit val statusErrorFormat: Format[StatusError] =
    Format(
      Reads(jsValue =>
        ApiStatusError.apiStatusErrorFormat.reads(jsValue) orElse ApiStatusErrors.apiStatusErrorsFormat.reads(jsValue)
      ),
      Writes({
        case statusError: ApiStatusError => ApiStatusError.apiStatusErrorFormat.writes(statusError)
        case statusError: ApiStatusErrors => ApiStatusErrors.apiStatusErrorsFormat.writes(statusError)
      })
    )

  case class ApiStatusError(status: Int, body: ApiErrorBody) extends StatusError {
    def toMdtpError: ApiStatusError = {
      val mdtpStatus = if (body.code == "INVALID_MTD_ID" || body.code == "NVALID_CORRELATIONID") INTERNAL_SERVER_ERROR else status
      this.copy(status = mdtpStatus, body = body.toMdtpError)
    }
  }

  object ApiStatusError {
    implicit val apiStatusErrorFormat: OFormat[ApiStatusError] = Json.format[ApiStatusError]
  }

  case class ApiStatusErrors(status: Int, body: ApiErrorsBody) extends StatusError {
    def toMdtpError: ApiStatusErrors =
      this.copy(body = body.copy(failures = body.failures.map(_.toMdtpError)))
  }

  object ApiStatusErrors {
    implicit val apiStatusErrorsFormat: OFormat[ApiStatusErrors] = Json.format[ApiStatusErrors]
  }
}
