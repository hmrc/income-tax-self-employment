package controllers

import base.IntegrationBaseSpec
import helpers.AuthStub
import models.common.IdType
import models.common.JourneyName.{CapitalAllowancesTailoring, ExpensesTailoring, Income, ProfitOrLoss, SelfEmploymentAbroad, TradeDetails}
import models.commonTaskList.TaskStatus.{CannotStartYet, InProgress, NotStarted}
import models.commonTaskList.{SectionTitle, TaskListSection, TaskListSectionItem, TaskStatus}
import org.scalatest.Assertion
import play.api.http.Status.OK
import play.api.libs.json.{Format, JsLookupResult, Json}
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
      "return task list sections for each business, displaying all static rows" in {
        stubAuthorisedIndividual()
        stubGetWithResponseBody(
          url = s"/registration/business-details/${IdType.Nino}/$testNino",
          expectedResponse = Json.toJson(test1171Response).toString(),
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
