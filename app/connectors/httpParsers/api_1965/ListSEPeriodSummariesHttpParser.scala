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

package connectors.httpParsers.api_1965

import cats.implicits.catsSyntaxEitherId
import connectors.httpParsers.DownstreamParser
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.error.DownstreamError
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ListSEPeriodSummariesHttpParser extends DownstreamParser {

  type Api1965Response = Either[DownstreamError, ListSEPeriodSummariesResponse]

  override val parserName: String        = "ListSEPeriodSummariesHttpParser"
  override val downstreamService: String = "Self Employment Business API"

  implicit val listSEPeriodSummariesHttpReads: HttpReads[Api1965Response] = new HttpReads[Api1965Response] {
    override def read(method: String, url: String, response: HttpResponse): Api1965Response =
      response.status match {
        case OK =>
          response.json
            .validate[ListSEPeriodSummariesResponse]
            .fold[Api1965Response](_ => invalidJsonError.asLeft, parsedModel => parsedModel.asRight)

        case _ => pagerDutyError(response).asLeft
      }
  }
}
