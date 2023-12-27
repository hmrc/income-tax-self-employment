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

import helpers.PagerDutyAware
import models.error.DownstreamError
import models.error.DownstreamErrorBody._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.HttpResponse
import utils.PagerDutyHelper.PagerDutyKeys._

import DownstreamParser._

class CommonDownstreamParserSpec extends AnyWordSpecLike with PagerDutyAware with TableDrivenPropertyChecks {
  val newValue = "http://someurl"

  "logMessage" should {
    "log properly formatted message" in {
      val result = CommonDownstreamParser(newValue).logMessage(HttpResponse(500, "test"))

      assert(result === "[CommonDownstreamParser][read] Received 500 from http://someurl. Body: test ")
    }
  }

  "reportInvalidJsonError" should {
    "return a SingleDownstreamError and log a pager duty error" in new PagerDutyAware {
      val jsPath = __ \ "some"
      val result = CommonDownstreamParser(newValue).reportInvalidJsonError(
        List(
          (jsPath, Nil)
        ))

      assert(result === DownstreamError.SingleDownstreamError(500, SingleDownstreamErrorBody.parsingError))
      assert(loggedErrors.exists(_.contains(BAD_SUCCESS_JSON_FROM_API.toString)) === true)
      assert(loggedErrors.exists(_.contains("some")) === true)
    }
  }

  "pagerDutyError" should {
    val cases = Table(
      ("status", "expectedPagerDutyKey", "expectedStatus"),
      (BAD_REQUEST, FOURXX_RESPONSE_FROM_API, BAD_REQUEST),
      (NOT_FOUND, FOURXX_RESPONSE_FROM_API, NOT_FOUND),
      (INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_FROM_API, INTERNAL_SERVER_ERROR),
      (SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE_FROM_API, SERVICE_UNAVAILABLE),
      (NETWORK_AUTHENTICATION_REQUIRED, UNEXPECTED_RESPONSE_FROM_API, INTERNAL_SERVER_ERROR)
    )

    forAll(cases) { case (status, expectedKey, expectedStatus) =>
      s"return a pager duty $expectedKey and http status $expectedStatus for an error response with status=$status" in new PagerDutyAware {
        val parser = DownstreamParser.CommonDownstreamParser("url")
        val result = parser.pagerDutyError(HttpResponse(status, ""))

        assert(result.status === expectedStatus)
        assert(loggedErrors.exists(_.contains(expectedKey.toString)) === true)
      }
    }
  }
}
