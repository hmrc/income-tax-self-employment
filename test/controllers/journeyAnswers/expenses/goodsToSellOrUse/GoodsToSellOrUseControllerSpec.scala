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
import play.api.http.Status.BAD_REQUEST
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

  "incoming request is json" when {
    "service returns a success response" must {
      "return a 204" in {
        mockService
          .createSEPeriodSummary(eqTo(requestData), eqTo(expectedAnswers))(*, *) returns Future.successful(().asRight)

        val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequestWithAnswers)

        status(result) shouldBe 204
      }
    }
    "service returns a single downstream error" must {
      "return the error" in {
        mockService
          .createSEPeriodSummary(eqTo(requestData), eqTo(expectedAnswers))(*, *) returns Future.successful(singleDownstreamError.asLeft)

        val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequestWithAnswers)

        val expectedJson = Json.parse(s"""
             |{
             |  "status": 400,
             |  "body": {
             |    "code": "FORMAT_NINO",
             |    "reason": "Submission has not passed validation. Invalid parameter NINO.",
             |    "errorType": "DOMAIN_ERROR_CODE"
             |  }
             |}
             |""".stripMargin)

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedJson
      }
    }
    "service returns multiple downstream errors" must {
      "return all errors" in {
        mockService
          .createSEPeriodSummary(eqTo(requestData), eqTo(expectedAnswers))(*, *) returns Future.successful(multipleDownstreamErrors.asLeft)

        val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequestWithAnswers)

        val expectedJson = Json.parse(s"""
             |{
             |  "status": 400,
             |  "body": {
             |    "failures": [
             |      {
             |        "code": "FORMAT_NINO",
             |        "reason": "Submission has not passed validation. Invalid parameter NINO.",
             |        "errorType": "DOMAIN_ERROR_CODE"
             |      },
             |      {
             |        "code": "INTERNAL_SERVER_ERROR",
             |        "reason": "Submission has not passed validation. Invalid parameter MTDID.",
             |        "errorType": "DOMAIN_ERROR_CODE"
             |       }
             |    ]
             |  }
             |}
             |""".stripMargin)

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe expectedJson
      }
    }
  }
  "incoming request does not have a json payload" must {
    "return a 500" in {
      val result = controller.handleRequest(taxYear, businessId, nino)(fakeRequest)

      status(result) shouldBe BAD_REQUEST
    }
  }

}
