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
import models.error.ApiError.ApiErrorBody.{data404, ifsServer500, nino400, service503}
import models.error.ApiError.ApiStatusError
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class BusinessControllerSpec extends ControllerBehavioursSpec {
  val mockBusinessService = MockitoSugar.mock[BusinessService]
  val underTest = new BusinessController(mockBusinessService, mockAuthorisedAction, mockControllerComponents)

  val nino = "FI290077A"
  val businessId = "SJPR05893938418"

  s"GET /individuals/business/details/$nino/list" should {
    behave like controllerSpec(OK, Json.toJson(aBusinesses).toString(),
      () => stubGetBusinesses(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])),
      () => underTest.getBusinesses(nino))

    for ((errorStatus, apiError) <- Seq(
      (NOT_FOUND, data404), (BAD_REQUEST, nino400),
      (INTERNAL_SERVER_ERROR, ifsServer500), (SERVICE_UNAVAILABLE, service503))) {
      
      behave like controllerSpec(errorStatus, Json.toJson(ApiStatusError(errorStatus, apiError.toMdtpError)).toString(),
        () => stubGetBusinesses(Left(ApiStatusError(errorStatus, apiError))),
        () => underTest.getBusinesses(nino))
    }
  }

  s"GET /individuals/business/details/$nino/$businessId" should {
    behave like controllerSpec(OK, Json.toJson(aBusinesses).toString(),
      () => stubGetBusiness(Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])),
      () => underTest.getBusiness(nino, businessId))
    
     for ((errorStatus, apiError) <- Seq(
      (NOT_FOUND, data404), (BAD_REQUEST, nino400),
      (INTERNAL_SERVER_ERROR, ifsServer500), (SERVICE_UNAVAILABLE, service503))) {
      
       behave like controllerSpec(errorStatus, Json.toJson(ApiStatusError(errorStatus, apiError.toMdtpError)).toString(),
         () => stubGetBusiness(Left(ApiStatusError(errorStatus, apiError))),
         () => underTest.getBusiness(nino, businessId))
     }
  }

  def stubGetBusinesses(expectedResult: GetBusinessesResponse): Unit =
    when(mockBusinessService.getBusinesses(meq(nino))(any[HeaderCarrier])) thenReturn Future.successful(expectedResult)

  def stubGetBusiness(expectedResult: GetBusinessesResponse): Unit =
    when(mockBusinessService.getBusiness(meq(nino), meq(businessId))(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.successful(expectedResult)

}
