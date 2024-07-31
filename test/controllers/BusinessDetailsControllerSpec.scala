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

import bulders.BusinessDataBuilder._
import models.common.{BusinessId, Nino}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody.{invalidNino, notFound, serverError, serviceUnavailable}
import play.api.http.Status._
import play.api.libs.json.Json
import services.BusinessService
import stubs.services.StubBusinessService

import java.time.LocalDate

class BusinessDetailsControllerSpec extends ControllerBehaviours {

  def underTest(stubbedService: BusinessService) = new BusinessDetailsController(stubbedService, mockAuthorisedAction, stubControllerComponents)

  val nino       = Nino("FI290077A")
  val businessId = BusinessId("SJPR05893938418")
  val taxYear    = LocalDate.now.getYear

  s"GET /individuals/business/details/$nino/list" should {
    val service = StubBusinessService(getBusinessesRes = Right(aBusinesses))

    behave like controllerSpec(
      OK,
      Json.toJson(aBusinesses).toString(),
      () => underTest(service).getBusinesses(nino)
    )

    for {
      (errorStatus, apiError) <- Seq(
        (NOT_FOUND, notFound),
        (BAD_REQUEST, invalidNino),
        (INTERNAL_SERVER_ERROR, serverError),
        (SERVICE_UNAVAILABLE, serviceUnavailable))
      error        = SingleDownstreamError(errorStatus, apiError)
      errorService = StubBusinessService(getBusinessesRes = Left(error))
    }
      behave like controllerSpec(
        errorStatus,
        Json.toJson(SingleDownstreamError(errorStatus, apiError.toDomain)).toString(),
        () => underTest(errorService).getBusinesses(nino)
      )
  }

  s"GET /individuals/business/details/$nino/$businessId" should {
    val service = StubBusinessService(getBusinessRes = Right(aBusinesses))

    behave like controllerSpec(
      OK,
      Json.toJson(aBusinesses).toString(),
      () => underTest(service).getBusiness(nino, businessId)
    )

    for {
      (errorStatus, apiError) <- Seq(
        (NOT_FOUND, notFound),
        (BAD_REQUEST, invalidNino),
        (INTERNAL_SERVER_ERROR, serverError),
        (SERVICE_UNAVAILABLE, serviceUnavailable))
      error        = SingleDownstreamError(errorStatus, apiError)
      errorService = StubBusinessService(getBusinessRes = Left(error))
    }
      behave like controllerSpec(
        errorStatus,
        Json.toJson(SingleDownstreamError(errorStatus, apiError.toDomain)).toString(),
        () => underTest(errorService).getBusiness(nino, businessId)
      )
  }
}
