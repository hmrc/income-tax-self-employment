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

package controllers

import base.IntegrationBaseSpec
import connectors.data.Api1171Test
import helpers.AuthStub
import models.common.JourneyName.{CapitalAllowancesTailoring, ExpensesTailoring, Income, ProfitOrLoss, SelfEmploymentAbroad, TradeDetails}
import models.commonTaskList.TaskStatus.{CannotStartYet, NotStarted}
import models.commonTaskList.{SectionTitle, TaskListSection, TaskListSectionItem, TaskStatus}
import org.scalatest.Assertion
import play.api.http.Status.OK
import play.api.libs.json.{Format, JsLookupResult}
import play.api.test.Helpers.await
import testdata.CommonTestData

class TaskListControllerISpec extends IntegrationBaseSpec with CommonTestData with AuthStub {

  def jsPath[T](path: JsLookupResult)(implicit format: Format[T]): Option[T] =
    path.validate[T].asOpt

  implicit class SectionOps(section: TaskListSection) {

    def is(expected: (SectionTitle, Map[String, TaskStatus])): Assertion =
      (
        section.sectionTitle,
        section.taskItems
          .getOrElse(Nil)
          .map(item => item.title -> item.status)
          .toMap) mustBe expected

  }

  implicit class TaskOps(tasks: List[TaskListSectionItem]) {
    def taskExistsWithStatus(title: String, status: TaskStatus): Boolean =
      tasks.exists(task => task.title == title && task.status == status)
  }


  "GET /tasks/:nino" when {
    "minimal data is present" must {
      "return task list sections for each business, displaying all static rows" in new Api1171Test {
        stubAuthorisedIndividual()
        stubGetWithResponseBody(
          url = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?mtdReference=$testMtdItId&nino=$testNino",
          expectedResponse = test1171HipResponseJson,
          expectedStatus = OK
        )

        val res = await(buildClient(s"/$testTaxYear/tasks/$testNino").get)

        res.status mustBe OK

        // Business 1 checks
        val selfEmploymentSection = jsPath[TaskListSection](res.json \ "taskList" \ 0)

        selfEmploymentSection.get.is(
          SectionTitle.SelfEmploymentTitle() -> Map(
            TradeDetails.entryName         -> NotStarted(),
            SelfEmploymentAbroad.entryName -> CannotStartYet(),
            Income.entryName               -> CannotStartYet()
          )
        )

        val expensesSection = jsPath[TaskListSection](res.json \ "taskList" \ 1)
        expensesSection.get.is(
          SectionTitle.ExpensesTitle() -> Map(
            ExpensesTailoring.entryName -> CannotStartYet()
          )
        )

        val capitalAllowancesSection = jsPath[TaskListSection](res.json \ "taskList" \ 2)
        capitalAllowancesSection.get.is(
          SectionTitle.CapitalAllowancesTitle() -> Map(
            CapitalAllowancesTailoring.entryName -> CannotStartYet()
          )
        )

        val adjustmentsSection = jsPath[TaskListSection](res.json \ "taskList" \ 3)
        adjustmentsSection.get.is(
          SectionTitle.AdjustmentsTitle() -> Map(
            ProfitOrLoss.entryName -> CannotStartYet()
          )
        )

        // Business 2 checks
        val selfEmploymentSection2 = jsPath[TaskListSection](res.json \ "taskList" \ 4)
        selfEmploymentSection2.get.is(
          SectionTitle.SelfEmploymentTitle() -> Map(
            TradeDetails.entryName         -> NotStarted(),
            SelfEmploymentAbroad.entryName -> CannotStartYet(),
            Income.entryName               -> CannotStartYet()
          )
        )

        val expensesSection2 = jsPath[TaskListSection](res.json \ "taskList" \ 5)
        expensesSection2.get.is(
          SectionTitle.ExpensesTitle() -> Map(
            ExpensesTailoring.entryName -> CannotStartYet()
          )
        )

        val capitalAllowancesSection2 = jsPath[TaskListSection](res.json \ "taskList" \ 6)
        capitalAllowancesSection2.get.is(
          SectionTitle.CapitalAllowancesTitle() -> Map(
            CapitalAllowancesTailoring.entryName -> CannotStartYet()
          )
        )

        val adjustmentsSection2 = jsPath[TaskListSection](res.json \ "taskList" \ 7)
        adjustmentsSection2.get.is(
          SectionTitle.AdjustmentsTitle() -> Map(
            ProfitOrLoss.entryName -> CannotStartYet()
          )
        )
      }
    }
  }

}
