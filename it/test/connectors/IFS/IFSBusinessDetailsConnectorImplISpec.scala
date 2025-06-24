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

package connectors.IFS

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import connectors.data._
import models.common.JourneyContextWithNino
import models.connector.api_2085.ListOfIncomeSources
import models.error.DownstreamError.GenericDownstreamError
import models.error.ServiceError
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import play.api.test.Helpers.await

class IFSBusinessDetailsConnectorImplISpec extends IntegrationBaseSpec {

  val connector                   = new IFSBusinessDetailsConnectorImpl(httpClientV2, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdItId, testNino)

  "getBusinesses" must {
    "return successful response" ignore new Api1171Test {
      // TODO: Implement feature switching functionality so we can enable/disable switches in tests
//      stubGetWithResponseBody(
//        url = downstreamUrl,
//        expectedResponse = api1171IfsResponseJson,
//        expectedStatus = OK
//      )
//      connector.getBusinesses(testNino).value.futureValue shouldBe Right(api1171IfsResponse)
    }
  }

  "getBusinessIncomeSourcesSummary" must {
    "return successful response" in new Api1871Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = api1871ResponseJson,
        expectedStatus = OK
      )
      await(connector.getBusinessIncomeSourcesSummary(testTaxYear, testNino, testBusinessId).value) shouldBe Right(api1871Response)
    }
  }

  "createBroughtForwardLoss" must {
    "return successful response" in new Api1500Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = api1500ResponseJson,
        expectedStatus = OK
      )
      await(connector.createBroughtForwardLoss(data).value) shouldBe api1500Response.asRight
    }
  }

  "updateBroughtForwardLoss" must {
    "return successful response" in new Api1501Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = api1501ResponseJson,
        expectedStatus = OK
      )
      await(connector.updateBroughtForwardLoss(data).value) shouldBe api1501Response.asRight
    }
  }

  "getBroughtForwardLoss" must {
    "return successful response" in new Api1502Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = api1502ResponseJson,
        expectedStatus = OK
      )
      await(connector.getBroughtForwardLoss(testNino, testBusinessId.value).value) shouldBe api1502Response.asRight
    }
  }

  "listBroughtForwardLoss" must {
    "return successful response" in new Api1870Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = api1870ResponseJson,
        expectedStatus = OK
      )
      await(connector.listBroughtForwardLosses(testNino, testTaxYear).value) shouldBe api1870Response.asRight
    }
  }

  "listOfIncomeSources" must {
    "return successful response" in new Api2085Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = api2085ResponseJson,
        expectedStatus = OK
      )

      await(connector.getListOfIncomeSources(testTaxYear, testNino).value) shouldBe api2085Response.asRight
    }

    for (errorStatus <- Seq(BAD_REQUEST, NOT_FOUND, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR))
      s"returns $errorStatus GenericDownstreamError when expected status is $errorStatus" in new Api2085Test {
        stubGetWithResponseBody(
          url = downstreamUrl,
          expectedResponse = api2085FailedResponse,
          expectedStatus = errorStatus
        )

        val result: Either[ServiceError, ListOfIncomeSources] = await(connector.getListOfIncomeSources(testTaxYear, testNino).value)
        result match {
          case Left(GenericDownstreamError(status, message)) =>
            status shouldBe errorStatus
            message should include(
              s"Downstream error when calling GET http://localhost:11111/income-tax/income-sources/$testNino?taxYear=${testTaxYear.toYYYY_YY}")
            message should include(s"status=$errorStatus")
            message should include(s"body:\n$api2085FailedResponse")
          case _ => fail("Expected a GenericDownstreamError")
        }
      }
  }
}
