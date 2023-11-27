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
import helpers.WiremockSpec
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import models.frontend.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.CREATED
import play.api.libs.json.Json

class SelfEmploymentBusinessConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  private val someJourneyAnswers = GoodsToSellOrUseJourneyAnswers(100.00, Some(100.00))

  private val downstreamUrl =
    s"/income-tax/${requestData.taxYear.value}/${requestData.nino.value}/self-employments/${requestData.businessId.value}/periodic-summaries"

  private val downstreamSuccessResponse = Json.obj("ibdSubmissionPeriodId" -> "someId")

//  private val downstreamSingleFailureResponse = Json.parse(s"""
//       |{
//       |  "failures": [
//       |    {
//       |      "code": "SOME_CODE",
//       |      "reason": "Some reason."
//       |    }
//       |  ]
//       |}
//       |""".stripMargin)

//  private val downstreamMultipleFailuresResponse = Json.parse(s"""
//       |{
//       |  "failures": [
//       |    {
//       |      "code": "SOME_CODE",
//       |      "reason": "Some reason."
//       |    },
//       |    {
//       |      "code": "SOME_OTHER_CODE",
//       |      "reason": "Some other reason."
//       |    }
//       |  ]
//       |}
//       |""".stripMargin)

  private val connector = new SelfEmploymentBusinessConnector(httpClient, appConfig)

  "Downstream returns a success response" must {
    "return the submission id" in {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = someJourneyAnswers,
        expectedResponse = downstreamSuccessResponse.toString,
        expectedStatus = CREATED)

      val expectedResponse = CreateSEPeriodSummaryResponse("someId")

      connector.createSEPeriodSummary(requestData, someJourneyAnswers).futureValue shouldBe expectedResponse.asRight
    }
    // FIXME - Reinstate tests when we know what our IFS response will look like - currently what is in our `DownstreamParser` for error handling looks wrong.
    //
    //  "Downstream returns a single error" must {
    //    "return the error" in {
    //      stubPostWithRequestAndResponseBody(
    //        url = downstreamUrl,
    //        requestBody = someJourneyAnswers,
    //        expectedResponse = downstreamSingleFailureResponse.toString,
    //        expectedStatus = BAD_REQUEST)
    //
    //      val expectedError = SingleDownstreamError(BAD_REQUEST, SingleDownstreamErrorBody.parsingError)
    //
    //      connector.createSEPeriodSummary(requestData, someJourneyAnswers).futureValue shouldBe expectedError.asLeft
    //    }
    //  }
    //  "Downstream returns multiple errors" must {
    //    "return them" in {
    //      stubPostWithRequestAndResponseBody(
    //        url = downstreamUrl,
    //        requestBody = someJourneyAnswers,
    //        expectedResponse = downstreamMultipleFailuresResponse.toString,
    //        expectedStatus = BAD_REQUEST)
    //
    //      val expectedError = MultipleDownstreamErrors(BAD_REQUEST, MultipleDownstreamErrorBody(Seq(SingleDownstreamErrorBody.parsingError)))
    //
    //      connector.createSEPeriodSummary(requestData, someJourneyAnswers).futureValue shouldBe expectedError.asLeft
    //    }
    //  }

  }
}
