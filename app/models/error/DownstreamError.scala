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

import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json._

sealed trait DownstreamError extends ServiceError {
  val errorMessage: String = ""
  val status: Int
}

object DownstreamError {

  implicit val formats: Format[DownstreamError] =
    Format(
      Reads(jsValue => SingleDownstreamError.formats.reads(jsValue) orElse MultipleDownstreamErrors.formats.reads(jsValue)),
      Writes {
        case statusError: SingleDownstreamError    => SingleDownstreamError.formats.writes(statusError)
        case statusError: MultipleDownstreamErrors => MultipleDownstreamErrors.formats.writes(statusError)
      }
    )

  case class SingleDownstreamError(status: Int, body: SingleDownstreamErrorBody) extends DownstreamError {

    def toDomain: SingleDownstreamError = {
      val domainStatus = if (body.code == "INVALID_MTD_ID" || body.code == "INVALID_CORRELATIONID") INTERNAL_SERVER_ERROR else status
      this.copy(status = domainStatus, body = body.toDomain)
    }

  }

  object SingleDownstreamError {
    implicit val formats: OFormat[SingleDownstreamError] = Json.format[SingleDownstreamError]
  }

  case class MultipleDownstreamErrors(status: Int, body: MultipleDownstreamErrorBody) extends DownstreamError {

    def toDomain: MultipleDownstreamErrors =
      this.copy(body = body.copy(failures = body.failures.map(_.toDomain)))

  }

  object MultipleDownstreamErrors {
    implicit val formats: OFormat[MultipleDownstreamErrors] = Json.format[MultipleDownstreamErrors]
  }

}
