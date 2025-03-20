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
import models.common.TaxYear.asTys
import models.connector.ReliefClaimType.{CF, CSGI}
import models.connector._
import models.connector.api_1505.ClaimId
import models.connector.common.ReliefClaim
import models.error.DownstreamError.GenericDownstreamError
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers.logger
import play.api.http.Status._
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.await
import testdata.CommonTestData
import uk.gov.hmrc.http.HttpReads

import java.time.LocalDateTime

class ReliefClaimsConnectorISpec extends IntegrationBaseSpec with CommonTestData {

  val connector: ReliefClaimsConnector = app.injector.instanceOf[ReliefClaimsConnector]

  val api1505Url: String = s"/income-tax/claims-for-relief/${testNino.value}/${asTys(testTaxYear)}"
  val api1507Url: String = s"/income-tax/claims-for-relief/${testNino.value}"
  val api1867Url: String = s"/income-tax/${testTaxYear2024.endYear}/claims-for-relief/${testNino.value}"

  implicit val reads: HttpReads[ApiResponse[ClaimId]] = lossClaimReads[ClaimId]

  val selfEmploymentClaim: JsObject = Json.obj(
    "incomeSourceId"    -> "XAIS12345678901",
    "reliefClaimed"     -> "01",
    "reliefClaimed"     -> "CF",
    "taxYearClaimedFor" -> "2024",
    "claimId"           -> "1234567890",
    "submissionDate"    -> "2024-01-01T12:13:48.763Z"
  )

  val testBaseReliefClaim: ReliefClaim = ReliefClaim(
    incomeSourceId = "XAIS12345678901",
    incomeSourceType = None,
    reliefClaimed = ReliefClaimType.CF,
    taxYearClaimedFor = "2024",
    claimId = "1234567890",
    submissionDate = LocalDateTime.parse("2024-01-01T12:13:48.763")
  )

  val claims: List[ReliefClaim] = List(
    ReliefClaim("XAIS12345678900", None, CF, "2025", "12345", None, LocalDateTime.parse("2024-10-01T12:13:48.763")),
    ReliefClaim("XAIS12345678901", None, CSGI, "2024", "1234567890", None, LocalDateTime.parse("2024-10-01T12:13:48.763"))
  )

  val expectedClaims: List[ReliefClaim] = List(
    ReliefClaim("XAIS12345678901", None, CF, "2024", "1234567890", None, LocalDateTime.parse("2024-01-01T12:13:48.763")),
    ReliefClaim("XAIS12345678901", None, CF, "2025", "1234567890", None, LocalDateTime.parse("2024-01-01T12:13:48.763"))
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

        val result = connector.getAllReliefClaims(testContextWithNino).value.futureValue

        result mustBe Right(List(testBaseReliefClaim))
        result.map(_.head.isSelfEmploymentClaim) mustBe Right(true)
      }
    }

    "call API1507 and return all claims" in {
      val selfEmploymentClaim2025 = selfEmploymentClaim + ("taxYearClaimedFor" -> Json.toJson("2025"))
      val response                = Json.arr(selfEmploymentClaim, selfEmploymentClaim2025)

      stubGetWithResponseBody(
        url = api1507Url,
        expectedStatus = OK,
        expectedResponse = Json.stringify(response)
      )

      val result = connector.getAllReliefClaims(testContextWithNino).value.futureValue

      result mustBe Right(expectedClaims)

    }

    "the API returns 404 NOT_FOUND" should {
      "return a Left with an error message" in {
        val response: JsObject = Json.obj("failures" -> Json.obj("code" -> "NOT_FOUND", "reason" -> "Resource not found"))

        stubGetWithResponseBody(
          url = api1867Url,
          expectedStatus = NOT_FOUND,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getAllReliefClaims(testContextWithNino).value.futureValue

        result mustBe Right(Nil)
      }
    }

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
                "code"   -> code,
                "reason" -> reason
              )
            )
          )

          stubGetWithResponseBody(
            url = api1507Url,
            expectedStatus = status,
            expectedResponse = Json.stringify(response)
          )

          val result = connector.getAllReliefClaims(testContextWithNino).value.futureValue

          result.isLeft mustBe true
          result.merge mustBe a[GenericDownstreamError]
        }
      }
    }
  }

  "createLossClaims" should {

    "call API 1505 once to create a relief claim for 1 checkbox" in {
      val expectedResponse = ClaimId(claimId = testClaimId)

      stubPostWithResponseBody(
        url = api1505Url,
        expectedStatus = OK,
        expectedResponse = Json.stringify(Json.toJson(expectedResponse))
      )

      val result = await(connector.createReliefClaim(testContextWithNino, ReliefClaimType.CF).value)
      result mustBe Right(expectedResponse)

      verify(1, postRequestedFor(urlEqualTo(api1505Url)))
    }

  }
}
