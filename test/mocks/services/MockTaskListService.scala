/*
 * Copyright 2025 HM Revenue & Customs
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

package mocks.services

import models.common.{Mtditid, TaxYear}
import models.commonTaskList.{TaskListModel, TaskListSection}
import models.frontend.TaskList
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import services.TaskListService

import scala.concurrent.Future

object MockTaskListService {

  val mockInstance: TaskListService = mock[TaskListService]

  def mockBuildCommonTaskList(legacyTaskList: TaskList, taxYear: TaxYear, mtditid: Mtditid)(
      response: Future[TaskListModel]): OngoingStubbing[TaskListSection] =
    when(mockInstance.buildTaskList(eqTo(legacyTaskList), eqTo(taxYear), eqTo(mtditid)))
      .thenReturn(response)

}
