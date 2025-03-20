/*
 * Copyright 2024 HM Revenue & Customs
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

package models.errors

import models.error.DownstreamError
import models.error.DownstreamError._
import models.error.DownstreamErrorBody.{MultipleDownstreamErrorBody, SingleDownstreamErrorBody}
import models.error.ErrorType.DownstreamErrorCode
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsError, JsSuccess, Json}

class DownstreamErrorSpec extends AnyWordSpec with Matchers {

  "DownstreamError" should {

    "write and read GenericDownstreamError correctly" in {
      val error = GenericDownstreamError(status = 500, errorMessage = "Internal Server Error")

      val json = Json.toJson(error)

      json.toString() shouldBe """{"status":500,"errorMessage":"Internal Server Error"}"""
      json.validate[GenericDownstreamError] shouldBe JsSuccess(error)
    }

    "write and read SingleDownstreamError correctly" in {
      val body  = SingleDownstreamErrorBody(code = "ERROR_CODE", reason = "Some reason", errorType = DownstreamErrorCode)
      val error = SingleDownstreamError(status = 400, body = body)

      val json = Json.toJson(error)

      json.toString() shouldBe """{"status":400,"body":{"code":"ERROR_CODE","reason":"Some reason","errorType":"DOWNSTREAM_ERROR_CODE"}}"""
      json.validate[SingleDownstreamError] shouldBe JsSuccess(error)
    }

    "write and read MultipleDownstreamErrors correctly" in {
      val body = MultipleDownstreamErrorBody(failures =
        Seq(SingleDownstreamErrorBody(code = "ERROR_CODE", reason = "Some reason", errorType = DownstreamErrorCode)))
      val error = MultipleDownstreamErrors(status = 400, body = body)

      val json = Json.toJson(error)

      json.toString() shouldBe """{"status":400,"body":{"failures":[{"code":"ERROR_CODE","reason":"Some reason","errorType":"DOWNSTREAM_ERROR_CODE"}]}}"""
      json.validate[MultipleDownstreamErrors] shouldBe JsSuccess(error)
    }

    "read unknown error type as JsError" in {
      val json = Json.parse("""{"status":400,"unknownField":"unknownValue"}""")

      json.validate[DownstreamError] shouldBe a[JsError]
    }
  }

  "SingleDownstreamError" should {

    "convert to domain correctly" in {
      val body        = SingleDownstreamErrorBody(code = "INVALID_MTD_ID", reason = "Some reason", errorType = DownstreamErrorCode)
      val error       = SingleDownstreamError(status = 400, body = body)
      val domainError = error.toDomain

      domainError.status shouldBe INTERNAL_SERVER_ERROR

      domainError.body shouldBe body.toDomain
    }
  }

  "MultipleDownstreamErrors" should {

    "convert to domain correctly" in {
      val body = MultipleDownstreamErrorBody(failures =
        Seq(SingleDownstreamErrorBody(code = "ERROR_CODE", reason = "Some reason", errorType = DownstreamErrorCode)))
      val error = MultipleDownstreamErrors(status = 400, body = body)

      val domainError = error.toDomain

      domainError.body.failures shouldBe body.failures.map(_.toDomain)
    }
  }
}
