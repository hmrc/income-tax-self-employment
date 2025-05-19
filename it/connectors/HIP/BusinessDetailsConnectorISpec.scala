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
import com.github.tomakehurst.wiremock.http.HttpHeader
import connectors.data.Api1171Test
import models.connector.businessDetailsConnector.BusinessDetailsHipSuccessWrapper
import models.error.DownstreamError.GenericDownstreamError
import models.error.ServiceError
import org.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.await
import testdata.CommonTestData
import utils.{MockIdGenerator, MockTimeMachine}

import java.time.OffsetDateTime

class BusinessDetailsConnectorISpec extends IntegrationBaseSpec with CommonTestData with MockitoSugar with MockTimeMachine with MockIdGenerator {

  val fixedTime: OffsetDateTime = OffsetDateTime.parse("2025-04-30T15:00:00+01:00")
  mockNow(fixedTime)
  mockCorrelationId(testCorrelationId)

  val connector = new BusinessDetailsConnector(
    httpClientV2,
    appConfig,
    mockTimeMachine,
    mockIdGenerator
  )

  val baseUrl = s"/RESTAdapter/itsa/taxpayer/business-details"

  val api1171Url = s"$baseUrl\\?incomeSourceId=$testBusinessId&mtdReference=$testMtdItId&nino=$testNino"

  val additionalHeaders: Seq[HttpHeader] = Seq(
    new HttpHeader("correlationid", testCorrelationId),
    new HttpHeader("X-Message-Type", "TaxpayerDisplay"),
    new HttpHeader("X-Originating-System", "MDTP"),
    new HttpHeader("X-Receipt-Date", fixedTime.toString),
    new HttpHeader("X-Regime-Type", "ITSA"),
    new HttpHeader("X-Transmitting-System", "HIP")
  )

  "getBusinessDetails" should {
    "return Right when the API returns 200 OK" in new Api1171Test {
      stubGetWithResponseBody(
        url = api1171Url,
        expectedStatus = OK,
        expectedResponse = api1171HipResponseJson,
        requestHeaders = additionalHeaders
      )

      val result: Either[ServiceError, Option[BusinessDetailsHipSuccessWrapper]] =
        await(connector.getBusinessDetails(Some(testBusinessId), testMtdItId, testNino).value)

      result mustBe Right(Some(api1171HipResponse))
    }

    "Return Right when the API returns 404 NOT FOUND" in {
      stubGetWithoutResponseBody(
        url = api1171Url,
        expectedStatus = NOT_FOUND
      )

      val result: Either[ServiceError, Option[BusinessDetailsHipSuccessWrapper]] =
        await(connector.getBusinessDetails(Some(testBusinessId), testMtdItId, testNino).value)

      result mustBe Right(None)
    }

    Seq(
      ("BadRequest", BAD_REQUEST),
      ("Unauthorized", UNAUTHORIZED),
      ("Forbidden", FORBIDDEN),
      ("Unsupported mediaType", UNSUPPORTED_MEDIA_TYPE),
      ("Resource not found", UNPROCESSABLE_ENTITY),
      ("Server Error", INTERNAL_SERVER_ERROR),
      ("Server unavailable", SERVICE_UNAVAILABLE)
    ) foreach { case (statusStr, status) =>
      s"return failure when downstream fails with $statusStr" in new Api1171Test {
        stubGetWithResponseBody(
          url = api1171Url,
          expectedStatus = status,
          expectedResponse = "",
          requestHeaders = additionalHeaders
        )

        val result: Either[ServiceError, Option[BusinessDetailsHipSuccessWrapper]] =
          await(connector.getBusinessDetails(Some(testBusinessId), testMtdItId, testNino).value)

        result.isLeft mustBe true
        result.merge mustBe a[GenericDownstreamError]
      }
    }
  }
}
