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
  def msg: String
}

object ServiceError {
  trait DatabaseError extends ServiceError

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
      },
      Writes {
        mge: MongoError => JsString(mge.msg)
      }
    )
  }

  object DatabaseError {
    implicit val databaseErrorFormat: Format[DatabaseError] =
      Format(
        Reads {
          case JsString(DataNotUpdated.msg) => JsSuccess(DataNotUpdated)
          case JsString(DataNotFound.msg) => JsSuccess(DataNotFound)
          case jsValue@_ =>
            MongoError.mongoErrorFormat.reads(jsValue)
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
        DatabaseError.databaseErrorFormat.reads(jsValue)
          .orElse(UnavailableServiceError.unavailableServiceErrorFormat.reads(jsValue))
          .orElse(StatusError.statusErrorFormat.reads(jsValue))
          .orElse(ErrorBody.errorBodyFormat.reads(jsValue))
      ),
      Writes {
        case dbe: DatabaseError => DatabaseError.databaseErrorFormat.writes(dbe)
        case use: UnavailableServiceError => UnavailableServiceError.unavailableServiceErrorFormat.writes(use)
        case ste: StatusError => StatusError.statusErrorFormat.writes(ste)
        case eby: ErrorBody => ErrorBody.errorBodyFormat.writes(eby)
      }
    )
}

