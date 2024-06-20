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
import play.api.mvc.PathBindable

sealed abstract class JourneyName(override val entryName: String) extends EnumEntry {
  override def toString: String = entryName

  def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean = false): String
}

object JourneyName extends Enum[JourneyName] with utils.PlayJsonEnum[JourneyName] {
  val values: IndexedSeq[JourneyName] = findValues

  case object TradeDetails extends JourneyName("trade-details") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String = "" // TODO do I need one here?
  }
  case object SelfEmploymentAbroad extends JourneyName("self-employment-abroad") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/details/self-employment-abroad/check"
      else s"$taxYear/$businessId/details/self-employment-abroad"
  }
  case object Income extends JourneyName("income") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/income/check-your-income"
      else s"$taxYear/$businessId/income/not-counted-turnover"
  }
  case object IncomePrepop extends JourneyName("income-prepop") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String = "" // TODO assuming we don't need these prepop ones
  }
  case object AdjustmentsPrepop extends JourneyName("adjustments-prepop") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String = ""
  }
  case object BusinessDetailsPrepop extends JourneyName("business-details-prepop") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String = "" // TODO do I need one here?
  }
  case object ExpensesTailoring extends JourneyName("expenses-categories") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/check"
      else s"$taxYear/$businessId/expenses"
  }
  case object GoodsToSellOrUse extends JourneyName("expenses-goods-to-sell-or-use") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/goods-sell-use/check"
      else s"$taxYear/$businessId/expenses/goods-sell-use/taxi-minicab-road-haulage-industry-driver"
  }
  case object WorkplaceRunningCosts extends JourneyName("expenses-workplace-running-costs") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/workplace-running-costs/workplace-running-costs/check"
      else
        s"$taxYear/$businessId/expenses/workplace-running-costs/working-from-home/more-than-25-hours" // TODO add logic to determine whether this should be WFH or WFBP
  }
  case object RepairsAndMaintenanceCosts extends JourneyName("expenses-repairs-and-maintenance") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/repairs-maintenance/check"
      else s"$taxYear/$businessId/expenses/repairs-maintenance/amount"
  }
  case object AdvertisingOrMarketing extends JourneyName("expenses-advertising-marketing") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/advertising-marketing/check"
      else s"$taxYear/$businessId/expenses/advertising-marketing/amount"
  }
  case object OfficeSupplies extends JourneyName("expenses-office-supplies") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/office-supplies/check"
      else s"$taxYear/$businessId/expenses/office-supplies/amount"
  }
  case object Entertainment extends JourneyName("expenses-entertainment") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/entertainment/check"
      else s"$taxYear/$businessId/expenses/entertainment/disallowable-amount"
  }
  case object StaffCosts extends JourneyName("expenses-staff-costs") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/staff/check"
      else s"$taxYear/$businessId/expenses/staff/amount"
  }
  case object Construction extends JourneyName("expenses-construction") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/construction-industry/check"
      else s"$taxYear/$businessId/expenses/construction-industry/amount"
  }
  case object ProfessionalFees extends JourneyName("expenses-professional-fees") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/professional-fees/check"
      else s"$taxYear/$businessId/expenses/professional-fees/amount"
  }
  case object Interest extends JourneyName("expenses-interest") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/interest-bank-business-loans/check"
      else s"$taxYear/$businessId/expenses/interest-bank-business-loans/amount"
  }
  case object OtherExpenses extends JourneyName("expenses-other-expenses") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/other-expenses/check "
      else s"$taxYear/$businessId/expenses/other-expenses/amount"
  }
  case object FinancialCharges extends JourneyName("expenses-financial-charges") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/bank-credit-card-financial-charges/check"
      else s"$taxYear/$businessId/expenses/bank-credit-card-financial-charges/amount"
  }
  case object IrrecoverableDebts extends JourneyName("expenses-irrecoverable-debts") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/irrecoverable-debts/check"
      else s"$taxYear/$businessId/expenses/irrecoverable-debts/amount"
  }
  case object Depreciation extends JourneyName("expenses-depreciation") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/expenses/depreciation/check"
      else s"$taxYear/$businessId/expenses/depreciation/disallowable-amount"
  }
  case object CapitalAllowancesTailoring extends JourneyName("capital-allowances-tailoring") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/check"
      else s"$taxYear/$businessId/capital-allowances"
  }
  case object ZeroEmissionCars extends JourneyName("capital-allowances-zero-emission-cars") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/cars/check"
      else s"$taxYear/$businessId/capital-allowances/cars"
  }
  case object ZeroEmissionGoodsVehicle extends JourneyName("capital-allowances-zero-emission-goods-vehicle") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/goods-vehicles/check"
      else s"$taxYear/$businessId/capital-allowances/goods-vehicles"
  }
  case object ElectricVehicleChargePoints extends JourneyName("capital-allowances-electric-vehicle-charge-points") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/electric-vehicle-charge-points/check"
      else s"$taxYear/$businessId/capital-allowances/electric-vehicle-charge-points/buy"
  }
  case object BalancingAllowance extends JourneyName("capital-allowances-balancing-allowance") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/balancing-allowance/check"
      else s"$taxYear/$businessId/capital-allowances/balancing-allowance"
  }
  case object WritingDownAllowance extends JourneyName("capital-allowances-writing-down-allowance") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/writing-down-allowance/check"
      else s"$taxYear/$businessId/capital-allowances/writing-down-allowance"
  }
  case object AnnualInvestmentAllowance extends JourneyName("capital-allowances-annual-investment-allowance") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/check"
      else s"$taxYear/$businessId/capital-allowances/details"
  }
  case object SpecialTaxSites extends JourneyName("capital-allowances-special-tax-sites") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/special-tax-sites/check"
      else s"$taxYear/$businessId/capital-allowances/special-tax-sites"
  }
  case object StructuresBuildings extends JourneyName("capital-allowances-structures-buildings") {
    override def getHref(taxYear: TaxYear, businessId: BusinessId, toCYA: Boolean): String =
      if (toCYA) s"/$taxYear/$businessId/capital-allowances/structures-buildings/check"
      else s"$taxYear/$businessId/capital-allowances/structures-buildings"
  }

  // Are we using this code? Should we be if not?
  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[JourneyName] = new PathBindable[JourneyName] {

    override def bind(key: String, value: String): Either[String, JourneyName] =
      strBinder.bind(key, value).flatMap { stringValue =>
        JourneyName.withNameOption(stringValue) match {
          case Some(journeyName) => Right(journeyName)
          case None              => Left(s"$stringValue Invalid journey name")
        }
      }

    override def unbind(key: String, journeyName: JourneyName): String =
      strBinder.unbind(key, journeyName.entryName)
  }

}
