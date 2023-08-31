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

import models.error.APIErrorBody.ErrorType.{DOWNSTREAM_ERROR_CODE, MDTP_ERROR_CODE}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{Format, JsError, JsString, JsSuccess, JsValue, Json, OFormat, Reads, Writes}

sealed trait APIErrorBody

object APIErrorBody {
  case class APIStatusError(status: Int, body: APIErrorBody) {
    def toJson: JsValue = {
      body match {
        case error: APIError => Json.toJson(error)
        case errors: APIErrors => Json.toJson(errors)
      }
    }

    def toMdtpError: APIStatusError = {
      val (errorBody, errorStatus) = body match {
        case apiError: APIError =>
          val mdtpStatus = if (apiError.code == "INVALID_MTD_ID" || apiError.code == "NVALID_CORRELATIONID") INTERNAL_SERVER_ERROR else status
          (apiError.toMdtpError, mdtpStatus)
        case apiErrors: APIErrors =>
          (apiErrors.copy(failures = apiErrors.failures.map(_.toMdtpError)), status)
      }
      this.copy(status = errorStatus, body = errorBody)
    }
  }

  trait ErrorType {
    def str: String
  }

  object ErrorType {
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

  /** Single API Error * */
  case class APIError(code: String, reason: String, errorType: ErrorType = DOWNSTREAM_ERROR_CODE) extends APIErrorBody {
    def toMdtpError: APIError =
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
        APIError(mdtpCode, reason, errorType = MDTP_ERROR_CODE)
      }
  }

  object APIError {
    implicit val formats: OFormat[APIError] = Json.format[APIError]
    val parsingError: APIError = APIError("PARSING_ERROR", "Error parsing response from API")

    val nino400: APIError = APIError(
      "INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")

    val correlationI400: APIError = APIError(
      "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header CorrelationId.")

    val mtdId400: APIError = APIError(
      "INVALID_MTDID", "Submission has not passed validation. Invalid parameter MTDID.")

    val data404: APIError = APIError(
      "NOT_FOUND", "The remote endpoint has indicated that no data can be found.")

    val desServer500: APIError = APIError(
      "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")

    val service503: APIError = APIError(
      "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
  }

  /** Multiple API Errors * */
  case class APIErrors(failures: Seq[APIError]) extends APIErrorBody

  object APIErrors {
    implicit val formats: OFormat[APIErrors] = Json.format[APIErrors]
  }

}

