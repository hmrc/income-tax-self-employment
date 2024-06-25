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

package models.commonTaskList

import cats.implicits.catsSyntaxOptionId
import models.common.{BusinessId, TaxYear}
import models.commonTaskList.TaskStatus._
import models.domain.JourneyNameAndStatus
import play.api.libs.json.{Json, OFormat}

case class TaskListSectionItem(title: TaskTitle, status: TaskStatus, href: Option[String])

object TaskListSectionItem {
  implicit val format: OFormat[TaskListSectionItem] = Json.format[TaskListSectionItem]

  def fromJourneys(taxYear: TaxYear, businessId: BusinessId, savedJourneys: Seq[JourneyNameAndStatus]): Seq[TaskListSectionItem] =
    SelfEmploymentTitles.values.map { journey => // TODO only get default and tailored journeys, not all
      val maybeJourneyStatus: Option[JourneyNameAndStatus] = savedJourneys.find(_.name.entryName == journey.journeyName)
      maybeJourneyStatus match {
        case Some(nameAndStatus) => // Status means submission exists -> NotStarted, InProgress or Completed
          TaskListSectionItem(journey, nameAndStatus.journeyStatus.toCommonTaskListStatus, journey.getHref(taxYear, businessId, toCYA = true).some)
        case None => // No status -> CheckOurRecords or CannotStartYet
          TaskListSectionItem(
            journey,
            CheckNow(),
            journey.getHref(taxYear, businessId).some
          ) // TODO move logic for CannotStartYet status from FE to BE
      }
    }
}
