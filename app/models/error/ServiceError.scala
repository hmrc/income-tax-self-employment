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
import models.error.ErrorType.{DOWNSTREAM_ERROR_CODE, MDTP_ERROR_CODE}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json._

sealed trait ServiceError {
  def msg: String
}

object ServiceError {
  trait DatabaseError extends ServiceError

  object DatabaseError {

    case object DataNotUpdated extends DatabaseError {
      val msg: String = "User data was not updated due to mongo exception"
    }

    case object DataNotFound extends DatabaseError {
      val msg: String = "User data could not be found due to mongo exception"
    }

    case class MongoError(error: String) extends DatabaseError {
      val msg: String = s"Mongo exception occurred. Exception: $error"
    }

    object MongoError {

      implicit val mongoErrorFormat: Format[MongoError] =
        Format(
          Reads {
            case JsString(s"Mongo exception occurred. Exception: $error") => JsSuccess(MongoError(error))
            case _                                                        => JsError("Invalid format for MongoError")
          },
          Writes { mge: MongoError =>
            JsString(mge.msg)
          }
        )

    }

    implicit val databaseErrorFormat: Format[DatabaseError] =
      Format(
        Reads {
          case JsString(DataNotUpdated.msg) => JsSuccess(DataNotUpdated)
          case JsString(DataNotFound.msg)   => JsSuccess(DataNotFound)
          case jsValue @ _ =>
            MongoError.mongoErrorFormat
              .reads(jsValue)
              .orElse(JsError(s"DataBaseError $jsValue is not one of supported"))
        },
        Writes { dbError: DatabaseError => JsString(dbError.msg) }
      )

  }

  case class UnavailableServiceError(error: String) extends ServiceError {
    val msg: String = s"Unavailable Service exception occurred. Exception: $error"
  }

  object UnavailableServiceError {
    implicit val unavailableServiceErrorFormat: OFormat[UnavailableServiceError] = Json.format[UnavailableServiceError]
  }

  implicit val serviceErrorFormat: Format[ServiceError] =
    Format(
      Reads(jsValue =>
        DatabaseError.databaseErrorFormat
          .reads(jsValue)
          .orElse(UnavailableServiceError.unavailableServiceErrorFormat.reads(jsValue))
          .orElse(StatusError.statusErrorFormat.reads(jsValue))
          .orElse(ErrorBody.errorBodyFormat.reads(jsValue))),
      Writes {
        case dbe: DatabaseError           => DatabaseError.databaseErrorFormat.writes(dbe)
        case use: UnavailableServiceError => UnavailableServiceError.unavailableServiceErrorFormat.writes(use)
        case ste: StatusError             => StatusError.statusErrorFormat.writes(ste)
        case eby: ErrorBody               => ErrorBody.errorBodyFormat.writes(eby)
      }
    )

}

sealed trait StatusError extends ServiceError {
  val msg: String = ""

  def status: Int
  def toMdtpError: StatusError
  def body: ErrorBody
}

object StatusError {

  implicit val statusErrorFormat: Format[StatusError] =
    Format(
      Reads(jsValue => ApiStatusError.apiStatusErrorFormat.reads(jsValue) orElse ApiStatusErrors.apiStatusErrorsFormat.reads(jsValue)),
      Writes({
        case statusError: ApiStatusError  => ApiStatusError.apiStatusErrorFormat.writes(statusError)
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

sealed trait ErrorBody extends ServiceError {
  lazy val msg: String = reason

  def reason: String
}

object ErrorBody {

  implicit val errorBodyFormat: Format[ErrorBody] = Format(
    Reads { jsValue =>
      ApiErrorBody.apiErrorBodyFormat.reads(jsValue) orElse ApiErrorsBody.apiErrorsBodyFormat.reads(jsValue)
    },
    Writes {
      case ape: ApiErrorBody  => ApiErrorBody.apiErrorBodyFormat.writes(ape)
      case ape: ApiErrorsBody => ApiErrorsBody.apiErrorsBodyFormat.writes(ape)
    }
  )

  /** Single API Error * */
  case class ApiErrorBody(code: String, reason: String, errorType: ErrorType = DOWNSTREAM_ERROR_CODE) extends ErrorBody {

    def toMdtpError: ApiErrorBody =
      if (errorType == MDTP_ERROR_CODE) {
        this
      } else {
        val mdtpCode = code match {
          case "INVALID_NINO"         => "FORMAT_NINO"
          case "UNMATCHED_STUB_ERROR" => "RULE_INCORRECT_GOV_TEST_SCENARIO"
          case "NOT_FOUND"            => "MATCHING_RESOURCE_NOT_FOUND"
          case _                      => "INTERNAL_SERVER_ERROR"
        }
        ApiErrorBody(mdtpCode, reason, errorType = MDTP_ERROR_CODE)
      }

  }

  object ApiErrorBody {
    implicit val apiErrorBodyFormat: OFormat[ApiErrorBody] = Json.format[ApiErrorBody]

    val parsingError: ApiErrorBody = ApiErrorBody("PARSING_ERROR", "Error parsing response from API")

    val nino400: ApiErrorBody = ApiErrorBody("INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")

    val correlationI400: ApiErrorBody = ApiErrorBody("INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header CorrelationId.")

    val mtdId400: ApiErrorBody = ApiErrorBody("INVALID_MTDID", "Submission has not passed validation. Invalid parameter MTDID.")

    val data404: ApiErrorBody = ApiErrorBody("NOT_FOUND", "The remote endpoint has indicated that no data can be found.")

    val ifsServer500: ApiErrorBody = ApiErrorBody("SERVER_ERROR", "IF is currently experiencing problems that require live service intervention.")

    val service503: ApiErrorBody = ApiErrorBody("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
  }

  /** Multiple API Errors * */
  case class ApiErrorsBody(failures: Seq[ApiErrorBody], reason: String = "") extends ErrorBody

  object ApiErrorsBody {
    implicit val apiErrorsBodyFormat: OFormat[ApiErrorsBody] = Json.format[ApiErrorsBody]
  }

}
