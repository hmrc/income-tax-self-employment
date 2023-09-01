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

package controllers

import bulders.BusinessDataBuilder.{aBusinesses, aGetBusinessDataRequestStr}
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import controllers.BusinessController
import models.api.BusinessData.GetBusinessDataRequest
import models.error.APIErrorBody.{APIError, APIStatusError}
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class BusinessControllerSpec extends TestUtils {
  val mockBusinessService = mock[BusinessService]
  val underTest = new BusinessController(mockBusinessService, mockAuthorisedAction, mockControllerComponents)

  val nino = "FI290077A"
  val businessId = "SJPR05893938418"

  "GET /business" should {
    behave like businessRequestReturnsOk(
      () => stubGetBusinesses(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
      () => underTest.getBusinesses(nino))

    behave like businessRequestReturnsError(
      () => stubGetBusinesses(Left(APIStatusError(BAD_REQUEST, APIError("INVALID_NINO",
        "Submission has not passed validation. Invalid parameter  NINO")))))(
      () => underTest.getBusinesses(nino))
  }

  "GET /business/:nino" should {
    behave like businessRequestReturnsOk(
      () => stubGetBusiness(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
      () => underTest.getBusiness(nino, businessId))

    behave like businessRequestReturnsError(
      () => stubGetBusiness(Left(APIStatusError(BAD_REQUEST, APIError("INVALID_NINO",
        "Submission has not passed validation. Invalid parameter  NINO")))))(
      () => underTest.getBusiness(nino, businessId))
  }


  def businessRequestReturnsOk(stubs: () => Unit)(block: () => Action[AnyContent]): Unit =
    "return a 200 response and a GetBusinessRequest model" in {
      val result = {
        mockAuth()
        stubs()
        block()(fakeRequest)
      }
      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(aBusinesses).toString()
    }

  def businessRequestReturnsError(stubs: () => Unit)(block: () => Action[AnyContent]): Unit =
    "return an error when the connector returns an error" in {
      val result = {
        mockAuth()
        stubs()
        block()(fakeRequest)
      }
      status(result) mustBe BAD_REQUEST
    }

  def stubGetBusinesses(expectedResult: GetBusinessesResponse): Unit =
    (mockBusinessService.getBusinesses(_: String)(_: HeaderCarrier))
      .expects(nino, *)
      .returning(Future.successful(expectedResult))

  def stubGetBusiness(expectedResult: GetBusinessesResponse): Unit =
    (mockBusinessService.getBusiness(_: String, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, businessId, *, *)
      .returning(Future.successful(expectedResult))

}
