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
import play.api.http.Status._
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.error.DownstreamError.GenericDownstreamError
import models.error.ServiceError
import org.mockito.MockitoSugar
import play.api.http.Status.{NOT_FOUND, OK}
import testdata.CommonTestData
import utils.MockTimeMachine

import java.time.OffsetDateTime

class BusinessDetailsConnectorISpec extends IntegrationBaseSpec with CommonTestData with MockitoSugar with MockTimeMachine {

  val fixedTime: OffsetDateTime = OffsetDateTime.parse("2025-04-30T15:00:00+01:00")
  mockNow(fixedTime)

  val connector = new BusinessDetailsConnectorImpl(
    httpClient,
    appConfig,
    mockTimeMachine
  )

  val api1171Url: String =
    s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?incomeSourceId=$testBusinessId&mtdReference=$testMtdItId&nino=$testNino"

  val additionalHeaders: Seq[HttpHeader] = Seq(
    new HttpHeader("X-Message-Type", "TaxpayerDisplay"),
    new HttpHeader("X-Originating-System", "MDTP"),
    new HttpHeader("X-Receipt-Date", fixedTime.toString),
    new HttpHeader("X-Regime-Type", "ITSA"),
    new HttpHeader("X-Transmitting-System", "HIP")
  )

  "getBusinessDetails" when {
    "the API returns 200 OK" in new Api1171Test {
      stubGetWithResponseBody(
        url = api1171Url,
        expectedStatus = OK,
        expectedResponse = successResponseRaw,
        requestHeaders = additionalHeaders
      )

      val result: Either[ServiceError, BusinessDetailsSuccessResponseSchema] =
        connector.getBusinessDetails(testBusinessId, testMtdItId, testNino).value.futureValue

      result mustBe Right(successResponse)
    }

    Seq(
      ("BadRequest", BAD_REQUEST),
      ("Unauthorized", UNAUTHORIZED),
      ("Forbidden", FORBIDDEN),
      ("NotFound", NOT_FOUND),
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

        val result: Either[ServiceError, BusinessDetailsSuccessResponseSchema] =
          connector.getBusinessDetails(testBusinessId, testMtdItId, testNino).value.futureValue

        result.isLeft mustBe true
        result.merge mustBe a[GenericDownstreamError]
      }
    }
  }
}
