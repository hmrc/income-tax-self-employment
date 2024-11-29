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

package models.connector

import models.connector.api_1867.{CarryForward, ReliefClaim}
import models.error.DownstreamError.GenericDownstreamError
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

class CommonReadsSpec extends AnyWordSpec with Matchers {

  val testIncomeSourceId      = "XAIS12345678901"
  val testTaxYear             = "2024"
  val testClaimId             = "1234567890"
  implicit val logger: Logger = Logger.apply("test")

  "commonGetListReads" when {
    "the response is 200 OK" must {
      "return a valid list" in {
        val reliefClaimList = List(
          ReliefClaim(
            incomeSourceId = testIncomeSourceId,
            reliefClaimed = CarryForward,
            taxYearClaimedFor = testTaxYear,
            claimId = testClaimId,
            submissionDate = LocalDate.of(2024, 1, 1)
          ))
        val response = HttpResponse(OK, Json.stringify(Json.toJson(reliefClaimList)))

        val result = commonGetListReads[ReliefClaim].read("GET", "http://localhost:1234", response)

        result mustBe Right(reliefClaimList)
      }
    }

    "the response is 404 NOT_FOUND" must {
      "return an empty list" in {
        val response = HttpResponse(NOT_FOUND, "")

        val result = commonGetListReads[ReliefClaim].read("GET", "http://localhost:1234", response)

        result mustBe Right(Nil)
      }
    }

    "the response is 204 NO_CONTENT" must {
      "return an empty list" in {
        val response = HttpResponse(NO_CONTENT, "")

        val result = commonGetListReads[ReliefClaim].read("GET", "http://localhost:1234", response)

        result mustBe Right(Nil)
      }
    }

    "the response is anything else" must {
      "return a GenericDownstreamError" in {
        val response = HttpResponse(INTERNAL_SERVER_ERROR, "")

        val result = commonGetListReads[ReliefClaim].read("GET", "http://localhost:1234", response)

        result.isLeft mustBe true
        result.merge mustBe a[GenericDownstreamError]
      }
    }

  }

}
