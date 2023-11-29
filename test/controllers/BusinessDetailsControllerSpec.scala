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
import controllers.BusinessDetailsControllerSpec.{stubGetBusiness, stubGetBusinessJourneyStates, stubGetBusinesses}
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody.{notFound, serverError, invalidNino, serviceUnavailable}
import models.error.ServiceError.DatabaseError.MongoError
import models.error.DownstreamError.SingleDownstreamError
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json
import services.BusinessService
import services.BusinessService.{GetBusinessJourneyStatesResponse, GetBusinessResponse}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsControllerSpec extends ControllerBehaviours {
  val mockBusinessService = MockitoSugar.mock[BusinessService]
  val underTest           = new BusinessDetailsController(mockBusinessService, mockAuthorisedAction, stubControllerComponents)

  val nino       = "FI290077A"
  val businessId = "SJPR05893938418"
  val taxYear    = LocalDate.now.getYear

  s"GET /individuals/business/details/$nino/list" should {
    behave like controllerSpec(
      OK,
      Json.toJson(aBusinesses).toString(),
      () => stubGetBusinesses(mockBusinessService, nino, Right(Seq(aBusiness))),
      () => underTest.getBusinesses(nino))

    for ((errorStatus, apiError) <- Seq(
        (NOT_FOUND, notFound),
        (BAD_REQUEST, invalidNino),
        (INTERNAL_SERVER_ERROR, serverError),
        (SERVICE_UNAVAILABLE, serviceUnavailable)))
      behave like controllerSpec(
        errorStatus,
        Json.toJson(SingleDownstreamError(errorStatus, apiError.toDomain)).toString(),
        () => stubGetBusinesses(mockBusinessService, nino, Left(SingleDownstreamError(errorStatus, apiError))),
        () => underTest.getBusinesses(nino)
      )
  }

  s"GET /individuals/business/details/$nino/$businessId" should {
    behave like controllerSpec(
      OK,
      Json.toJson(aBusinesses).toString(),
      () => stubGetBusiness(mockBusinessService, nino, businessId, Right(Seq(aBusiness))),
      () => underTest.getBusiness(nino, businessId)
    )

    for ((errorStatus, apiError) <- Seq(
        (NOT_FOUND, notFound),
        (BAD_REQUEST, invalidNino),
        (INTERNAL_SERVER_ERROR, serverError),
        (SERVICE_UNAVAILABLE, serviceUnavailable)))
      behave like controllerSpec(
        errorStatus,
        Json.toJson(SingleDownstreamError(errorStatus, apiError.toDomain)).toString(),
        () => stubGetBusiness(mockBusinessService, nino, businessId, Left(SingleDownstreamError(errorStatus, apiError))),
        () => underTest.getBusiness(nino, businessId)
      )
  }

  s"GET /individuals/business/journeyStates/$nino/$taxYear" should {
    behave like controllerSpec(
      OK,
      Json.toJson(aTradesJourneyStatusesSeq).toString,
      () => stubGetBusinessJourneyStates(mockBusinessService, taxYear, Right(aTradesJourneyStatusesSeq)),
      () => underTest.getBusinessJourneyStates(nino, taxYear)
    )

    behave like controllerSpec(
      NO_CONTENT,
      "",
      () => stubGetBusinessJourneyStates(mockBusinessService, taxYear, Right(Nil)),
      () => underTest.getBusinessJourneyStates(nino, taxYear))

    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(MongoError("db error")).toString(),
      () => stubGetBusinessJourneyStates(mockBusinessService, taxYear, Left(MongoError("db error"))),
      () => underTest.getBusinessJourneyStates(nino, taxYear),
      "Mongo-Error"
    )

    val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, serverError)
    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(downstreamError).toString(),
      () => stubGetBusinessJourneyStates(mockBusinessService, taxYear, Left(downstreamError)),
      () => underTest.getBusinessJourneyStates(businessId, taxYear),
      "Api-Error"
    )
  }

}

object BusinessDetailsControllerSpec {

  def stubGetBusinesses(mockBusinessService: BusinessService, nino: String, expectedResult: GetBusinessResponse): Unit = {
    when(mockBusinessService.getBusinesses(meq(nino))(any[HeaderCarrier])) thenReturn Future.successful(expectedResult)
    ()
  }

  def stubGetBusiness(mockBusinessService: BusinessService, nino: String, businessId: String, expectedResult: GetBusinessResponse): Unit = {
    when(mockBusinessService.getBusiness(meq(nino), meq(businessId))(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.successful(
      expectedResult)
    ()
  }

  def stubGetBusinessJourneyStates(mockBusinessService: BusinessService, taxYear: Int, expectedResult: GetBusinessJourneyStatesResponse): Unit = {
    when(mockBusinessService.getBusinessJourneyStates(any, meq(taxYear))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(expectedResult))
    ()
  }

}
