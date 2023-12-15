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

trait ServiceError {
  val errorMessage: String
}

object ServiceError {
  type JsonErrorWithPath = List[(JsPath, scala.collection.Seq[JsonValidationError])]

  // TODO We need just one common case class which we always are going to return to the caller in an error scenario
  implicit val formats: Format[ServiceError] =
    Format(
      Reads((jsValue: JsValue) =>
        DatabaseError.formats
          .reads(jsValue)
          .orElse(ServiceUnavailableError.formats.reads(jsValue))
          .orElse(DownstreamError.formats.reads(jsValue))
          .orElse(DownstreamErrorBody.formats.reads(jsValue))),
      Writes {
        case de: DatabaseError           => DatabaseError.formats.writes(de)
        case se: ServiceUnavailableError => ServiceUnavailableError.formats.writes(se)
        case de: DownstreamError         => DownstreamError.formats.writes(de)
        case eb: DownstreamErrorBody     => DownstreamErrorBody.formats.writes(eb)
        case error                       => Json.obj("errorMessage" -> error.errorMessage)
      }
    )

  case class InvalidJsonFormatError(expectedCaseClassName: String, rawJson: String, error: JsonErrorWithPath) extends ServiceError {
    val errorMessage: String = s"Cannot convert JSON to a case class: $expectedCaseClassName. Error: ${error.toString}. JSON:\n$rawJson"
  }

  case class ServiceUnavailableError(error: String) extends ServiceError {
    val errorMessage: String = s"Unavailable Service exception occurred. Exception: $error"
  }

  object ServiceUnavailableError {
    implicit val formats: OFormat[ServiceUnavailableError] = Json.format[ServiceUnavailableError]
  }

  final case class CannotReadJsonError(details: JsonErrorWithPath) extends ServiceError {
    val errorMessage: String = s"Cannot read JSON: ${details.toString}"
  }

  final case class CannotParseJsonError(details: Throwable) extends ServiceError {
    val errorMessage: String = s"Cannot parse JSON: ${details.getMessage}"
  }

  sealed trait DatabaseError extends ServiceError

  object DatabaseError {

    case object DataNotUpdated extends DatabaseError {
      val errorMessage: String = "User data was not updated due to mongo exception"
    }

    case object DataNotFound extends DatabaseError {
      val errorMessage: String = "User data could not be found due to mongo exception"
    }

    case class MongoError(error: String) extends DatabaseError {
      val errorMessage: String = s"Mongo exception occurred. Exception: $error"
    }

    object MongoError {
      implicit val formats: Format[MongoError] =
        Format(
          Reads {
            case JsString(s"Mongo exception occurred. Exception: $error") => JsSuccess(MongoError(error))
            case _                                                        => JsError("Invalid format for MongoError")
          },
          Writes { mge: MongoError =>
            JsString(mge.errorMessage)
          }
        )

    }

    implicit val formats: Format[DatabaseError] =
      Format(
        Reads {
          case JsString(DataNotUpdated.`errorMessage`) => JsSuccess(DataNotUpdated)
          case JsString(DataNotFound.`errorMessage`)   => JsSuccess(DataNotFound)
          case jsValue @ _ =>
            MongoError.formats
              .reads(jsValue)
              .orElse(JsError(s"DataBaseError $jsValue is not one of supported"))
        },
        Writes { dbError: DatabaseError => JsString(dbError.errorMessage) }
      )

  }

}
