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

package bulders

import cats.implicits.catsSyntaxOptionId
import models.common.JourneyName._
import models.common.JourneyStatus._
import models.commonTaskList.{SelfEmploymentTitles, TaskListSectionItem, TaskStatus}
import models.domain.JourneyNameAndStatus
import utils.BaseSpec.{businessId, taxYear}

object JourneyNameAndStatusBuilder {

  val completedExpensesJourneys: Seq[JourneyNameAndStatus] = List(
    JourneyNameAndStatus(ExpensesTailoring, Completed),
    JourneyNameAndStatus(GoodsToSellOrUse, Completed),
    JourneyNameAndStatus(WorkplaceRunningCosts, Completed),
    JourneyNameAndStatus(RepairsAndMaintenanceCosts, Completed),
    JourneyNameAndStatus(AdvertisingOrMarketing, Completed),
    JourneyNameAndStatus(OfficeSupplies, Completed),
    JourneyNameAndStatus(Entertainment, Completed),
    JourneyNameAndStatus(StaffCosts, Completed),
    JourneyNameAndStatus(Construction, Completed),
    JourneyNameAndStatus(ProfessionalFees, Completed),
    JourneyNameAndStatus(Interest, Completed),
    JourneyNameAndStatus(OtherExpenses, Completed),
    JourneyNameAndStatus(FinancialCharges, Completed),
    JourneyNameAndStatus(IrrecoverableDebts, Completed),
    JourneyNameAndStatus(Depreciation, Completed)
  )

  val completedCapitalAllowancesJourneys: Seq[JourneyNameAndStatus] = List(
    JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
    JourneyNameAndStatus(ZeroEmissionCars, Completed),
    JourneyNameAndStatus(ZeroEmissionGoodsVehicle, Completed),
    JourneyNameAndStatus(BalancingAllowance, Completed),
    JourneyNameAndStatus(WritingDownAllowance, Completed),
    JourneyNameAndStatus(AnnualInvestmentAllowance, Completed),
    JourneyNameAndStatus(SpecialTaxSites, Completed),
    JourneyNameAndStatus(StructuresBuildings, Completed)
  )

  val allCompetedJourneyStatuses: Seq[JourneyNameAndStatus] = Seq(
    JourneyNameAndStatus(TradeDetails, Completed),
    JourneyNameAndStatus(SelfEmploymentAbroad, Completed),
    JourneyNameAndStatus(Income, Completed)
  ) ++ completedExpensesJourneys ++ completedCapitalAllowancesJourneys

  val mixedPartialJourneyStatuses: Seq[JourneyNameAndStatus] = Seq(
    JourneyNameAndStatus(TradeDetails, Completed),
    JourneyNameAndStatus(SelfEmploymentAbroad, InProgress),
    JourneyNameAndStatus(Income, InProgress)
  ) ++ completedExpensesJourneys.map(_.copy(journeyStatus = NotStarted))

  val completedExpensesTaskListSectionItems: Seq[TaskListSectionItem] = List(
    TaskListSectionItem(
      SelfEmploymentTitles.ExpensesTailoring(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.ExpensesTailoring().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.GoodsToSellOrUse(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.GoodsToSellOrUse().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.WorkplaceRunningCosts(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.WorkplaceRunningCosts().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.RepairsAndMaintenanceCosts(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.RepairsAndMaintenanceCosts().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.AdvertisingOrMarketing(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.AdvertisingOrMarketing().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.OfficeSupplies(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.OfficeSupplies().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.Entertainment(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.Entertainment().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.StaffCosts(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.StaffCosts().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.Construction(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.Construction().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.ProfessionalFees(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.ProfessionalFees().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.Interest(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.Interest().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.OtherExpenses(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.OtherExpenses().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.FinancialCharges(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.FinancialCharges().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.IrrecoverableDebts(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.IrrecoverableDebts().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.Depreciation(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.Depreciation().getHref(taxYear, businessId, toCYA = true).some)
  )

  val completedCapitalAllowancesTaskListSectionItems: Seq[TaskListSectionItem] = List(
    TaskListSectionItem(
      SelfEmploymentTitles.CapitalAllowancesTailoring(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.CapitalAllowancesTailoring().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.ZeroEmissionCars(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.ZeroEmissionCars().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.ZeroEmissionGoodsVehicle(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.ZeroEmissionGoodsVehicle().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.BalancingAllowance(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.BalancingAllowance().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.WritingDownAllowance(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.WritingDownAllowance().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.AnnualInvestmentAllowance(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.AnnualInvestmentAllowance().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.SpecialTaxSites(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.SpecialTaxSites().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.StructuresBuildings(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.StructuresBuildings().getHref(taxYear, businessId, toCYA = true).some
    )
  )

  val allCompletedTaskListSectionItems: Seq[TaskListSectionItem] = Seq(
    TaskListSectionItem(
      SelfEmploymentTitles.TradeDetails(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.TradeDetails().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.SelfEmploymentAbroad(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.SelfEmploymentAbroad().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.Income(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.Income().getHref(taxYear, businessId, toCYA = true).some)
  ) ++ completedExpensesTaskListSectionItems ++ completedCapitalAllowancesTaskListSectionItems

  val allCheckNowTaskListSectionItems: Seq[TaskListSectionItem] =
    allCompletedTaskListSectionItems.map(s => s.copy(status = TaskStatus.CheckNow(), href = s.title.getHref(taxYear, businessId).some))

  val notStartedExpensesTaskListSectionItems: Seq[TaskListSectionItem] = completedExpensesTaskListSectionItems.map(s =>
    s.copy(status = TaskStatus.NotStarted(), href = s.title.getHref(taxYear, businessId, toCYA = true).some))
  val checkNowCapitalAllowancesTaskListSectionItems: Seq[TaskListSectionItem] =
    completedCapitalAllowancesTaskListSectionItems.map(s => s.copy(status = TaskStatus.CheckNow(), href = s.title.getHref(taxYear, businessId).some))

  val mixedTaskListSectionItems: Seq[TaskListSectionItem] = Seq(
    TaskListSectionItem(
      SelfEmploymentTitles.TradeDetails(),
      TaskStatus.Completed(),
      SelfEmploymentTitles.TradeDetails().getHref(taxYear, businessId, toCYA = true).some),
    TaskListSectionItem(
      SelfEmploymentTitles.SelfEmploymentAbroad(),
      TaskStatus.InProgress(),
      SelfEmploymentTitles.SelfEmploymentAbroad().getHref(taxYear, businessId, toCYA = true).some
    ),
    TaskListSectionItem(
      SelfEmploymentTitles.Income(),
      TaskStatus.InProgress(),
      SelfEmploymentTitles.Income().getHref(taxYear, businessId, toCYA = true).some)
  ) ++ notStartedExpensesTaskListSectionItems ++ checkNowCapitalAllowancesTaskListSectionItems
}
