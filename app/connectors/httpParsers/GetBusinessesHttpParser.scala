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

package connectors.httpParsers

import models.connector.api_1171.BusinessData.GetBusinessDataRequest
import models.error.DownstreamError
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetBusinessesHttpParser extends DownstreamParser {
  type GetBusinessesRequestResponse = Either[DownstreamError, GetBusinessDataRequest]

  override val parserName: String        = "GetBusinessHttpParser"
  override val downstreamService: String = "Business Details API"

  implicit object GetBusinessesHttpReads extends HttpReads[GetBusinessesRequestResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetBusinessesRequestResponse =
      response.status match {
        case OK =>
          response.json
            .validate[GetBusinessDataRequest]
            .fold[GetBusinessesRequestResponse](_ => Left(invalidJsonError), parsedModel => Right(parsedModel))

        case _ => Left(pagerDutyError(response))
      }
  }
}
