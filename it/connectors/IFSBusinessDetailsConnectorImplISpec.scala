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

package connectors

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import connectors.data.{Api1171Test, Api1500Test, Api1501Test, Api1502Test, Api1504Test, Api1870Test, Api1871Test}
import helpers.WiremockSpec
import models.common.JourneyContextWithNino
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{NO_CONTENT, OK}

class IFSBusinessDetailsConnectorImplISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector                   = new IFSBusinessDetailsConnectorImpl(httpClient, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(taxYear, businessId, mtditid, nino)

  "getBusinesses" must {
    "return successful response" in new Api1171Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBusinesses(nino).value.futureValue shouldBe successResponse.asRight
    }
  }

  "getBusinessIncomeSourcesSummary" must {
    "return successful response" in new Api1871Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value.futureValue shouldBe successResponse.asRight
    }
  }

  "createBroughtForwardLoss" must {
    "return successful response" in new Api1500Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.createBroughtForwardLoss(data).value.futureValue shouldBe successResponse.asRight
    }
  }

  "updateBroughtForwardLoss" must {
    "return successful response" in new Api1501Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.updateBroughtForwardLoss(data).value.futureValue shouldBe successResponse.asRight
    }
  }

  "getBroughtForwardLoss" must {
    "return successful response" in new Api1502Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBroughtForwardLoss(nino, lossId).value.futureValue shouldBe successResponse.asRight
    }
  }

  "deleteBroughtForwardLoss" must {
    "return unit with NO_CONTENT status" in new Api1504Test {
      stubDelete(
        url = downstreamUrl,
        expectedResponse = "",
        expectedStatus = NO_CONTENT
      )
      connector.deleteBroughtForwardLoss(nino, lossId).value.futureValue shouldBe Right(())
    }
  }

  "listBroughtForwardLoss" must {
    "return successful response" in new Api1870Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.listBroughtForwardLosses(nino, taxYear).value.futureValue shouldBe successResponse.asRight
    }
  }
}
