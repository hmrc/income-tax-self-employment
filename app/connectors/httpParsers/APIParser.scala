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

import models.error.ErrorBody.{ApiErrorBody, ApiErrorsBody}
import models.error.StatusError
import models.error.StatusError.{ApiStatusError, ApiStatusErrors}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

import scala.util.{Failure, Success, Try}

trait APIParser {
  def parserName: String

  def apiType: String

  def logMessage(response: HttpResponse): String =
    s"[$parserName][read] Received ${response.status} from $apiType API. Body:${response.body} ${getCorrelationId(response)}"

  def apiJsonValidatingError[Response]: Either[ApiStatusError, Response] = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, s"[$parserName][read] Invalid Json from $apiType API.")
    Left(ApiStatusError(INTERNAL_SERVER_ERROR, ApiErrorBody.parsingError))
  }

  def handleAPIError[Response](response: HttpResponse, statusOverride: Option[Int] = None): Either[StatusError, Response] = {
    val status = statusOverride.getOrElse(response.status)
    Try {
      val json    = response.json
      val apiErrs = json.asOpt[ApiErrorsBody]
      if (apiErrs.nonEmpty) Left(ApiStatusErrors(status, apiErrs.get)) else Left(ApiStatusError(status, json.as[ApiErrorBody]))
    } match {
      case Success(leftStatusError) => leftStatusError
      case Failure(t) =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, s"[$parserName][read] Unexpected Json error: ${t.getMessage} from $apiType API.")
        Left(ApiStatusError(status, ApiErrorBody.parsingError))
    }
  }

  def pagerDutyError[A](response: HttpResponse): Either[StatusError, A] =
    response.status match {
      case BAD_REQUEST =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response)
      case NOT_FOUND =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response)
      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, logMessage(response))
        handleAPIError(response)
      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, logMessage(response))
        handleAPIError(response)
      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, logMessage(response))
        handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
    }
}
