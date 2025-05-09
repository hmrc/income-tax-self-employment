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

import models.common.{JourneyName, JourneyStatus}
import models.commonTaskList.TaskStatus.{Completed, NotStarted}
import models.domain.JourneyNameAndStatus
import org.scalatest.matchers.must.Matchers
import data.CommonTestData
import utils.BaseSpec

class TaskListRowBuilderSpec extends BaseSpec with CommonTestData with Matchers {

  val standardUrl = s"/test/$testBusinessId/$testTaxYear"
  val cyaUrl      = s"/test/cya/$testBusinessId/$testTaxYear"

  private val rowBuilder1 = TaskListRowBuilder(
    journey = JourneyName.TradeDetails,
    url = (businessId, taxYear) => s"/test/$businessId/$taxYear",
    cyaUrl = Some((businessId, taxYear) => s"/test/cya/$businessId/$taxYear"),
    prerequisiteRows = Seq.empty,
    extraDisplayConditions = Seq.empty,
    completeConditions = Seq.empty
  )

  private val rowBuilder2 = TaskListRowBuilder(
    journey = JourneyName.Income,
    url = (businessId, taxYear) => s"/test/$businessId/$taxYear",
    cyaUrl = Some((businessId, taxYear) => s"/test/cya/$businessId/$taxYear"),
    prerequisiteRows = Seq.empty,
    extraDisplayConditions = Seq.empty,
    completeConditions = Seq.empty
  )

  "build" when {
    "the row has prerequisite rows" should {
      "display when all prerequisite rows have COMPLETED status" in {
        val testRow = rowBuilder2.copy(prerequisiteRows = Seq(rowBuilder1))

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.TradeDetails, JourneyStatus.Completed),
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          )
        )

        result mustBe Some(TaskListSectionItem(JourneyName.Income.entryName, NotStarted(), Some(standardUrl)))
      }

      "display COMPLETED with CYA URL when all prerequisite rows have COMPLETED status AND row status is COMPLETED" in {
        val testRow = rowBuilder2.copy(prerequisiteRows = Seq(rowBuilder1))

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.TradeDetails, JourneyStatus.Completed),
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.Completed)
          )
        )

        result mustBe Some(TaskListSectionItem(JourneyName.Income.entryName, Completed(), Some(cyaUrl)))
      }

      "display when all prerequisite rows have COMPLETED status and extra display conditions are met" in {
        val testRow = rowBuilder2.copy(
          prerequisiteRows = Seq(rowBuilder1),
          extraDisplayConditions = Seq(true)
        )

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.TradeDetails, JourneyStatus.Completed),
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          )
        )

        result mustBe Some(TaskListSectionItem(JourneyName.Income.entryName, NotStarted(), Some(standardUrl)))
      }
      "not display when any prerequisite row is not COMPLETED" in {
        val testRow = rowBuilder2.copy(
          prerequisiteRows = Seq(rowBuilder1)
        )

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          ))

        result mustBe None
      }
      "not display when all prerequisite rows are COMPLETED, but extra display conditions are not met" in {
        val testRow = rowBuilder2.copy(
          prerequisiteRows = Seq(rowBuilder1),
          extraDisplayConditions = Seq(false)
        )

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.TradeDetails, JourneyStatus.Completed),
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          )
        )

        result mustBe None
      }
    }
    "the row has no prerequisite rows" should {
      "display when extra display conditions are met" in {
        val testRow = rowBuilder2.copy(extraDisplayConditions = Seq(true))

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          ))

        result mustBe Some(TaskListSectionItem(JourneyName.Income.entryName, NotStarted(), Some(standardUrl)))
      }
      "not display when extra display conditions are not met" in {
        val testRow = rowBuilder2.copy(extraDisplayConditions = Seq(false))

        val result = testRow.build(
          testBusinessId,
          testTaxYear,
          Seq(
            JourneyNameAndStatus(JourneyName.Income, JourneyStatus.NotStarted)
          ))

        result mustBe None
      }
    }
  }

}
