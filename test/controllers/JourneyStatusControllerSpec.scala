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

import controllers.ControllerBehaviours.{buildRequest, buildRequestNoContent}
import models.common.{JourneyName, JourneyStatus}
import models.domain.JourneyNameAndStatus
import models.frontend.{JourneyStatusData, TaskList}
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import stubs.services.StubJourneyStatusService
import utils.BaseSpec.{businessId, currTaxYear, nino}

class JourneyStatusControllerSpec extends ControllerBehaviours {
  private val journeyStatusService = StubJourneyStatusService(
    getRes = Right(JourneyStatus.InProgress)
  )

  private val underTest = new JourneyStatusController(journeyStatusService, mockAuthorisedAction, stubControllerComponents)

  "getStatus" should {
    "return journey name with status" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json
          .toJson(
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.InProgress)
          )
          .toString(),
        methodBlock = () => underTest.getStatus(businessId, JourneyName.Income, currTaxYear)
      )
    }
  }

  "setStatus" should {
    "set a new status" in {
      behave like testRoute(
        request = buildRequest(JourneyStatusData(JourneyStatus.Completed)),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.setStatus(businessId, JourneyName.Income, currTaxYear)
      )
    }
  }

  "getTaskList" should {
    "return task list" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json
          .toJson(TaskList.empty)
          .toString(),
        methodBlock = () => underTest.getTaskList(currTaxYear, nino)
      )
    }
  }

  "getCommonTaskList" should {
    "return a list of all " in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json
          .toJson(TaskList.empty)
          .toString(),
        methodBlock = () => underTest.getTaskList(currTaxYear, nino)
      )
    }
  }

}
