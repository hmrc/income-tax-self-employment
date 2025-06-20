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
import models.connector.api_2085.ListOfIncomeSources
import models.error.DownstreamError
import org.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.await
import testdata.CommonTestData
import utils.{MockIdGenerator, MockTimeMachine}

import java.time.OffsetDateTime

class IncomeSourceConnectorISpec extends IntegrationBaseSpec
  with CommonTestData
  with MockitoSugar
  with MockTimeMachine
  with MockIdGenerator {

  val connector = new IncomeSourcesConnector(
    httpClientV2,
    appConfig,
    mockTimeMachine,
    mockIdGenerator
  )

  val url = s"/itsd/income-sources/v2/$testNino"
  val fixedTime: OffsetDateTime = OffsetDateTime.parse("2025-04-30T15:00:00+01:00")

  "getIncomeSources" should {
    "return a list of income sources when the API returns 200 OK" in {
      stubGetWithResponseBody(
        url = url,
        expectedStatus = OK,
        expectedResponse = Json.stringify(Json.toJson(testListOfIncomeSources)),
      )
      mockNow(fixedTime)
      mockCorrelationId(testCorrelationId)

      val result = await(connector.getIncomeSources(testNino).value)

      result mustBe Right(testListOfIncomeSources)
    }

    "return an empty list when the API returns 404 NOT FOUND" in {
      stubGetWithoutResponseBody(
        url = url,
        expectedStatus = NOT_FOUND
      )
      mockNow(fixedTime)
      mockCorrelationId(testCorrelationId)

      val result = await(connector.getIncomeSources(testNino).value)

      result mustBe Right(ListOfIncomeSources(Nil))
    }

    Seq(
      BAD_REQUEST,
      UNAUTHORIZED,
      INTERNAL_SERVER_ERROR,
      BAD_GATEWAY,
      SERVICE_UNAVAILABLE
    ).foreach { status =>
      s"return Left(DownstreamError) for error status $status" in {
        stubGetWithoutResponseBody(
          url = url,
          expectedStatus = status
        )
        mockNow(fixedTime)
        mockCorrelationId(testCorrelationId)

        val result = await(connector.getIncomeSources(testNino).value)

        result.isLeft mustBe true
        result.merge mustBe a[DownstreamError]
      }
    }

  }

}
