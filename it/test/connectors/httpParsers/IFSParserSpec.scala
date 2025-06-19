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

import connectors.DownstreamParser
import models.error.DownstreamError.{GenericDownstreamError, MultipleDownstreamErrors, SingleDownstreamError}
import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtils

// TODO make it normal test with assert ===
class IFSParserSpec extends TestUtils {
  val downstreamApi = "IFS"

  object FakeParser extends DownstreamParser {
    val parserName: String    = "TestParser"
    val targetUrl: String = downstreamApi
    val requestMethod: String = "GET"
    val responseStatus: Int   = 500
    val responseBody: String  = "error body"
  }

  val serviceUnavailableReason = "Dependent systems are currently not responding."
  val serverErrorReason        = "IFS is currently experiencing problems that require live service intervention."
  val parsingErrorReason       = "Error parsing response from API"

  val svcUnavailJs    = s"""{"code":"SERVICE_UNAVAILABLE", "reason":"$serviceUnavailableReason", "errorType":"DOWNSTREAM_ERROR_CODE"}"""
  val svrErrJs        = s"""{"code":"SERVER_ERROR", "reason":"$serverErrorReason", "errorType":"DOWNSTREAM_ERROR_CODE"}"""
  val multiErrJs: String = s"""{"failures":[$svcUnavailJs, $svrErrJs], "reason":""}""".stripMargin
  val nonValidatingJs = s"""{"code":"SERVER_ERROR", "reason":"$serverErrorReason", "errorType":"WRONG_ERROR_CODE"}"""

  def failureHttpResponse(json: JsValue): HttpResponse =
    HttpResponse(INTERNAL_SERVER_ERROR, json, Map("CorrelationId" -> Seq("1234645654645")))

  def parserShould(): Unit =
    "FakeParser" should {
      logHttpResponse()
      handleSingleError()
      handleMultpleError()
      returnParsingErrors()
    }

  def returnParsingErrors(): Unit =
    "return a parsing error" when {
      returnErrorOnWrongModel()
      handleNonApiErrorResponseError()
      handleNonJsonResponseBodyError()
    }

  def logHttpResponse(): Unit =
    "log the correct message" in {
      val result = FakeParser.logMessage(failureHttpResponse(Json.parse(multiErrJs)))

      result mustBe
        // note*: the spacings in the String below are important and the test will fail if altered
        s"""[TestParser][read] Received 500 from $downstreamApi. Body: {
           |  "failures" : [ {
           |    "code" : "SERVICE_UNAVAILABLE",
           |    "reason" : "$serviceUnavailableReason",
           |    "errorType" : "DOWNSTREAM_ERROR_CODE"
           |  }, {
           |    "code" : "SERVER_ERROR",
           |    "reason" : "$serverErrorReason",
           |    "errorType" : "DOWNSTREAM_ERROR_CODE"
           |  } ],
           |  "reason" : ""
           |}  CorrelationId: 1234645654645""".stripMargin
    }

  def handleSingleError(): Unit =
    "handle a single error" in {
      val result = FakeParser.handleDownstreamError(failureHttpResponse(Json.parse(svrErrJs)))
      result mustBe SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody("SERVER_ERROR", serverErrorReason))
    }

  def handleMultpleError(): Unit =
    "handle a multiple error" in {
      val result = FakeParser.handleDownstreamError(failureHttpResponse(Json.parse(multiErrJs)))
      result mustBe
        MultipleDownstreamErrors(
          INTERNAL_SERVER_ERROR,
          MultipleDownstreamErrorBody(
            Seq(
              SingleDownstreamErrorBody("SERVICE_UNAVAILABLE", serviceUnavailableReason),
              SingleDownstreamErrorBody("SERVER_ERROR", serverErrorReason)
            ))
        )
    }

  def returnErrorOnWrongModel(): Unit =
    "return an error even if model is different" in {
      val result = FakeParser.handleDownstreamError(failureHttpResponse(Json.parse(nonValidatingJs)))
      val expected = GenericDownstreamError(
        500,
        """Downstream error when calling GET IFS: status=500, body:
          |{
          |  "code" : "SERVER_ERROR",
          |  "reason" : "IFS is currently experiencing problems that require live service intervention.",
          |  "errorType" : "WRONG_ERROR_CODE"
          |}""".stripMargin
      )

      result mustBe expected
    }

  def handleNonApiErrorResponseError(): Unit =
    "handling a response that is neither a single or a multiple error" in {
      val result   = FakeParser.handleDownstreamError(failureHttpResponse(Json.obj()))
      val expected = GenericDownstreamError(500, "Downstream error when calling GET IFS: status=500, body:\n{ }")

      result mustBe expected
    }

  def handleNonJsonResponseBodyError(): Unit =
    "handling a response where the response body is not json" in {
      val result =
        FakeParser.handleDownstreamError(HttpResponse(INTERNAL_SERVER_ERROR, "non-json body", Map("CorrelationId" -> Seq("1234645654645"))))
      val expected = GenericDownstreamError(500, "Downstream error when calling GET IFS: status=500, body:\nnon-json body")

      result mustBe expected
    }

  behave like parserShould()
}
