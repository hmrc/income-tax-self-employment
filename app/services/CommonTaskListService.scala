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

package services

import cats.implicits._
import jakarta.inject.Singleton
import models.common.{Mtditid, Nino, TaxYear}
import models.taskList._
import play.api.Logging
import services.journeyAnswers.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommonTaskListService @Inject()(journeyStatusService: JourneyStatusService) extends Logging {
  
  def get(taxYear: TaxYear, nino: Nino, mtditid: Mtditid)
         (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[TaskListSection] =
    journeyStatusService.getCommonTaskList(
        taxYear,
        mtditid,
        nino
    ).map(
      model => convertToTaskListSection(model).headOption.getOrElse(
        models.taskList.TaskListSection(
          models.taskList.SectionTitle.SelfEmploymentTitle,
          None
        )
      )
    ).valueOr(
      _ => models.taskList.TaskListSection(
        models.taskList.SectionTitle.SelfEmploymentTitle,
        None
      )
    )

  def convertToTaskListSection(commonModel: models.commonTaskList.TaskListModel): Seq[models.taskList.TaskListSection] = {
    commonModel.taskList.map { commonSection =>
      models.taskList.TaskListSection(
        sectionTitle = convertSectionTitle(commonSection.sectionTitle),
        taskItems = commonSection.taskItems.map(_.map(convertTaskListSectionItem))
      )
    }
  }

  private def convertSectionTitle(commonTitle: models.commonTaskList.SectionTitle): models.taskList.SectionTitle =
    commonTitle match {
      case _: models.commonTaskList.SectionTitle.SelfEmploymentTitle =>
        models.taskList.SectionTitle.SelfEmploymentTitle
    }

  private def convertTaskStatus(commonStatus: models.commonTaskList.TaskStatus): models.taskList.TaskStatus =
    commonStatus match {
      case _: models.commonTaskList.TaskStatus.Completed => models.taskList.TaskStatus.Completed
      case _: models.commonTaskList.TaskStatus.InProgress => models.taskList.TaskStatus.InProgress
      case _: models.commonTaskList.TaskStatus.NotStarted => models.taskList.TaskStatus.NotStarted
      case _: models.commonTaskList.TaskStatus.CheckNow => models.taskList.TaskStatus.CheckNow
      case _: models.commonTaskList.TaskStatus.CannotStartYet => models.taskList.TaskStatus.UnderMaintenance
    }

  private def convertTaskListSectionItem(commonItem: models.commonTaskList.TaskListSectionItem): models.taskList.TaskListSectionItem =
    models.taskList.TaskListSectionItem(
      title = models.taskList.TaskTitle.SelfEmployment,
      status = convertTaskStatus(commonItem.status),
      href = commonItem.href
    )

}
