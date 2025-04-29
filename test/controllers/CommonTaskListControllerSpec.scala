/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.data.EitherT
import controllers.ControllerBehaviours.buildRequestNoContent
import models.common.{Mtditid, Nino, TaxYear}
import models.commonTaskList.{SelfEmploymentTitles, TaskListModel, SectionTitle => CommonSectionTitle, TaskListSection => CommonTaskListSection, TaskStatus => CommonTaskStatus}
import models.taskList.{SectionTitle, TaskListSection, TaskStatus, TaskTitle}
import play.api.http.Status.OK
import play.api.libs.json.Json
import services.CommonTaskListService
import services.journeyAnswers.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

class CommonTaskListControllerSpec extends ControllerBehaviours {
  val mockJourneyStatusService: JourneyStatusService = mock[JourneyStatusService]
  val service = new CommonTaskListService(mockJourneyStatusService)
  val controller = new CommonTaskListController(
    service = service,
    auth = mockAuthorisedAction,
    cc = stubControllerComponents
  )
  private val taxYear = TaxYear(2024)
  private val nino = Nino("AA123456A")
  private val mtditid = Mtditid("1234567890")

  "getCommonTaskList" should {
    "return a empty tasks" in {
      val expectedResult = TaskListSection(SectionTitle.SelfEmploymentTitle,None)

      (mockJourneyStatusService.getCommonTaskList(_: TaxYear, _: Mtditid, _: Nino)(_: HeaderCarrier))
        .expects(taxYear, mtditid, nino, *)
        .returning(EitherT.rightT(TaskListModel(List.empty)))

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json
          .toJson(expectedResult)
          .toString(),
        methodBlock = () => controller.getCommonTaskList(taxYear, nino)
      )
    }

    "return a tasks" in {
      val expectedResult = TaskListSection(
        SectionTitle.SelfEmploymentTitle,
        Option(
          List(
            models.taskList.TaskListSectionItem(
              TaskTitle.SelfEmployment,
              TaskStatus.Completed,
              Option("/test-url")
            )
          ))
      )

      val taskListModel = TaskListModel(List(
        CommonTaskListSection(
          sectionTitle = CommonSectionTitle.SelfEmploymentTitle(),
          taskItems = Option(
            List(
              models.commonTaskList.TaskListSectionItem(
                title = SelfEmploymentTitles.AdvertisingOrMarketing(),
                status = CommonTaskStatus.Completed(),
                href = Option("/test-url")
              )
            ))
        )
      ))
      (mockJourneyStatusService.getCommonTaskList(_: TaxYear, _: Mtditid, _: Nino)(_: HeaderCarrier))
        .expects(taxYear, mtditid, nino, *)
        .returning(EitherT.rightT(taskListModel))

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json
          .toJson(expectedResult)
          .toString(),
        methodBlock = () => controller.getCommonTaskList(taxYear, nino)
      )
    }
  }
}
