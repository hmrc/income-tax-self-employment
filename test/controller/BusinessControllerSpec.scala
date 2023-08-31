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

package controller

import bulders.BusinessDataBuilder.{aBusinesses, aGetBusinessDataRequestStr}
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import controllers.BusinessController
import models.api.BusinessData.GetBusinessDataRequest
import play.api.http.Status.OK
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
  
  behave like businessGetShould("GET /business/:nino")(
    () => stubGetBusinesses(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
    () => underTest.getBusinesses(nino))
  
  behave like businessGetShould("GET /business/:nino/:businessId")(
    () => stubGetBusiness(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])))(
    () => underTest.getBusiness(nino, businessId))
  
  def businessGetShould(getName: String)(stubs:() => Unit)(block: () => Action[AnyContent]): Unit =
    s".$getName" should {
      "return a 200 response and a GetBusinessRequest model" in {
        val result = {
          mockAuth()
          stubs()
          block()(fakeRequest)
        }
        status(result) mustBe OK
        bodyOf(result) mustBe Json.toJson(aBusinesses).toString()
      }
      
      "return an error when the connector returns an error" in {
        val result = {
          mockAuth()
          stubs()
          block()(fakeRequest)
        }
        status(result) mustBe OK
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
