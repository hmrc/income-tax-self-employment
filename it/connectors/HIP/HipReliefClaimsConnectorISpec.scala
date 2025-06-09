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

package connectors.HIP

import base.IntegrationBaseSpec
import com.github.tomakehurst.wiremock.client.WireMock._
import models.common.JourneyContextWithNino
import models.connector.ReliefClaimType
import models.connector.api_1505.ClaimId
import models.error.DownstreamError.GenericDownstreamError
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.await
import testdata.CommonTestData

class HipReliefClaimsConnectorISpec extends IntegrationBaseSpec with CommonTestData {

  val connector                   = new HipReliefClaimsConnector(httpClientV2, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdItId, testNino)

  val api1505Url: String = s"/itsd/income-sources/claims-for-relief/${testNino.value}"

  "createReliefClaim" should {

    "call HIP API 1505 once to create a relief claim for 1 checkbox" in {
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

          stubPostWithResponseBody(
            url = api1505Url,
            expectedStatus = status,
            expectedResponse = Json.stringify(response)
          )

          val result = await(connector.createReliefClaim(testContextWithNino, ReliefClaimType.CF).value)

          result.isLeft mustBe true
          result.merge mustBe a[GenericDownstreamError]
        }
      }
    }
  }
}
