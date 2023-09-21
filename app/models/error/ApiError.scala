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

import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, JsValue, Json, OFormat, Reads, Writes}

object ApiError {
  trait ErrorBody extends ServiceError {
    lazy val msg: String = reason
    def reason: String
  }

  trait StatusError {
    def status: Int
    def body: ErrorBody
  }

  trait ErrorType {
    def str: String
  }

  /** Single API Error * */
  case class ApiErrorBody(code: String, reason: String, errorType: ErrorType = DOWNSTREAM_ERROR_CODE) extends ErrorBody {
    def toMdtpError: ApiErrorBody =
      if (errorType == MDTP_ERROR_CODE) {
        this
      }
      else {
        val mdtpCode = code match {
          case "INVALID_NINO" => "FORMAT_NINO"
          case "UNMATCHED_STUB_ERROR" => "RULE_INCORRECT_GOV_TEST_SCENARIO"
          case "NOT_FOUND" => "MATCHING_RESOURCE_NOT_FOUND"
          case _ => "INTERNAL_SERVER_ERROR"
        }
        ApiErrorBody(mdtpCode, reason, errorType = MDTP_ERROR_CODE)
      }
  }

  object ApiErrorBody {
    implicit val formats: OFormat[ApiErrorBody] = Json.format[ApiErrorBody]
    val parsingError: ApiErrorBody = ApiErrorBody("PARSING_ERROR", "Error parsing response from API")

    val nino400: ApiErrorBody = ApiErrorBody(
      "INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")

    val correlationI400: ApiErrorBody = ApiErrorBody(
      "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header CorrelationId.")

    val mtdId400: ApiErrorBody = ApiErrorBody(
      "INVALID_MTDID", "Submission has not passed validation. Invalid parameter MTDID.")

    val data404: ApiErrorBody = ApiErrorBody(
      "NOT_FOUND", "The remote endpoint has indicated that no data can be found.")

    val ifsServer500: ApiErrorBody = ApiErrorBody(
      "SERVER_ERROR", "IF is currently experiencing problems that require live service intervention.")

    val service503: ApiErrorBody = ApiErrorBody(
      "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
  }

  /** Multiple API Errors * */
  case class ApiErrorsBody(failures: Seq[ApiErrorBody], reason: String = "") extends ErrorBody

  object ApiErrorsBody {
    implicit val formats: OFormat[ApiErrorsBody] = Json.format[ApiErrorsBody]
  }

  case class ApiStatusError(status: Int, body: ApiErrorBody) extends StatusError {
    def toMdtpError: ApiStatusError = {
      val mdtpStatus = if (body.code == "INVALID_MTD_ID" || body.code == "NVALID_CORRELATIONID") INTERNAL_SERVER_ERROR else status
      this.copy(status = mdtpStatus, body = body.toMdtpError)
    }
  }

  object ApiStatusError {
    implicit val apiStatusErrorFormats: OFormat[ApiStatusError] = Json.format[ApiStatusError]
  }

  case class ApiStatusErrors(status: Int, body: ApiErrorsBody) extends StatusError {
    def toMdtpError: ApiStatusErrors =
      this.copy(body = body.copy(failures = body.failures.map(_.toMdtpError)))
  }

  object ApiStatusErrors {
    implicit val apiStatusErrorsFormats: OFormat[ApiStatusErrors] = Json.format[ApiStatusErrors]
  }

  case object DOWNSTREAM_ERROR_CODE extends ErrorType {
    val str = "DOWNSTREAM_ERROR_CODE"
  }

  case object MDTP_ERROR_CODE extends ErrorType {
    override val str = "MDTP_ERROR_CODE"
  }

  implicit val errorTypeFormat: Format[ErrorType] = {
    Format(
      Reads {
        case JsString("DOWNSTREAM_ERROR_CODE") => JsSuccess(DOWNSTREAM_ERROR_CODE)
        case JsString("MDTP_ERROR_CODE") => JsSuccess(MDTP_ERROR_CODE)
        case jsValue: JsValue => JsError(s"ErrorType $jsValue is not one of supported [DOWNSTREAM_ERROR_CODE, MDTP_ERROR_CODE]")
      },
      Writes { errType: ErrorType => JsString(errType.str) }
    )
  }
}


