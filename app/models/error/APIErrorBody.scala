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

import play.api.libs.json.{JsValue, Json, OFormat}

sealed trait APIErrorBody

object APIErrorBody {
  case class APIStatusError(status: Int, body: APIErrorBody) {
    def toJson: JsValue = {
      body match {
        case error: APIError => Json.toJson(error)
        case errors: APIErrors => Json.toJson(errors)
      }
    }
  }

  /** Single API Error * */
  case class APIError(code: String, reason: String) extends APIErrorBody

  object APIError {
    implicit val formats: OFormat[APIError] = Json.format[APIError]
    val parsingError: APIError = APIError("PARSING_ERROR", "Error parsing response from API")
    
    val nino400: APIError = APIError(
      "INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
    
    val correlationI400: APIError = APIError(
      "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header CorrelationId.")
    
    val mtdId400: APIError = APIError(
      "INVALID_MTDID",  "Submission has not passed validation. Invalid parameter MTDID.")
    
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

