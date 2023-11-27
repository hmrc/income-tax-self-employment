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

import models.error.DownstreamError
import models.error.DownstreamError.{MultipleDownstreamErrors, SingleDownstreamError}
import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

import scala.util.{Failure, Success, Try}

trait DownstreamParser {
  val parserName: String
  val downstreamService: String

  def logMessage(response: HttpResponse): String =
    s"[$parserName][read] Received ${response.status} from $downstreamService. Body:${response.body} ${getCorrelationId(response)}"

  def apiJsonValidatingError: SingleDownstreamError = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, s"[$parserName][read] Invalid Json from $downstreamService.")
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
  }

  // FIXME - I don't think this code does what it should do.
  // It seems as if the case classes for deserializing the error response json are wrong based off what is documented in
  // the API OAS for error responses. Until we know what the response json will actually look like from IFS (which we
  // currently have not integrated with them in order to try), we cannot be sure what our model will look like.
  def handleDownstreamError(response: HttpResponse, statusOverride: Option[Int] = None): DownstreamError = {
    val status = statusOverride.getOrElse(response.status)
    Try {
      val json    = response.json
      val apiErrs = json.asOpt[MultipleDownstreamErrorBody]
      if (apiErrs.nonEmpty) MultipleDownstreamErrors(status, apiErrs.get)
      else SingleDownstreamError(status, json.as[SingleDownstreamErrorBody])
    } match {
      case Success(leftStatusError) => leftStatusError
      case Failure(t) =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, s"[$parserName][read] Unexpected Json error: ${t.getMessage} from $downstreamService service.")
        SingleDownstreamError(status, SingleDownstreamErrorBody.parsingError)
    }
  }

  def pagerDutyError(response: HttpResponse): DownstreamError =
    response.status match {
      case BAD_REQUEST | NOT_FOUND =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
        handleDownstreamError(response)

      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
        handleDownstreamError(response)

      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
        handleDownstreamError(response)

      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
        handleDownstreamError(response, Some(INTERNAL_SERVER_ERROR))
    }
}
