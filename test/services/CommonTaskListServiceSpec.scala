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

package services

import cats.data.EitherT
import models.common.{Mtditid, Nino, TaxYear}
import models.commonTaskList.{
  SelfEmploymentTitles,
  TaskListModel,
  SectionTitle => CommonSectionTitle,
  TaskListSection => CommonTaskListSection,
  TaskStatus => CommonTaskStatus
}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody
import models.taskList.{SectionTitle, TaskListSection, TaskStatus, TaskTitle}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import services.journeyAnswers.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CommonTaskListServiceSpec extends AnyWordSpecLike with MockFactory with ScalaFutures with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockJourneyStatusService: JourneyStatusService = mock[JourneyStatusService]
  val service = new CommonTaskListService(mockJourneyStatusService)

  private val taxYear = TaxYear(2024)
  private val nino = Nino("AA123456A")
  private val mtditid = Mtditid("1234567890")

  private val defaultTaskListSection = TaskListSection(
    SectionTitle.SelfEmploymentTitle,
    None
  )

  private val error = SingleDownstreamError(500, DownstreamErrorBody.SingleDownstreamErrorBody.serverError)

  "CommonTaskListService" should {
    "get" when {
      "return task list section with items when journey status returns valid data" in {
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

        val expectedSection = TaskListSection(
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

        val result = service.get(taxYear, nino, mtditid).futureValue
        result shouldBe expectedSection
      }

      "return default section when journey status returns empty task list" in {
        val emptyTaskListModel = TaskListModel(List.empty)

        (mockJourneyStatusService.getCommonTaskList(_: TaxYear, _: Mtditid, _: Nino)(_: HeaderCarrier))
          .expects(taxYear, mtditid,nino, *)
          .returning(EitherT.rightT(emptyTaskListModel))

        val result = service.get(taxYear, nino, mtditid).futureValue
        result shouldBe defaultTaskListSection
      }

      "return default section when journey status returns error" in {
        (mockJourneyStatusService.getCommonTaskList(_: TaxYear, _: Mtditid, _: Nino)(_: HeaderCarrier))
          .expects(taxYear, mtditid,nino, *)
          .returning(EitherT.leftT[Future, TaskListModel](error))

        val result = service.get(taxYear, nino, mtditid).futureValue
        result shouldBe defaultTaskListSection
      }

      "return task list section with multiple items in different states" in {
        val taskListModel = TaskListModel(List(
          CommonTaskListSection(
            sectionTitle = CommonSectionTitle.SelfEmploymentTitle(),
            taskItems = Option(List(
                  models.commonTaskList.TaskListSectionItem(
                    title = SelfEmploymentTitles.AdvertisingOrMarketing(),
                    status = CommonTaskStatus.Completed(),
                    href = Option("/completed-url")
                  ),
                  models.commonTaskList.TaskListSectionItem(
                    title = SelfEmploymentTitles.AdvertisingOrMarketing(),
                    status = CommonTaskStatus.InProgress(),
                    href = Option("/in-progress-url")
                  ),
                  models.commonTaskList.TaskListSectionItem(
                    title = SelfEmploymentTitles.AdvertisingOrMarketing(),
                    status = CommonTaskStatus.NotStarted(),
                    href = Option("/not-started-url")
                  )
                ))
          )
        ))

        (mockJourneyStatusService.getCommonTaskList(_: TaxYear, _: Mtditid, _: Nino)(_: HeaderCarrier))
          .expects(taxYear, mtditid,nino, *)
          .returning(EitherT.rightT(taskListModel))

        val expectedSection = TaskListSection(
          SectionTitle.SelfEmploymentTitle,
          Option(
            List(
              models.taskList.TaskListSectionItem(
                TaskTitle.SelfEmployment,
                TaskStatus.Completed,
                Option("/completed-url")
              ),
              models.taskList.TaskListSectionItem(
                TaskTitle.SelfEmployment,
                TaskStatus.InProgress,
                Option("/in-progress-url")
              ),
              models.taskList.TaskListSectionItem(
                TaskTitle.SelfEmployment,
                TaskStatus.NotStarted,
                Option("/not-started-url")
              )
            ))
        )

        val result = service.get(taxYear, nino, mtditid).futureValue
        result shouldBe expectedSection
      }
    }
  }
}
