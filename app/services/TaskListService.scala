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

import jakarta.inject.{Inject, Singleton}
import models.common.JourneyName.{CapitalAllowancesTailoring, ExpensesTailoring}
import models.common.{BusinessId, JourneyContext, Mtditid, TaxYear}
import models.commonTaskList.{SectionTitle, TaskListModel, TaskListRows, TaskListSection}
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
import models.frontend.TaskList
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TaskListService @Inject() (rows: TaskListRows, repo: JourneyAnswersRepository)(implicit ec: ExecutionContext) {

  def buildTaskList(legacyTaskList: TaskList, taxYear: TaxYear, mtditid: Mtditid): Future[TaskListModel] = {
    val journeyStatuses = legacyTaskList.businesses.flatMap(_.journeyStatuses)
    for {
      sections <- Future.sequence(legacyTaskList.businesses.map(business => buildBusinessRows(business, taxYear, mtditid, journeyStatuses)))
    } yield TaskListModel(sections.flatten)
  }

  private def buildBusinessRows(business: TradesJourneyStatuses,
                                taxYear: TaxYear,
                                mtditid: Mtditid,
                                journeyStatuses: Seq[JourneyNameAndStatus]): Future[Seq[TaskListSection]] =
    for {
      expenses          <- buildExpensesSection(business.businessId, taxYear, mtditid, journeyStatuses)
      capitalAllowances <- buildCapitalAllowancesSection(business.businessId, taxYear, mtditid, journeyStatuses)
      tradeDetails = buildBusinessDetailsSection(business, taxYear, journeyStatuses)
      adjustments  = buildAdjustmentsSection(business.businessId, taxYear, journeyStatuses)
    } yield Seq(tradeDetails, expenses, capitalAllowances, adjustments)

  private def buildBusinessDetailsSection(business: TradesJourneyStatuses,
                                          taxYear: TaxYear,
                                          journeyStatuses: Seq[JourneyNameAndStatus]): TaskListSection =
    TaskListSection(
      sectionTitle = SectionTitle.SelfEmploymentTitle(),
      caption = Some("SelfEmploymentCaption"),
      titleParams = business.tradingName.map(_.value).toSeq,
      taskItems = Some(
        Seq(
          rows.tradeDetailsRow.build(business.businessId, taxYear, journeyStatuses),
          rows.industrySectorsRow.build(business.businessId, taxYear, journeyStatuses),
          rows.incomeRow.build(business.businessId, taxYear, journeyStatuses)
        ).flatten)
    )

  private def buildExpensesSection(businessId: BusinessId,
                                   taxYear: TaxYear,
                                   mtditid: Mtditid,
                                   journeyStatuses: Seq[JourneyNameAndStatus]): Future[TaskListSection] =
    (for {
      optExpensesTailoring <- repo
        .getAnswers[ExpensesTailoringIndividualCategoriesAnswers](JourneyContext(taxYear, businessId, mtditid, ExpensesTailoring))
        .value
    } yield optExpensesTailoring match {
      case Right(Some(expensesTailoring)) =>
        Seq(
          rows.expenseCategoriesRow.build(businessId, taxYear, journeyStatuses),
          rows.officeSuppliesRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.goodsToSellOrUseRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.repairsAndMaintenanceCostsRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.workplaceRunningCostsRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.advertisingOrMarketingRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.travelCostsRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.entertainmentRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.staffCostsRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.constructionIndustryRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.professionalFeesRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.interestRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.financialChargesRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.irrecoverableDebtsRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.depreciationRow(expensesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.otherExpensesRow(expensesTailoring).build(businessId, taxYear, journeyStatuses)
        ).flatten
      case Right(_) | Left(_) =>
        Seq(
          rows.expenseCategoriesRow.build(businessId, taxYear, journeyStatuses)
        ).flatten
    }).map { taskItems =>
      TaskListSection(
        sectionTitle = SectionTitle.ExpensesTitle(),
        taskItems = Some(taskItems),
        isSubSection = Some(true)
      )
    }

  private def buildCapitalAllowancesSection(businessId: BusinessId,
                                            taxYear: TaxYear,
                                            mtditid: Mtditid,
                                            journeyStatuses: Seq[JourneyNameAndStatus]): Future[TaskListSection] =
    (for {
      optCapitalAllowancesTailoring <- repo
        .getAnswers[CapitalAllowancesTailoringAnswers](JourneyContext(taxYear, businessId, mtditid, CapitalAllowancesTailoring))
        .value
    } yield optCapitalAllowancesTailoring match {
      case Right(Some(capitalAllowancesTailoring)) =>
        Seq(
          rows.capitalAllowancesTailoringRow.build(businessId, taxYear, journeyStatuses),
          rows.balancingAllowanceRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.annualInvestmentAllowanceRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.structuresAndBuildingsAllowanceRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.writingDownAllowanceRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.specialTaxSitesRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses),
          rows.balancingChargeRow(capitalAllowancesTailoring).build(businessId, taxYear, journeyStatuses)
        ).flatten
      case Right(_) | Left(_) =>
        Seq(
          rows.capitalAllowancesTailoringRow.build(businessId, taxYear, journeyStatuses)
        ).flatten
    }).map { taskItems =>
      TaskListSection(
        sectionTitle = SectionTitle.CapitalAllowancesTitle(),
        taskItems = Some(taskItems),
        isSubSection = Some(true)
      )
    }

  private def buildAdjustmentsSection(businessId: BusinessId, taxYear: TaxYear, journeyStatuses: Seq[JourneyNameAndStatus]): TaskListSection =
    TaskListSection(
      sectionTitle = SectionTitle.AdjustmentsTitle(),
      taskItems = Some(
        Seq(
          rows.profitOrLossRow.build(businessId, taxYear, journeyStatuses)
        ).flatten),
      isSubSection = Some(true)
    )

}
