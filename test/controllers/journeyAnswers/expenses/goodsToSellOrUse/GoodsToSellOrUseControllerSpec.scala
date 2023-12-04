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

import mocks.MockAuth

class GoodsToSellOrUseControllerSpec extends MockAuth {

  // TODO Move to JourneyAnswersControllerSpec unhappy path
//
//  private val mockService = mock[SelfEmploymentBusinessService]
//
//  private val controller = new GoodsToSellOrUseController(mockService, mockAuthorisedAction, stubControllerComponents)
//
//  "incoming request is json" when {
//    "service returns a success response" must {
//      "return a 204" in new Test {
//        mockService
//          .createSEPeriodSummary(eqTo(expectedData))(*) returns Future.successful(().asRight)
//
//        val result: Future[Result] =
//          controller.handleRequest(taxYear, businessId, nino)(fakeRequest.withJsonBody(requestJson))
//
//        status(result) shouldBe 204
//      }
//    }
//    "service returns a single downstream error" must {
//      "return the error" in new Test {
//        mockService
//          .createSEPeriodSummary(eqTo(expectedData))(*) returns Future.successful(singleDownstreamError.asLeft)
//
//        val result: Future[Result] =
//          controller.handleRequest(taxYear, businessId, nino)(fakeRequest.withJsonBody(requestJson))
//
//        val expectedJson: JsValue = Json.parse(s"""
//             |{
//             |  "status": 400,
//             |  "body": {
//             |    "code": "FORMAT_NINO",
//             |    "reason": "Submission has not passed validation. Invalid parameter NINO.",
//             |    "errorType": "DOMAIN_ERROR_CODE"
//             |  }
//             |}
//             |""".stripMargin)
//
//        status(result) shouldBe BAD_REQUEST
//        contentAsJson(result) shouldBe expectedJson
//      }
//    }
//    "service returns multiple downstream errors" must {
//      "return all errors" in new Test {
//        mockService
//          .createSEPeriodSummary(eqTo(expectedData))(*) returns Future.successful(multipleDownstreamErrors.asLeft)
//
//        val result: Future[Result] =
//          controller.handleRequest(taxYear, businessId, nino)(fakeRequest.withJsonBody(requestJson))
//
//        val expectedJson: JsValue = Json.parse(s"""
//             |{
//             |  "status": 400,
//             |  "body": {
//             |    "failures": [
//             |      {
//             |        "code": "FORMAT_NINO",
//             |        "reason": "Submission has not passed validation. Invalid parameter NINO.",
//             |        "errorType": "DOMAIN_ERROR_CODE"
//             |      },
//             |      {
//             |        "code": "INTERNAL_SERVER_ERROR",
//             |        "reason": "Submission has not passed validation. Invalid parameter MTDID.",
//             |        "errorType": "DOMAIN_ERROR_CODE"
//             |       }
//             |    ]
//             |  }
//             |}
//             |""".stripMargin)
//
//        status(result) shouldBe BAD_REQUEST
//        contentAsJson(result) shouldBe expectedJson
//      }
//    }
//  }
//  "incoming request does not have a json payload" must {
//    "return a 400" in {
//      val result =
//        controller.handleRequest(taxYear, businessId, nino)(fakeRequest)
//
//      status(result) shouldBe BAD_REQUEST
//    }
//  }
//
//  trait Test {
//    protected val requestJson: JsObject = Json.obj("goodsToSellOrUseAmount" -> 100.00, "disallowableGoodsToSellOrUseAmount" -> 100.00)
//
//    protected val expectedBody: CreateSEPeriodSummaryRequestBody = CreateSEPeriodSummaryRequestBody(
//      TaxYear.startDate(taxYear),
//      TaxYear.endDate(taxYear),
//      Some(
//        FinancialsType(
//          None,
//          Some(
//            DeductionsType(
//              Some(SelfEmploymentDeductionsDetailPosNegType(Some(100.00), Some(100.00))),
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None,
//              None))
//        ))
//    )
//
//    protected val expectedData: CreateSEPeriodSummaryRequestData = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, expectedBody)
//  }

}
