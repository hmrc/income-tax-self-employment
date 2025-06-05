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
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.await
import testdata.CommonTestData

class HipReliefClaimsConnectorISpec extends IntegrationBaseSpec with CommonTestData {

  val connector                   = new HipReliefClaimsConnector(httpClientV2, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdItId, testNino)

  val api1505Url: String = s"/income-sources/claims-for-relief/${testNino.value}"

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

  }

}
