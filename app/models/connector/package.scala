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

package models

import connectors.DownstreamParser
import connectors.DownstreamParser.CommonDownstreamParser
import models.error.DownstreamError
import models.logging.ConnectorResponseInfo
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import java.time.format.DateTimeFormatter

package object connector {
  type ApiResponse[A]       = Either[DownstreamError, A]
  type ApiResponseOption[A] = Either[DownstreamError, Option[A]]

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  /** It treats any non OK / CREATED / ACCEPTED as an error
    */
  implicit def commonReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[A]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK | CREATED | ACCEPTED => toA(response, method, url)
      case _                       => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  /** It treats any non OK / CREATED / NO_CONTENT / ACCEPTED as an error, and return Unit otherwise
    */
  def commonNoBodyResponse(implicit logger: Logger): HttpReads[ApiResponse[Unit]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK | NO_CONTENT | CREATED | ACCEPTED => Right(())
      case _                                    => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  /** It treats NOT_FOUND / OK / CREATED / ACCEPTED as correct response and returns None
    */
  def commonGetReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[Option[A]]] =
    (method: String, url: String, response: HttpResponse) => {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      response.status match {
        case OK | CREATED | ACCEPTED => toA(response, method, url).map(Some(_))
        case NOT_FOUND | NO_CONTENT  => Right(None)
        case _                       => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
      }
    }

  def commonGetListReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[List[A]]] =
    (method: String, url: String, response: HttpResponse) => {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      response.status match {
        case OK                     => toA[List[A]](response, method, url)
        case NOT_FOUND | NO_CONTENT => Right(Nil)
        case _                      => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
      }
    }

  def listSEPeriodGetReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[Option[A]]] =
    (method: String, url: String, response: HttpResponse) => {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      response.status match {
        case OK | CREATED | ACCEPTED                      => toA(response, method, url).map(Option(_))
        case NOT_FOUND | NO_CONTENT | SERVICE_UNAVAILABLE => Right(None)
        case _                                            => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
      }
    }

  /** It treats NOT_FOUND / NO_CONTENT / OK / ACCEPTED as correct response and returns None
    */
  def commonDeleteReads(implicit logger: Logger): HttpReads[ApiResponse[Unit]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case NOT_FOUND | NO_CONTENT | OK | ACCEPTED => Right(())
      case _                                      => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  def lossClaimReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[A]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK                                                        => toA(response, method, url)
      case BAD_REQUEST | NOT_FOUND | CONFLICT | UNPROCESSABLE_ENTITY => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
      case _                                                         => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  private def toA[A: Reads](response: HttpResponse, method: String, url: String): Either[DownstreamError, A] =
    response.json
      .validate[A]
      .fold[Either[DownstreamError, A]](
        errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
        parsedModel => Right(parsedModel)
      )

  private def createCommonErrorParser(method: String, url: String, response: HttpResponse): DownstreamParser =
    CommonDownstreamParser(method, url, response)
}
