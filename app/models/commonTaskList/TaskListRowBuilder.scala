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

package models.commonTaskList

import models.common._
import models.commonTaskList.TaskStatus.{CannotStartYet, Completed, NotStarted}
import models.domain.JourneyNameAndStatus

case class TaskListRowBuilder(journey: JourneyName,
                              url: (BusinessId, TaxYear) => String,
                              cyaUrl: Option[(BusinessId, TaxYear) => String] = None,
                              prerequisiteRows: Seq[TaskListRowBuilder] = Nil,
                              extraDisplayConditions: Seq[Boolean] = Nil,
                              completeConditions: Seq[Boolean] = Nil, // For future use - do not use
                              defaultStatus: TaskStatus = CannotStartYet(),
                              isStatic: Boolean = false) {

  def build(businessId: BusinessId, taxYear: TaxYear, journeyStatuses: Seq[JourneyNameAndStatus]): Option[TaskListSectionItem] = {
    val href    = url(businessId, taxYear)
    val cyaHref = cyaUrl.getOrElse(url).apply(businessId, taxYear)

    val arePrerequisitesMet     = prerequisitesMet(journeyStatuses)
    val areDisplayConditionsMet = extraDisplayConditions.isEmpty || extraDisplayConditions.forall(_ == true)
    val status = journeyStatuses
      .find(_.name == journey)
      .map(_.journeyStatus.toCommonTaskListStatus)
      .getOrElse {
        if (arePrerequisitesMet) {
          NotStarted()
        } else {
          defaultStatus
        }
      }

    if (arePrerequisitesMet) {
      if (isComplete(journeyStatuses)) {
        Some(TaskListSectionItem(journey.entryName, Completed(), Some(cyaHref)))
      } else if (areDisplayConditionsMet) {
        Some(TaskListSectionItem(journey.entryName, status, Some(href)))
      } else {
        None
      }
    } else {
      if (isStatic) {
        Some(TaskListSectionItem(journey.entryName, defaultStatus, Some(href)))
      } else {
        None
      }
    }
  }

  def isComplete(journeyStatuses: Seq[JourneyNameAndStatus]): Boolean =
    journeyStatuses
      .find(_.name == journey)
      .map(_.journeyStatus)
      .contains(JourneyStatus.Completed)

  private def prerequisitesMet(journeyStatuses: Seq[JourneyNameAndStatus]): Boolean =
    prerequisiteRows.isEmpty || prerequisiteRows.forall(_.isComplete(journeyStatuses))

}
