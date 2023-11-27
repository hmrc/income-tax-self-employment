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

import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import models.error.DownstreamError
import play.api.http.Status.CREATED
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object CreateSEPeriodSummaryHttpParser extends DownstreamParser {

  type Api1894Response = Either[DownstreamError, CreateSEPeriodSummaryResponse]

  override val parserName: String        = "CreateSEPeriodSummaryHttpParser"
  override val downstreamService: String = "Self Employment Business API"

  def createSEPeriodSummaryHttpReads(implicit reads: Reads[CreateSEPeriodSummaryResponse]): HttpReads[Api1894Response] =
    new HttpReads[Api1894Response] {
      override def read(method: String, url: String, response: HttpResponse): Api1894Response =
        response.status match {
          case CREATED =>
            response.json
              .validate[CreateSEPeriodSummaryResponse]
              .fold[Api1894Response](_ => Left(apiJsonValidatingError), parsedModel => Right(parsedModel))

          case _ => Left(pagerDutyError(response))
        }
    }

}
