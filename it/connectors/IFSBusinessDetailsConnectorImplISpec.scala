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
import connectors.data._
import models.common.JourneyContextWithNino
import models.connector.api_2085.ListOfIncomeSources
import models.error.DownstreamError.GenericDownstreamError
import models.error.ServiceError
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._

class IFSBusinessDetailsConnectorImplISpec extends IntegrationBaseSpec {

  val connector                   = new IFSBusinessDetailsConnectorImpl(httpClient, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdItId, testNino)

  "getBusinesses" must {
    "return successful response" in new Api1171Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBusinesses(testNino).value.futureValue shouldBe successResponse.asRight
    }
  }

  "getBusinessIncomeSourcesSummary" must {
    "return successful response" in new Api1871Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getBusinessIncomeSourcesSummary(testTaxYear, testNino, testBusinessId).value.futureValue shouldBe successResponse.asRight
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
      connector.getBroughtForwardLoss(testNino, testBusinessId.value).value.futureValue shouldBe successResponse.asRight
    }
  }

  "listBroughtForwardLoss" must {
    "return successful response" in new Api1870Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.listBroughtForwardLosses(testNino, testTaxYear).value.futureValue shouldBe successResponse.asRight
    }
  }

  "listOfIncomeSources" must {
    "return successful response" in new Api2085Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )

      connector.getListOfIncomeSources(testTaxYear, testNino).value.futureValue shouldBe successResponse.asRight
    }

    for (errorStatus <- Seq(BAD_REQUEST, NOT_FOUND, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR))
      s"returns $errorStatus GenericDownstreamError when expected status is $errorStatus" in new Api2085Test {
        stubGetWithResponseBody(
          url = downstreamUrl,
          expectedResponse = failedResponse,
          expectedStatus = errorStatus
        )

        val result: Either[ServiceError, ListOfIncomeSources] = connector.getListOfIncomeSources(testTaxYear, testNino).value.futureValue
        result match {
          case Left(GenericDownstreamError(status, message)) =>
            status shouldBe errorStatus
            message should include(
              s"Downstream error when calling GET http://localhost:11111/income-tax/income-sources/$testNino?taxYear=${testTaxYear.toYYYY_YY}")
            message should include(s"status=$errorStatus")
            message should include(s"body:\n$failedResponse")
          case _ => fail("Expected a GenericDownstreamError")
        }
      }
  }
}
