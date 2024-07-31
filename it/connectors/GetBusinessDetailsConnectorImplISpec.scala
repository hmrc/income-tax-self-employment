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

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import connectors.data.{Api1171Test, CitizenDetailsTest}
import helpers.WiremockSpec
import models.common.{IdType, JourneyContextWithNino}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.OK
import utils.BaseSpec._

class GetBusinessDetailsConnectorImplISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector                   = new GetBusinessDetailsConnectorImpl(httpClient, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(taxYear, businessId, mtditid, nino)

  "getBusinesses" must {
    "return successful response" in new Api1171Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBusinesses(IdType.Nino, nino.value).futureValue shouldBe successResponse.asRight
    }
  }

  "getCitizenDetails" must {
    "return successful response" in new CitizenDetailsTest {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getCitizenDetails(nino).futureValue shouldBe successResponse.asRight
    }
  }

}
