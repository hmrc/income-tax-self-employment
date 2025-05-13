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

import config.AppConfig
import jakarta.inject.Inject
import models.common.JourneyName.{CapitalAllowancesTailoring, _}
import models.frontend.capitalAllowances.{CapitalAllowances, CapitalAllowancesTailoringAnswers}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers
import models.frontend.expenses.tailoring.individualCategories.{
  AdvertisingOrMarketing => AdvertisingOrMarketingAnswer,
  GoodsToSellOrUse => GoodsToSellOrUseAnswer,
  OfficeSupplies => OfficeSuppliesAnswer,
  OtherExpenses => OtherExpensesAnswer,
  RepairsAndMaintenance => RepairsAndMaintenanceAnswer,
  TravelForWork => TravelForWorkAnswer,
  WorkFromBusinessPremises => WorkFromBusinessPremisesAnswer
}

class TaskListRows @Inject() (appConfig: AppConfig) {

  val urlPrefix = s"${appConfig.selfEmploymentFrontendHost}/update-and-submit-income-tax-return/self-employment"

  val tradeDetailsRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = TradeDetails,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/details/check",
    defaultStatus = TaskStatus.NotStarted(),
    isStatic = true
  )

  val industrySectorsRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = SelfEmploymentAbroad,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/details/self-employment-abroad",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/details/self-employment-abroad/check"),
    prerequisiteRows = Seq(tradeDetailsRow),
    isStatic = true
  )

  val incomeRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = Income,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/income/not-counted-turnover",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/income/check-your-income"),
    prerequisiteRows = Seq(industrySectorsRow),
    isStatic = true
  )

  val expenseCategoriesRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = ExpensesTailoring,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/check"),
    prerequisiteRows = Seq(industrySectorsRow),
    isStatic = true
  )

  def officeSuppliesRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = OfficeSupplies,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/office-supplies/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/office-supplies/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.officeSupplies != OfficeSuppliesAnswer.No)
  )

  def goodsToSellOrUseRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = GoodsToSellOrUse,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/goods-sell-use/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/goods-sell-use/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.goodsToSellOrUse != GoodsToSellOrUseAnswer.No)
  )

  def repairsAndMaintenanceCostsRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = RepairsAndMaintenanceCosts,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/repairs-maintenance/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/repairs-maintenance/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.repairsAndMaintenance != RepairsAndMaintenanceAnswer.No)
  )

  def workplaceRunningCostsRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = WorkplaceRunningCosts,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/workplace-running-costs/working-from-home/more-than-25-hours",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/workplace-running-costs/workplace-running-costs/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(
      expensesTailoring.workFromHome || expensesTailoring.workFromBusinessPremises != WorkFromBusinessPremisesAnswer.No
    )
  )

  def advertisingOrMarketingRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = AdvertisingOrMarketing,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/advertising-marketing/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/advertising-marketing/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.advertisingOrMarketing != AdvertisingOrMarketingAnswer.No)
  )

  def travelCostsRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = TravelExpenses,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/travel-costs/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/travel-costs/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.travelForWork != TravelForWorkAnswer.No)
  )

  def entertainmentRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = Entertainment,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/entertainment/disallowable-amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/entertainment/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.entertainmentCosts.contains(true))
  )

  def staffCostsRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = StaffCosts,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/staff/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/staff/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.disallowableStaffCosts.contains(true))
  )

  def constructionIndustryRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = Construction,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/construction-industry/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/construction-industry/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.disallowableSubcontractorCosts.contains(true))
  )

  def professionalFeesRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = ProfessionalFees,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/professional-fees/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/professional-fees/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.professionalServiceExpenses.nonEmpty)
  )

  def interestRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = Interest,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/interest-bank-business-loans/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/interest-bank-business-loans/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.disallowableInterest.contains(true))
  )

  def financialChargesRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = FinancialCharges,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/bank-credit-card-financial-charges/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/bank-credit-card-financial-charges/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.disallowableOtherFinancialCharges.contains(true))
  )

  def irrecoverableDebtsRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = IrrecoverableDebts,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/irrecoverable-debts/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/irrecoverable-debts/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.disallowableIrrecoverableDebts.contains(true))
  )

  def depreciationRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = Depreciation,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/depreciation/disallowable-amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/depreciation/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.depreciation)
  )

  def otherExpensesRow(expensesTailoring: ExpensesTailoringIndividualCategoriesAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = OtherExpenses,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/other-expenses/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/expenses/other-expenses/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    extraDisplayConditions = Seq(expensesTailoring.otherExpenses != OtherExpensesAnswer.No)
  )

  val capitalAllowancesTailoringRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = CapitalAllowancesTailoring,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/check"),
    prerequisiteRows = Seq(expenseCategoriesRow),
    isStatic = true
  )

  def balancingAllowanceRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = BalancingAllowance,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/balancing-allowance/amount",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/balancing-allowance/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.Balancing)
    )
  )

  def annualInvestmentAllowanceRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = AnnualInvestmentAllowance,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/annual-investment-allowance/use",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/annual-investment-allowance/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.AnnualInvestment)
    )
  )

  def structuresAndBuildingsAllowanceRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = StructuresBuildings,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/structures-buildings",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/structures-buildings/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.StructuresAndBuildings)
    )
  )

  def writingDownAllowanceRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = WritingDownAllowance,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/writing-down-allowance",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/writing-down-allowance/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.WritingDown)
    )
  )

  def specialTaxSitesRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = SpecialTaxSites,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/special-tax-sites",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/special-tax-sites/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.SpecialTaxSitesStructuresAndBuildings)
    )
  )

  def balancingChargeRow(caTailoring: CapitalAllowancesTailoringAnswers): TaskListRowBuilder = TaskListRowBuilder(
    journey = BalancingCharge,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/balancing-charge",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/capital-allowances/balancing-charge/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    extraDisplayConditions = Seq(
      caTailoring.claimCapitalAllowances,
      caTailoring.selectCapitalAllowances.contains(CapitalAllowances.BalancingCharge)
    )
  )

  val profitOrLossRow: TaskListRowBuilder = TaskListRowBuilder(
    journey = ProfitOrLoss,
    url = (businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/profit-or-loss",
    cyaUrl = Some((businessId, taxYear) => s"$urlPrefix/$taxYear/$businessId/profit-or-loss/check"),
    prerequisiteRows = Seq(capitalAllowancesTailoringRow),
    isStatic = true
  )

}
