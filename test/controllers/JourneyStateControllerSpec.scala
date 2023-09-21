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

import bulders.JourneyStateDataBuilder.aJourneyState
import models.error.ServiceError.{DatabaseError, MongoError}
import models.mdtp.JourneyState
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Json
import repositories.SessionRepository

import scala.concurrent.Future

class JourneyStateControllerSpec extends ControllerBehavioursSpec {
  
  lazy val mockSessionRepo = MockitoSugar.mock[SessionRepository]
  lazy val underTest = new JourneyStateController(mockSessionRepo, mockAuthorisedAction, mockControllerComponents)

  val taxYear = 2024
  val nino = "FI290077A"
  val journey = "view-trades"
  val completed = false
  
  s"GET /completed-section/$nino/$taxYear/$journey" should {
    behave like controllerSpec(OK, Json.toJson(aJourneyState.journeyStateData.completed).toString,
      () => stubSessionRepositoryGet(expectedResult = Right(Some(aJourneyState))),
      () => underTest.getJourneyState(nino, taxYear, journey))

    behave like controllerSpec(NO_CONTENT, "",
      () => stubSessionRepositoryGet(expectedResult = Right(None)),
      () => underTest.getJourneyState(nino, taxYear, journey))
    
    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(MongoError("db error").msg).toString(),
      () => stubSessionRepositoryGet(expectedResult = Left(MongoError("db error"))),
      () => underTest.getJourneyState(nino, taxYear, journey))
  }
  
  s"PUT /completed-section/$nino/$taxYear/$journey/$completed" should {
    behave like controllerSpec(NO_CONTENT, "",
      () => stubSessionRepositorySet(expectedResult = Right(true)),
      () => underTest.putJourneyState(nino, taxYear, journey, completed))

    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(MongoError("db error").msg).toString,
      () => stubSessionRepositorySet(expectedResult = Left(MongoError("db error"))),
      () => underTest.getJourneyState(nino, taxYear, journey))
  }
  
  private def stubSessionRepositoryGet(expectedResult: Either[DatabaseError, Option[JourneyState]]): Unit =
    when(mockSessionRepo.get(nino, taxYear, journey)) thenReturn (expectedResult match {
      case Right(optJourneyState) => Future.successful(optJourneyState)
      case Left(_) => Future.failed(new RuntimeException("db error"))
    })

  private def stubSessionRepositorySet(expectedResult: Either[DatabaseError, Boolean]): Unit =
    when(mockSessionRepo.set(any[JourneyState])) thenReturn(expectedResult match {
      case Right(_) =>  Future.successful(true)
      case Left(_) => Future.failed(new RuntimeException("db error"))
    })
}
