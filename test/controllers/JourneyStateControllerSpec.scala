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

import bulders.BusinessDataBuilder.{aBusiness, aTradesJourneyStatusesSeq}
import bulders.JourneyStateDataBuilder.aJourneyState
import models.database.JourneyState
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody.serverError
import models.error.ServiceError.DatabaseError
import models.error.ServiceError.DatabaseError.MongoError
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import repositories.MongoJourneyStateRepository
import services.BusinessService
import services.BusinessService.GetBusinessJourneyStatesResponse

import scala.concurrent.Future

class JourneyStateControllerSpec extends ControllerBehaviours {

  lazy val mockJourneyStateRepo = MockitoSugar.mock[MongoJourneyStateRepository]
  lazy val mockBusinessService  = MockitoSugar.mock[BusinessService]
  lazy val underTest = new JourneyStateController(mockJourneyStateRepo, mockBusinessService, mockAuthorisedAction, stubControllerComponents)

  val taxYear    = 2024
  val nino       = "some-nino"
  val businessId = aBusiness.businessId
  val journey    = "view-trades"
  val completed  = false

  s"GET /completed-section/$businessId/$taxYear/$journey" should {
    behave like controllerSpec(
      OK,
      Json.toJson(aJourneyState.journeyStateData.completedState).toString,
      () => stubJourneyStateRepositoryGet(expectedResult = Right(Some(aJourneyState))),
      () => underTest.getJourneyState(businessId, journey, taxYear)
    )

    behave like controllerSpec(
      NO_CONTENT,
      "",
      () => stubJourneyStateRepositoryGet(expectedResult = Right(None)),
      () => underTest.getJourneyState(businessId, journey, taxYear))

    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(MongoError("db error").errorMessage).toString(),
      () => stubJourneyStateRepositoryGet(expectedResult = Left(MongoError("db error"))),
      () => underTest.getJourneyState(businessId, journey, taxYear)
    )
  }

  s"GET /completed-section/$nino/$taxYear" should {
    behave like controllerSpec(
      OK,
      Json.toJson(aTradesJourneyStatusesSeq).toString,
      () => stubBusinessConnectorGet(expectedResult = Right(aTradesJourneyStatusesSeq)),
      () => underTest.getTaskList(nino, taxYear)
    )

    behave like controllerSpec(
      NO_CONTENT,
      "",
      () => stubBusinessConnectorGet(expectedResult = Right(Seq())),
      () => underTest.getTaskList(businessId, taxYear),
      "No content")

    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(MongoError("db error")).toString(),
      () => stubBusinessConnectorGet(expectedResult = Left(MongoError("db error"))),
      () => underTest.getTaskList(businessId, taxYear),
      "Mongo-Error"
    )

    val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, serverError)
    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(downstreamError).toString(),
      () => stubBusinessConnectorGet(expectedResult = Left(downstreamError)),
      () => underTest.getTaskList(businessId, taxYear),
      "Api-Error"
    )
  }

  s"PUT /completed-section/$businessId/$taxYear/$journey/$completed" should {
    behave like controllerSpec(
      CREATED,
      "",
      () => {
        stubJourneyStateRepositoryGet(expectedResult = Right(None))
        stubJourneyStateRepositorySet(expectedResult = Right(true))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed)
    )

    behave like controllerSpec(
      NO_CONTENT,
      "",
      () => {
        stubJourneyStateRepositoryGet(expectedResult = Right(Some(aJourneyState)))
        stubJourneyStateRepositorySet(expectedResult = Right(true))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed)
    )

    behave like controllerSpec(
      INTERNAL_SERVER_ERROR,
      Json.toJson(MongoError("db error").errorMessage).toString,
      () => {
        stubJourneyStateRepositoryGet(expectedResult = Right(Some(aJourneyState)))
        stubJourneyStateRepositorySet(expectedResult = Left(MongoError("db error")))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed)
    )
  }

  private def stubJourneyStateRepositoryGet(expectedResult: Either[DatabaseError, Option[JourneyState]]): Unit = {
    when(mockJourneyStateRepo.get(businessId, taxYear, journey)) thenReturn (expectedResult match {
      case Right(optJourneyState)  => Future.successful(optJourneyState)
      case Left(MongoError(error)) => Future.failed(new RuntimeException(error))
      case Left(error)             => Future.failed(new RuntimeException(error.errorMessage))
    })
    ()
  }

  private def stubJourneyStateRepositorySet(expectedResult: Either[DatabaseError, Boolean]): Unit = {
    when(mockJourneyStateRepo.set(any[JourneyState])) thenReturn (expectedResult match {
      case Right(_)                => Future.successful(())
      case Left(MongoError(error)) => Future.failed(new RuntimeException(error))
      case Left(error)             => Future.failed(new RuntimeException(error.errorMessage))
    })
    ()
  }

  private def stubBusinessConnectorGet(expectedResult: GetBusinessJourneyStatesResponse): Unit = {
    when(mockBusinessService.getBusinessJourneyStates(any(), meq(taxYear))(any(), any())) thenReturn Future.successful(expectedResult)
    ()
  }

}
