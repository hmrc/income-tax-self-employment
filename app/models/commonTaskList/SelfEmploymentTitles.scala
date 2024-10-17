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

import models.common.{BusinessId, TaxYear, WithName}
import play.api.libs.json.{Json, OWrites, Reads}

object SelfEmploymentTitles {

  val values: List[TaskTitle] = List(
    TradeDetails(), // TODO should trade details be excluded, or maybe in it's own section?
    SelfEmploymentAbroad(),
    Income(),
    ExpensesTailoring(),
    GoodsToSellOrUse(),
    WorkplaceRunningCosts(),
    RepairsAndMaintenanceCosts(),
    AdvertisingOrMarketing(),
    OfficeSupplies(),
    Entertainment(),
    StaffCosts(),
    Construction(),
    ProfessionalFees(),
    Interest(),
    OtherExpenses(),
    FinancialCharges(),
    IrrecoverableDebts(),
    Depreciation(),
    CapitalAllowancesTailoring(),
    ZeroEmissionCars(),
    ZeroEmissionGoodsVehicle(),
    BalancingAllowance(),
    WritingDownAllowance(),
    AnnualInvestmentAllowance(),
    SpecialTaxSites(),
    StructuresBuildings()
  )

  case class TradeDetails() extends WithName("trade-details") with TaskTitle
  object TradeDetails {
    implicit val nonStrictReads: Reads[TradeDetails] = Reads.pure(TradeDetails())
    implicit val writes: OWrites[TradeDetails]       = OWrites[TradeDetails](_ => Json.obj())
  }

  case class SelfEmploymentAbroad() extends WithName("self-employment-abroad") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/details/self-employment-abroad/check"
      else s"/$taxYear/$businessId/details/self-employment-abroad"
  }
  object SelfEmploymentAbroad {
    implicit val nonStrictReads: Reads[SelfEmploymentAbroad] = Reads.pure(SelfEmploymentAbroad())
    implicit val writes: OWrites[SelfEmploymentAbroad]       = OWrites[SelfEmploymentAbroad](_ => Json.obj())
  }

  case class Income() extends WithName("income") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/income/check-your-income"
      else s"/$taxYear/$businessId/income/not-counted-turnover"
  }
  object Income {
    implicit val nonStrictReads: Reads[Income] = Reads.pure(Income())
    implicit val writes: OWrites[Income]       = OWrites[Income](_ => Json.obj())
  }

  case class ExpensesTailoring() extends WithName("expenses-categories") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/check"
      else s"/$taxYear/$businessId/expenses"
  }
  object ExpensesTailoring {
    implicit val nonStrictReads: Reads[ExpensesTailoring] = Reads.pure(ExpensesTailoring())
    implicit val writes: OWrites[ExpensesTailoring]       = OWrites[ExpensesTailoring](_ => Json.obj())
  }

  case class GoodsToSellOrUse() extends WithName("expenses-goods-to-sell-or-use") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/goods-sell-use/check"
      else s"/$taxYear/$businessId/expenses/goods-sell-use/taxi-minicab-road-haulage-industry-driver"
  }
  object GoodsToSellOrUse {
    implicit val nonStrictReads: Reads[GoodsToSellOrUse] = Reads.pure(GoodsToSellOrUse())
    implicit val writes: OWrites[GoodsToSellOrUse]       = OWrites[GoodsToSellOrUse](_ => Json.obj())
  }

  case class WorkplaceRunningCosts() extends WithName("expenses-workplace-running-costs") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/workplace-running-costs/workplace-running-costs/check"
      else
        s"/$taxYear/$businessId/expenses/workplace-running-costs/working-from-home/more-than-25-hours" // TODO add logic to determine whether this should be WFH or WFBP
  }
  object WorkplaceRunningCosts {
    implicit val nonStrictReads: Reads[WorkplaceRunningCosts] = Reads.pure(WorkplaceRunningCosts())
    implicit val writes: OWrites[WorkplaceRunningCosts]       = OWrites[WorkplaceRunningCosts](_ => Json.obj())
  }

  case class RepairsAndMaintenanceCosts() extends WithName("expenses-repairs-and-maintenance") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/repairs-maintenance/check"
      else s"/$taxYear/$businessId/expenses/repairs-maintenance/amount"
  }
  object RepairsAndMaintenanceCosts {
    implicit val nonStrictReads: Reads[RepairsAndMaintenanceCosts] = Reads.pure(RepairsAndMaintenanceCosts())
    implicit val writes: OWrites[RepairsAndMaintenanceCosts]       = OWrites[RepairsAndMaintenanceCosts](_ => Json.obj())
  }

  case class AdvertisingOrMarketing() extends WithName("expenses-advertising-marketing") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/advertising-marketing/check"
      else s"/$taxYear/$businessId/expenses/advertising-marketing/amount"
  }
  object AdvertisingOrMarketing {
    implicit val nonStrictReads: Reads[AdvertisingOrMarketing] = Reads.pure(AdvertisingOrMarketing())
    implicit val writes: OWrites[AdvertisingOrMarketing]       = OWrites[AdvertisingOrMarketing](_ => Json.obj())
  }

  case class OfficeSupplies() extends WithName("expenses-office-supplies") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/office-supplies/check"
      else s"/$taxYear/$businessId/expenses/office-supplies/amount"
  }
  object OfficeSupplies {
    implicit val nonStrictReads: Reads[OfficeSupplies] = Reads.pure(OfficeSupplies())
    implicit val writes: OWrites[OfficeSupplies]       = OWrites[OfficeSupplies](_ => Json.obj())
  }

  case class Entertainment() extends WithName("expenses-entertainment") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/entertainment/check"
      else s"/$taxYear/$businessId/expenses/entertainment/disallowable-amount"
  }
  object Entertainment {
    implicit val nonStrictReads: Reads[Entertainment] = Reads.pure(Entertainment())
    implicit val writes: OWrites[Entertainment]       = OWrites[Entertainment](_ => Json.obj())
  }

  case class StaffCosts() extends WithName("expenses-staff-costs") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/staff/check"
      else s"/$taxYear/$businessId/expenses/staff/amount"
  }
  object StaffCosts {
    implicit val nonStrictReads: Reads[StaffCosts] = Reads.pure(StaffCosts())
    implicit val writes: OWrites[StaffCosts]       = OWrites[StaffCosts](_ => Json.obj())
  }

  case class Construction() extends WithName("expenses-construction") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/construction-industry/check"
      else s"/$taxYear/$businessId/expenses/construction-industry/amount"
  }
  object Construction {
    implicit val nonStrictReads: Reads[Construction] = Reads.pure(Construction())
    implicit val writes: OWrites[Construction]       = OWrites[Construction](_ => Json.obj())
  }

  case class ProfessionalFees() extends WithName("expenses-professional-fees") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/professional-fees/check"
      else s"/$taxYear/$businessId/expenses/professional-fees/amount"
  }
  object ProfessionalFees {
    implicit val nonStrictReads: Reads[ProfessionalFees] = Reads.pure(ProfessionalFees())
    implicit val writes: OWrites[ProfessionalFees]       = OWrites[ProfessionalFees](_ => Json.obj())
  }

  case class Interest() extends WithName("expenses-interest") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/interest-bank-business-loans/check"
      else s"/$taxYear/$businessId/expenses/interest-bank-business-loans/amount"
  }
  object Interest {
    implicit val nonStrictReads: Reads[Interest] = Reads.pure(Interest())
    implicit val writes: OWrites[Interest]       = OWrites[Interest](_ => Json.obj())
  }

  case class OtherExpenses() extends WithName("expenses-other-expenses") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/other-expenses/check "
      else s"/$taxYear/$businessId/expenses/other-expenses/amount"
  }
  object OtherExpenses {
    implicit val nonStrictReads: Reads[OtherExpenses] = Reads.pure(OtherExpenses())
    implicit val writes: OWrites[OtherExpenses]       = OWrites[OtherExpenses](_ => Json.obj())
  }

  case class FinancialCharges() extends WithName("expenses-financial-charges") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/bank-credit-card-financial-charges/check"
      else s"/$taxYear/$businessId/expenses/bank-credit-card-financial-charges/amount"
  }
  object FinancialCharges {
    implicit val nonStrictReads: Reads[FinancialCharges] = Reads.pure(FinancialCharges())
    implicit val writes: OWrites[FinancialCharges]       = OWrites[FinancialCharges](_ => Json.obj())
  }

  case class IrrecoverableDebts() extends WithName("expenses-irrecoverable-debts") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/irrecoverable-debts/check"
      else s"/$taxYear/$businessId/expenses/irrecoverable-debts/amount"
  }
  object IrrecoverableDebts {
    implicit val nonStrictReads: Reads[IrrecoverableDebts] = Reads.pure(IrrecoverableDebts())
    implicit val writes: OWrites[IrrecoverableDebts]       = OWrites[IrrecoverableDebts](_ => Json.obj())
  }

  case class Depreciation() extends WithName("expenses-depreciation") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/depreciation/check"
      else s"/$taxYear/$businessId/expenses/depreciation/disallowable-amount"
  }
  object Depreciation {
    implicit val nonStrictReads: Reads[Depreciation] = Reads.pure(Depreciation())
    implicit val writes: OWrites[Depreciation]       = OWrites[Depreciation](_ => Json.obj())
  }

  case class CapitalAllowancesTailoring() extends WithName("capital-allowances-tailoring") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/check"
      else s"/$taxYear/$businessId/capital-allowances"
  }
  object CapitalAllowancesTailoring {
    implicit val nonStrictReads: Reads[CapitalAllowancesTailoring] = Reads.pure(CapitalAllowancesTailoring())
    implicit val writes: OWrites[CapitalAllowancesTailoring]       = OWrites[CapitalAllowancesTailoring](_ => Json.obj())
  }

  case class ZeroEmissionCars() extends WithName("capital-allowances-zero-emission-cars") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/cars/check"
      else s"/$taxYear/$businessId/capital-allowances/cars"
  }
  object ZeroEmissionCars {
    implicit val nonStrictReads: Reads[ZeroEmissionCars] = Reads.pure(ZeroEmissionCars())
    implicit val writes: OWrites[ZeroEmissionCars]       = OWrites[ZeroEmissionCars](_ => Json.obj())
  }

  case class ZeroEmissionGoodsVehicle() extends WithName("capital-allowances-zero-emission-goods-vehicle") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/goods-vehicles/check"
      else s"/$taxYear/$businessId/capital-allowances/goods-vehicles"
  }
  object ZeroEmissionGoodsVehicle {
    implicit val nonStrictReads: Reads[ZeroEmissionGoodsVehicle] = Reads.pure(ZeroEmissionGoodsVehicle())
    implicit val writes: OWrites[ZeroEmissionGoodsVehicle]       = OWrites[ZeroEmissionGoodsVehicle](_ => Json.obj())
  }

  case class BalancingAllowance() extends WithName("capital-allowances-balancing-allowance") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/balancing-allowance/check"
      else s"/$taxYear/$businessId/capital-allowances/balancing-allowance"
  }
  object BalancingAllowance {
    implicit val nonStrictReads: Reads[BalancingAllowance] = Reads.pure(BalancingAllowance())
    implicit val writes: OWrites[BalancingAllowance]       = OWrites[BalancingAllowance](_ => Json.obj())
  }

  case class WritingDownAllowance() extends WithName("capital-allowances-writing-down-allowance") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/writing-down-allowance/check"
      else s"/$taxYear/$businessId/capital-allowances/writing-down-allowance"
  }
  object WritingDownAllowance {
    implicit val nonStrictReads: Reads[WritingDownAllowance] = Reads.pure(WritingDownAllowance())
    implicit val writes: OWrites[WritingDownAllowance]       = OWrites[WritingDownAllowance](_ => Json.obj())
  }

  case class AnnualInvestmentAllowance() extends WithName("capital-allowances-annual-investment-allowance") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/check"
      else s"/$taxYear/$businessId/capital-allowances/details"
  }
  object AnnualInvestmentAllowance {
    implicit val nonStrictReads: Reads[AnnualInvestmentAllowance] = Reads.pure(AnnualInvestmentAllowance())
    implicit val writes: OWrites[AnnualInvestmentAllowance]       = OWrites[AnnualInvestmentAllowance](_ => Json.obj())
  }

  case class SpecialTaxSites() extends WithName("capital-allowances-special-tax-sites") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/special-tax-sites/check"
      else s"/$taxYear/$businessId/capital-allowances/special-tax-sites"
  }
  object SpecialTaxSites {
    implicit val nonStrictReads: Reads[SpecialTaxSites] = Reads.pure(SpecialTaxSites())
    implicit val writes: OWrites[SpecialTaxSites]       = OWrites[SpecialTaxSites](_ => Json.obj())
  }

  case class StructuresBuildings() extends WithName("capital-allowances-structures-buildings") with TaskTitle {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/structures-buildings/check"
      else s"/$taxYear/$businessId/capital-allowances/structures-buildings"
  }
  object StructuresBuildings {
    implicit val nonStrictReads: Reads[StructuresBuildings] = Reads.pure(StructuresBuildings())
    implicit val writes: OWrites[StructuresBuildings]       = OWrites[StructuresBuildings](_ => Json.obj())
  }

}
