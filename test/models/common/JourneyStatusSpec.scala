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

import models.common.JourneyStatus._
import models.commonTaskList.TaskStatus
import org.scalatest.prop.TableDrivenPropertyChecks
import utils.BaseSpec

class JourneyStatusSpec extends BaseSpec with TableDrivenPropertyChecks {

  private val cases = Table(
    ("journeyStatus", "taskStatus"),
    (CheckOurRecords, TaskStatus.CheckNow()),
    (NotStarted, TaskStatus.NotStarted()),
    (InProgress, TaskStatus.InProgress()),
    (Completed, TaskStatus.Completed()),
    (CannotStartYet, TaskStatus.CannotStartYet())
  )

  "toCommonTaskListStatus" should {
    "convert JourneyStatus to equivalent TaskStatus" in {
      forAll(cases) { (journeyStatus, taskStatus) =>
        assert(journeyStatus.toCommonTaskListStatus == taskStatus)
      }
    }
  }

}
