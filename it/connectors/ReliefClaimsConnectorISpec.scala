/*
 * Copyright 2025 HM Revenue & Customs
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

import base.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import helpers.WiremockSpec
import models.connector.ReliefClaimType.{CF, CSGI}
import models.connector._
import models.connector.api_1505.{CreateLossClaimRequestBody, ClaimId}
import models.connector.common.ReliefClaim
import models.error.DownstreamError.GenericDownstreamError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers.logger
import play.api.http.Status._
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsObject, Json}
import testdata.CommonTestData
import uk.gov.hmrc.http.HttpReads

import java.time.LocalDate


class ReliefClaimsConnectorISpec extends WiremockSpec with IntegrationBaseSpec with CommonTestData {

  val connector: ReliefClaimsConnector = app.injector.instanceOf[ReliefClaimsConnector]

  val api1505Url: String = s"/income-tax/claims-for-relief/${testBusinessId.value}"
  val api1506Url: String = s"/income-tax/claims-for-relief/${testBusinessId.value}/1234567890"
  val api1507Url: String = api1505Url
  val api1867Url: String = s"/income-tax/${testTaxYear2024.endYear}/claims-for-relief/${testBusinessId.value}"

  implicit val reads: HttpReads[ApiResponse[ClaimId]] = lossClaimReads[ClaimId]

  val selfEmploymentClaim: JsObject = Json.obj(
    "incomeSourceId"    -> "XAIS12345678901",
    "reliefClaimed"     -> "01",
    "reliefClaimed"     -> "CF",
    "taxYearClaimedFor" -> "2024",
    "claimId"           -> "1234567890",
    "submissionDate"    -> "2024-01-01"
  )

  val testBaseReliefClaim: ReliefClaim = ReliefClaim(
    incomeSourceId = "XAIS12345678901",
    incomeSourceType = None,
    reliefClaimed = ReliefClaimType.CF,
    taxYearClaimedFor = "2024",
    claimId = "1234567890",
    submissionDate = LocalDate.of(2024, 1, 1)
  )

  val claims: List[ReliefClaim] = List(
    ReliefClaim("XAIS12345678900", None, CF, "2025", "12345", None, LocalDate.parse("2024-01-01")),
    ReliefClaim("XAIS12345678901", None, CSGI, "2024", "1234567890", None, LocalDate.parse("2024-01-01"))
  )

  val expectedClaims: List[ReliefClaim] = List(
    ReliefClaim("XAIS12345678901", None, CF, "2024", "1234567890", None, LocalDate.parse("2024-01-01")),
    ReliefClaim("XAIS12345678901", None, CF, "2025", "1234567890", None, LocalDate.parse("2024-01-01"))
  )

  "getAllReliefClaims" when {
    "the API returns 200 OK" should {
      "successfully parse a Self Employment claim" in {
        val response = Json.arr(selfEmploymentClaim)

        stubGetWithResponseBody(
          url = api1507Url,
          expectedStatus = OK,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getAllReliefClaims(testTaxYear2024, testBusinessId).value.futureValue

        result mustBe Right(List(testBaseReliefClaim))
        result.map(_.head.isSelfEmploymentClaim) mustBe Right(true)
      }
    }

    "call API1507 and return all claims" in {
      val selfEmploymentClaim2025 = selfEmploymentClaim + ("taxYearClaimedFor" -> Json.toJson("2025"))
      val response = Json.arr(selfEmploymentClaim, selfEmploymentClaim2025)

      stubGetWithResponseBody(
        url = api1507Url,
        expectedStatus = OK,
        expectedResponse = Json.stringify(response)
      )

      val result = connector.getAllReliefClaims(testTaxYear2024, testBusinessId).value.futureValue

      result mustBe Right(expectedClaims)
    }

//    "the API returns 404 NOT_FOUND" should {
//      "return a Left with an error message" in {
//        val response: JsObject = Json.obj("failures" -> Json.arr(Json.obj("code" -> "NOT_FOUND", "reason" -> "Resource not found")))
//
//        stubGetWithResponseBody(
//          url = api1867Url,
//          expectedStatus = NOT_FOUND,
//          expectedResponse = Json.stringify(response)
//        )
//
//        val result = connector.getAllReliefClaims(testTaxYear2024, testBusinessId).value.futureValue
//
//        result mustBe Right(Nil)
//      }
//    }


    "the API returns 400 BAD_REQUEST, 422 UNPROCESSABLE_ENTITY or 5xx response" should {
      "return a service error" in {
        Seq(
          (BAD_REQUEST, "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear."),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", "The remote endpoint has indicated that this tax year is not supported."),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", "IF is currently experiencing problems that require live service intervention."),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
        ) foreach { case (status, code, reason) =>

          val response: JsObject = Json.obj(
            "failures" -> Json.arr(
              Json.obj(
                "code" -> code,
                "reason" -> reason
              )
            )
          )

          stubGetWithResponseBody(
            url = api1507Url,
            expectedStatus = status,
            expectedResponse = Json.stringify(response)
          )

          val result = connector.getAllReliefClaims(testTaxYear2024, testBusinessId).value.futureValue

          result.isLeft mustBe true
          result.merge mustBe a[GenericDownstreamError]
        }
      }
    }
  }

  "createLossClaims" should {

    "call API 1505 once to create a relief claim for 1 checkbox" in {
      val expectedResponse = ClaimId(value = testClaimId)

      stubPostWithResponseBody(
        url = api1505Url,
        expectedStatus = OK,
        expectedResponse = Json.stringify(Json.toJson(expectedResponse))
      )

      val body: CreateLossClaimRequestBody = CreateLossClaimRequestBody(
        incomeSourceId = "012345678912345",
        reliefClaimed = "CF",
        taxYear = "2024"
      )

      val result = await(connector.createReliefClaim(testContextWithNino, ReliefClaimType.CF).value)
      result mustBe Right(expectedResponse)

      verify(1, postRequestedFor(urlEqualTo(api1505Url)))
    }

    // TODO: Move tests to service spec because we've refactored the connector to only make singular calls

  }

  // TODO: Move test to service spec
//  "updateReliefClaims" should {
//    "create and delete relief claims correctly" in {
//      val oldAnswers = claims
//      val newAnswers = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
//
//      stubDelete(
//        url = api1506Url,
//        expectedStatus = OK,
//        expectedResponse = ""
//      )
//
//      stubPostWithResponseBody(
//        url = api1505Url,
//        expectedStatus = OK,
//        expectedResponse = newAnswers.toString()
//      )
//
//      val result = connector.updateReliefClaims(testContextWithNino, oldAnswers, newAnswers).value
//
//      val finalClaims = result.map {
//        case Right(claims) => claims
//        case Left(_) => List.empty[WhatDoYouWantToDoWithLoss]
//      }
//
//      finalClaims.map { claims =>
//        claims must not contain WhatDoYouWantToDoWithLoss.fromReliefClaimType(ReliefClaimType.CF)
//      }
//
//      verify(1, postRequestedFor(urlEqualTo(api1505Url)))
//      verify(1, deleteRequestedFor(urlEqualTo(api1506Url)))
//
//    }
//
//    "do nothing if answer exists in both lists" in {
//      val oldAnswers = List(
//        ReliefClaim("XAIS12345678900", None, ReliefClaimType.CF, "2024", "1234567890", None, LocalDate.parse("2024-01-01")),
//        ReliefClaim("XAIS12345678900", None, ReliefClaimType.CF, "2024", "1234567891", None, LocalDate.parse("2024-01-01")),
//        ReliefClaim("XAIS12345678901", None, ReliefClaimType.CF, "2025", "1234567892", None, LocalDate.parse("2024-01-01"))
//      )
//      val newAnswers = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
//
//      val result = connector.updateReliefClaims(testContextWithNino, oldAnswers, newAnswers).value
//
//      result.map { res =>
//        res mustBe Right(oldAnswers)
//      }
//
//      verify(1, postRequestedFor(urlEqualTo(api1505Url)))
//      verify(0, deleteRequestedFor(urlEqualTo(api1506Url)))
//    }
//
//    "delete relief claims correctly" in {
//      val oldAnswers = List(
//        ReliefClaim("XAIS12345678900", None, ReliefClaimType.CF, "2024", "1234567890", None, LocalDate.parse("2024-01-01")),
//        ReliefClaim("XAIS12345678901", None, ReliefClaimType.CSGI, "2025", "1234567890", None, LocalDate.parse("2024-01-01"))
//      )
//      val newAnswers = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
//
//      stubDelete(
//        url = api1506Url,
//        expectedStatus = OK,
//        expectedResponse = ""
//      )
//
//      stubPostWithResponseBody(
//        url = api1505Url,
//        expectedStatus = OK,
//        expectedResponse = newAnswers.toString()
//      )
//
//      val result = connector.updateReliefClaims(testContextWithNino, oldAnswers, newAnswers).value
//
//      result.map { res =>
//        res mustBe Right(List(WhatDoYouWantToDoWithLoss.CarryItForward))
//      }
//
//      verify(1, deleteRequestedFor(urlEqualTo(api1506Url)))
//
//    }
//
//    "handle failure when creating relief claims" in {
//
//      val oldAnswers = claims
//      val newAnswers = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
//
//      stubPostWithResponseBody(
//        url = api1505Url,
//        expectedStatus = INTERNAL_SERVER_ERROR,
//        expectedResponse = "Internal Server Error"
//      )
//
//      stubDelete(
//        url = api1506Url,
//        expectedStatus = OK,
//        expectedResponse = ""
//      )
//
//      val result = connector.updateReliefClaims(testContextWithNino, oldAnswers, newAnswers).value
//
//      result.map { res =>
//        res mustBe a[GenericDownstreamError]
//      }
//
//      verify(1, postRequestedFor(urlEqualTo(api1505Url)))
//    }
//
//    "handle failure when deleting relief claims" in {
//
//      val oldAnswers = claims
//      val newAnswers = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
//
//      stubPostWithResponseBody(
//        url = api1505Url,
//        expectedStatus = OK,
//        expectedResponse = newAnswers.toString()
//      )
//
//      stubDelete(
//        url = api1506Url,
//        expectedStatus = INTERNAL_SERVER_ERROR,
//        expectedResponse = "Internal Server Error"
//      )
//
//      val result = connector.updateReliefClaims(testContextWithNino, oldAnswers, newAnswers).value
//
//      result.map { res =>
//        res mustBe a[GenericDownstreamError]
//      }
//
//      verify(1, deleteRequestedFor(urlEqualTo(api1506Url)))
//    }
//  }
}
