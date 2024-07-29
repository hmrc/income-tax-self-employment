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

import cats.implicits.catsSyntaxEitherId
import connectors.DownstreamParser
import connectors.DownstreamParser.CommonDownstreamParser
import models.error.DownstreamError
import models.logging.ConnectorResponseInfo
import play.api.Logger
import play.api.http.Status.{ACCEPTED, CREATED, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import java.time.format.DateTimeFormatter

package object connector {
  type ApiResponse[A]       = Either[DownstreamError, A]
  type ApiResponseOption[A] = Either[DownstreamError, Option[A]]

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit def commonReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[A]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK | CREATED | ACCEPTED => toA(response, method, url)
      case _                       => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  def commonNoBodyResponse(implicit logger: Logger): HttpReads[ApiResponse[Unit]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK | CREATED | ACCEPTED => Right(None)
      case _                       => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  def commonGetReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[Option[A]]] =
    (method: String, url: String, response: HttpResponse) => {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      response.status match {
        case OK | CREATED | ACCEPTED => toA(response, method, url).map(Some(_))
        case NOT_FOUND               => Right(None)
        case _                       => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
      }
    }

  def commonDeleteReads(implicit logger: Logger): HttpReads[ApiResponse[Unit]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case NOT_FOUND | NO_CONTENT | OK | ACCEPTED => Right(())
      case _                                      => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
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
