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

package connectors.httpParsers.api_1895

import cats.implicits.catsSyntaxEitherId
import connectors.httpParsers.DownstreamParser
import models.connector.api_1895.response.AmendSEPeriodSummaryResponse
import models.error.DownstreamError
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object AmendSEPeriodSummaryHttpParser extends DownstreamParser {

  type Api1895Response = Either[DownstreamError, AmendSEPeriodSummaryResponse]

  override val parserName: String        = "AmendSEPeriodSummaryHttpParser"
  override val downstreamService: String = "Self Employment Business API"

  implicit val amendSEPeriodSummaryHttpReads: HttpReads[Api1895Response] = new HttpReads[Api1895Response] {
    override def read(method: String, url: String, response: HttpResponse): Api1895Response =
      response.status match {
        case OK =>
          response.json
            .validate[AmendSEPeriodSummaryResponse]
            .fold[Api1895Response](_ => invalidJsonError.asLeft, parsedModel => parsedModel.asRight)

        case _ => pagerDutyError(response).asLeft
      }
  }

}
