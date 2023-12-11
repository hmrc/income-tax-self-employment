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

package connectors.httpParsers.api_1802

import connectors.httpParsers.DownstreamParser
import models.connector.api_1802.response.CreateAmendSEAnnualSubmissionResponse
import models.error.DownstreamError
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object CreateAmendSEAnnualSubmissionHttpParser extends DownstreamParser {

  type Api1802Response = Either[DownstreamError, CreateAmendSEAnnualSubmissionResponse]

  override val parserName: String        = "CreateAmendSEAnnualSubmissionHttpParser"
  override val downstreamService: String = "Self Employment Business API"

  implicit val createSEAnnualSubmissionHttpReads: HttpReads[Api1802Response] = new HttpReads[Api1802Response] {
    override def read(method: String, url: String, response: HttpResponse): Api1802Response =
      response.status match {
        case OK =>
          response.json
            .validate[CreateAmendSEAnnualSubmissionResponse]
            .fold[Api1802Response](_ => Left(invalidJsonError), parsedModel => Right(parsedModel))

        case _ => Left(pagerDutyError(response))
      }
  }

}
