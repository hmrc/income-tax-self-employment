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

package models.common

import enumeratum._
import models.commonTaskList.TaskStatus

sealed abstract class JourneyStatus(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  def toCommonTaskListStatus: TaskStatus
}

object JourneyStatus extends Enum[JourneyStatus] with utils.PlayJsonEnum[JourneyStatus] {

  val values: IndexedSeq[JourneyStatus] = findValues

  /** This status is used if there are no answers persisted */
  case object CheckOurRecords extends JourneyStatus("checkOurRecords") {
    override def toCommonTaskListStatus: TaskStatus = TaskStatus.CheckNow()
  }

  /** It is used to indicate the answers were submitted, but the 'Have you completed' question has not been answered */
  case object NotStarted extends JourneyStatus("notStarted") {
    override def toCommonTaskListStatus: TaskStatus = TaskStatus.NotStarted()
  }

  /** The completion page has been passed with answer No */
  case object InProgress extends JourneyStatus("inProgress") {
    override def toCommonTaskListStatus: TaskStatus = TaskStatus.InProgress()
  }

  /** The completion page has been passed with answer Yes */
  case object Completed extends JourneyStatus("completed") {
    override def toCommonTaskListStatus: TaskStatus = TaskStatus.Completed()
  }

  /** The completion page has been passed with answer Yes */
  case object CannotStartYet extends JourneyStatus("cannotStartYet") {
    override def toCommonTaskListStatus: TaskStatus = TaskStatus.CannotStartYet()
  }
}
