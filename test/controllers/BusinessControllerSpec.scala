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
import models.api.BusinessData.GetBusinessDataRequest
import models.error.APIErrorBody.APIError.{data404, ifsServer500, nino400, service503}
import models.error.APIErrorBody.APIStatusError
import play.api.http.Status._
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

  s"GET /individuals/business/details/$nino/list" should {
    behave like businessRequestReturnsOk(
      () => stubGetBusinesses(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
      () => underTest.getBusinesses(nino))

    behave like businessRequestReturnsError( stubGetBusinesses )(
      () => underTest.getBusinesses(nino))
  }

  s"GET /individuals/business/details/$nino/$businessId" should {
    behave like businessRequestReturnsOk(
      () => stubGetBusiness(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
      () => underTest.getBusiness(nino, businessId))
    
    behave like businessRequestReturnsError( stubGetBusiness )(
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
  
  def businessRequestReturnsError(stubs: GetBusinessesResponse => Unit)(block: () => Action[AnyContent]): Unit =
    "return an error" when {
      for ((errorStatus, apiError) <- Seq(
        (NOT_FOUND, data404), (BAD_REQUEST, nino400),
        (INTERNAL_SERVER_ERROR, ifsServer500), (SERVICE_UNAVAILABLE, service503))) {
        
        s"the connector returns a ${apiError.code} error" in {
          val result = {
            mockAuth()
            stubs(Left(APIStatusError(errorStatus, apiError)))
            block()(fakeRequest)
          }
          status(result) mustBe errorStatus
          bodyOf(result) mustBe APIStatusError(errorStatus, apiError.toMdtpError).toJson.toString()
        }
      }
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
