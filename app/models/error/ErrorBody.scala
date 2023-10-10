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

import models.error.ErrorType.{DOWNSTREAM_ERROR_CODE, MDTP_ERROR_CODE}
import play.api.libs.json._


trait ErrorBody extends ServiceError {
  lazy val msg: String = reason

  def reason: String
}

object ErrorBody {
  
  implicit val errorBodyFormat: Format[ErrorBody] = Format(
    Reads { jsValue =>
      ApiErrorBody.apiErrorBodyFormat.reads(jsValue) orElse ApiErrorsBody.apiErrorsBodyFormat.reads(jsValue)
    },
    Writes {
      case ape: ApiErrorBody => ApiErrorBody.apiErrorBodyFormat.writes(ape)
      case ape: ApiErrorsBody => ApiErrorsBody.apiErrorsBodyFormat.writes(ape)
    }
  )

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
    implicit val apiErrorBodyFormat: OFormat[ApiErrorBody] = Json.format[ApiErrorBody]

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
    implicit val apiErrorsBodyFormat: OFormat[ApiErrorsBody] = Json.format[ApiErrorsBody]
  }
}
