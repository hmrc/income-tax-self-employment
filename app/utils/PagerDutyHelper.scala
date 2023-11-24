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

package utils

import models.error.ServiceError
import models.error.ServiceError.DatabaseError.MongoError
import models.error.ServiceError.ServiceUnavailableError
import play.api.Logging
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys.FAILED_TO_GET_JOURNEY_STATE_DATA

import scala.concurrent.{ExecutionContext, Future}

object PagerDutyHelper extends Logging {

  object PagerDutyKeys extends Enumeration {
    val BAD_SUCCESS_JSON_FROM_API: PagerDutyKeys.Value        = Value
    val SERVICE_UNAVAILABLE_FROM_API: PagerDutyKeys.Value     = Value
    val INTERNAL_SERVER_ERROR_FROM_API: PagerDutyKeys.Value   = Value
    val UNEXPECTED_RESPONSE_FROM_API: PagerDutyKeys.Value     = Value
    val FOURXX_RESPONSE_FROM_API: PagerDutyKeys.Value         = Value
    val FAILED_TO_GET_JOURNEY_STATE_DATA: PagerDutyKeys.Value = Value
  }

  def pagerDutyLog(pagerDutyKey: PagerDutyKeys.Value, otherDetail: String = ""): Unit =
    logger.error(s"$pagerDutyKey $otherDetail")

  def getCorrelationId(response: HttpResponse): String =
    response.header("CorrelationId") match {
      case Some(id) => s" CorrelationId: $id"
      case _        => ""
    }

  type PagerDutyKey = PagerDutyHelper.PagerDutyKeys.Value

  type Not[T] = T => Nothing // L,R,T <: Not[Either[L,R]]

  implicit class WithRecovery[T](future: Future[T]) {
    def recoverWithPagerDutyLog(pagerDutyKey: PagerDutyKey, msg: String)(implicit ec: ExecutionContext): Future[Either[ServiceError, T]] =
      future
        .map(Right(_))
        .recover { case exception: Exception =>
          pagerDutyLog(pagerDutyKey, s"$msg Exception: ${exception.getMessage}")
          pagerDutyKey match {
            case FAILED_TO_GET_JOURNEY_STATE_DATA => Left(MongoError(exception.getMessage))
            case _                                => Left(ServiceUnavailableError(exception.getMessage))
          }
        }
  }

  implicit class WithRecoveryEither[L, R](future: Future[Either[L, R]]) {
    def recoverEitherWithPagerDutyLog(pagerDutyKey: PagerDutyKey, msg: String)(implicit ec: ExecutionContext): Future[Either[ServiceError, R]] =
      future
        .map {
          case Left(serviceError: ServiceError) => Left(serviceError)
          case Left(error)                      => throw new RuntimeException(error.toString)
          case Right(r)                         => Right(r)
        }
        .recover { case exception: Exception =>
          pagerDutyLog(pagerDutyKey, s"$msg Exception: ${exception.getMessage}")
          pagerDutyKey match {
            case FAILED_TO_GET_JOURNEY_STATE_DATA => Left(MongoError(exception.getMessage))
            case _                                => Left(ServiceUnavailableError(exception.getMessage))
          }
        }
  }
}
