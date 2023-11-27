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

package controllers.journeyAnswers.expenses.goodsToSellOrUse

import cats.implicits.catsSyntaxEitherId
import mocks.MockAuth
import models.frontend.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import services.journeyAnswers.expenses.goodsToSellOrUse.SelfEmploymentBusinessService

import scala.concurrent.Future

class GoodsToSellOrUseControllerSpec extends MockAuth {

  private val mockService = mock[SelfEmploymentBusinessService]

  private val fakeRequestWithAnswers =
    fakeRequest.withJsonBody(Json.obj("goodsToSellOrUseAmount" -> 100.00, "disallowableGoodsToSellOrUseAmount" -> 100.00))

  private val expectedAnswers = GoodsToSellOrUseJourneyAnswers(100.00, Some(100.00))

  private val controller = new GoodsToSellOrUseController(mockService, mockAuthorisedAction, stubControllerComponents)

  "service returns a success response" must {
    "return a 204" in {
      mockService
        .createSEPeriodSummary(eqTo(requestData), eqTo(expectedAnswers))(*, *) returns Future.successful(().asRight)

      val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequestWithAnswers)

      status(result) shouldBe 204
    }
  }
  "service returns an error from downstream" must {
    "return the error status and code" in {
      mockService
        .createSEPeriodSummary(eqTo(requestData), eqTo(expectedAnswers))(*, *) returns Future.successful(someDownstreamError.asLeft)

      val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequestWithAnswers)

      val expectedJson = Json.parse(s"""
               |{
               |  "status": $INTERNAL_SERVER_ERROR,
               |  "body": {
               |    "code": "INTERNAL_SERVER_ERROR",
               |    "reason": "some reason",
               |    "errorType":"DOMAIN_ERROR_CODE"
               |  }
               |}
               |""".stripMargin)

      status(result) shouldBe INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe expectedJson
    }
  }

}
