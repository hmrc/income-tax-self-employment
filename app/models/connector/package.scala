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
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import java.time.format.DateTimeFormatter

package object connector {
  type ApiResponse[A] = Either[DownstreamError, A]

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

  implicit def httpReads[A: Reads](implicit logger: Logger): HttpReads[ApiResponse[A]] = (method: String, url: String, response: HttpResponse) => {
    ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

    response.status match {
      case OK | CREATED =>
        response.json
          .validate[A]
          .fold[Either[DownstreamError, A]](
            errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
            parsedModel => Right(parsedModel)
          )

      case _ => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
    }
  }

  private def createCommonErrorParser(method: String, url: String, response: HttpResponse): DownstreamParser =
    CommonDownstreamParser(method, url, response)
}
