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

package connectors

import models.error.DownstreamError
import models.error.DownstreamError.{MultipleDownstreamErrors, SingleDownstreamError}
import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsPath, JsonValidationError}
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.{getCorrelationId, pagerDutyLog}

import scala.util.{Failure, Success, Try}

trait DownstreamParser {
  val parserName: String
  val targetUrl: String
  val requestMethod: String
  val responseStatus: Int
  val responseBody: String

  private[connectors] def logMessage(response: HttpResponse): String =
    s"[$parserName][read] Received ${response.status} from $targetUrl. Body: ${response.body} ${getCorrelationId(response)}"

  def reportInvalidJsonError(errors: List[(JsPath, scala.collection.Seq[JsonValidationError])]): SingleDownstreamError = {
    pagerDutyLog(
      BAD_SUCCESS_JSON_FROM_API,
      s"[$parserName][read] Invalid Json when calling $requestMethod $targetUrl: ${formatJsonErrors(errors)}, responseStatus: $responseStatus, responseBody: $responseBody"
    )
    SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
  }

  private def formatJsonErrors(errors: List[(JsPath, scala.collection.Seq[JsonValidationError])]): String = {
    val errorMessages = errors.flatMap { case (path, validationErrors) =>
      validationErrors.map { error =>
        val pathString   = path.toJsonString
        val errorMessage = error.message
        val args         = error.args.mkString(", ")
        s"Error at path $pathString: $errorMessage [args: $args]"
      }
    }

    errorMessages.mkString("\n")
  }

  // FIXME - I don't think this code does what it should.
  // It seems as if the case classes for deserializing the error response json are wrong based off what is documented in
  // the API OAS for error responses. Until we know what the response json will actually look like from IFS (which we
  // currently have not integrated with them in order to try), we cannot be 100% sure what our model will look like.
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
        pagerDutyLog(
          UNEXPECTED_RESPONSE_FROM_API,
          s"[$parserName][read] Unexpected Json error: ${t.getMessage} when calling $requestMethod $targetUrl, responseStatus: $responseStatus, responseBody $responseBody"
        )
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

object DownstreamParser {
  case class CommonDownstreamParser(method: String, url: String, response: HttpResponse) extends DownstreamParser {
    val parserName: String    = "CommonDownstreamParser"
    val targetUrl: String     = url
    val requestMethod: String = method
    val responseStatus: Int   = response.status
    val responseBody: String  = response.body
  }
}
