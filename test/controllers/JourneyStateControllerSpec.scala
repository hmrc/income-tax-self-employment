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

import bulders.BusinessDataBuilder.{aBusiness, aBusinessJourneyStateSeq}
import bulders.JourneyStateDataBuilder.aJourneyState
import models.error.ErrorBody.ApiErrorBody.ifsServer500
import models.error.ServiceError.{DatabaseError, MongoError}
import models.error.StatusError.ApiStatusError
import models.mdtp.JourneyState
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.Json
import repositories.SessionRepository
import services.BusinessService
import services.BusinessService.GetBusinessJourneyStatesResponse

import scala.concurrent.Future

class JourneyStateControllerSpec extends ControllerBehaviours {
  
  lazy val mockSessionRepo = MockitoSugar.mock[SessionRepository]
  lazy val mockBusinessService = MockitoSugar.mock[BusinessService]
  lazy val underTest = new JourneyStateController(mockSessionRepo, mockBusinessService, mockAuthorisedAction, mockControllerComponents)

  val taxYear = 2024
  val nino = "some-nino"
  val businessId = aBusiness.businessId
  val journey = "view-trades"
  val completed = false
  
  s"GET /completed-section/$businessId/$taxYear/$journey" should {
    behave like controllerSpec(OK, Json.toJson(aJourneyState.journeyStateData.completedState).toString,
      () => stubSessionRepositoryGet(expectedResult = Right(Some(aJourneyState))),
      () => underTest.getJourneyState(businessId, journey, taxYear))

    behave like controllerSpec(NO_CONTENT, "",
      () => stubSessionRepositoryGet(expectedResult = Right(None)),
      () => underTest.getJourneyState(businessId, journey, taxYear))
    
    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(MongoError("db error").msg).toString(),
      () => stubSessionRepositoryGet(expectedResult = Left(MongoError("db error"))),
      () => underTest.getJourneyState(businessId, journey, taxYear))
  }
  
  s"GET /completed-section/$nino/$taxYear" should {
    behave like controllerSpec(OK, Json.toJson(aBusinessJourneyStateSeq).toString,
      () => stubBusinessConnectorGet(expectedResult = Right(aBusinessJourneyStateSeq)),
      () => underTest.getJourneyStateSeq(nino, taxYear)
    )

    behave like controllerSpec(NO_CONTENT, "",
      () => stubBusinessConnectorGet(expectedResult = Right(Seq())),
      () => underTest.getJourneyStateSeq(businessId, taxYear),
      "No content"
    )

    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(MongoError("db error")).toString(),
      () =>  stubBusinessConnectorGet(expectedResult =  Left(MongoError("db error"))),
      () => underTest.getJourneyStateSeq(businessId, taxYear),
      "Mongo-Error"
    )
    
    val apiStatusError = ApiStatusError(INTERNAL_SERVER_ERROR, ifsServer500)
    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(apiStatusError).toString(),
      () => stubBusinessConnectorGet(expectedResult = Left(apiStatusError)),
      () => underTest.getJourneyStateSeq(businessId, taxYear),
      "Api-Error"
    )
  }
  
  s"PUT /completed-section/$businessId/$taxYear/$journey/$completed" should {
    behave like controllerSpec(CREATED, "",
      () => {
        stubSessionRepositoryGet(expectedResult = Right(None))
        stubSessionRepositorySet(expectedResult = Right(true))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed))

    behave like controllerSpec(NO_CONTENT, "",
      () => {
        stubSessionRepositoryGet(expectedResult = Right(Some(aJourneyState)))
        stubSessionRepositorySet(expectedResult = Right(true))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed))

    behave like controllerSpec(INTERNAL_SERVER_ERROR, Json.toJson(MongoError("db error").msg).toString,
      () => {
        stubSessionRepositoryGet(expectedResult = Right(Some(aJourneyState)))
        stubSessionRepositorySet(expectedResult = Left(MongoError("db error")))
      },
      () => underTest.putJourneyState(businessId, journey, taxYear, completed))
  }
  
  private def stubSessionRepositoryGet(expectedResult: Either[DatabaseError, Option[JourneyState]]): Unit =
    when(mockSessionRepo.get(businessId, journey, taxYear)) thenReturn (expectedResult match {
      case Right(optJourneyState) => Future.successful(optJourneyState)
      case Left(MongoError(error)) => Future.failed(new RuntimeException(error))
    })

  private def stubSessionRepositorySet(expectedResult: Either[DatabaseError, Boolean]): Unit =
    when(mockSessionRepo.set(any[JourneyState])) thenReturn(expectedResult match {
      case Right(_) =>  Future.successful(true)
      case Left(MongoError(error)) => Future.failed(new RuntimeException(error))
    })
    
  private def stubBusinessConnectorGet(expectedResult: GetBusinessJourneyStatesResponse): Unit =
    when(mockBusinessService.getBusinessJourneyStates(any(), meq(taxYear))(any(), any())) thenReturn Future.successful(expectedResult)
}
