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
}

object JourneyName extends Enum[JourneyName] with utils.PlayJsonEnum[JourneyName] {

  val values: IndexedSeq[JourneyName] = findValues

  case object TradeDetails                   extends JourneyName("trade-details")
  case object SelfEmploymentAbroad           extends JourneyName("self-employment-abroad")
  case object Income                         extends JourneyName("income")
  case object ExpensesTailoring              extends JourneyName("expenses-categories")
  case object GoodsToSellOrUse               extends JourneyName("expenses-goods-to-sell-or-use")
  case object WorkplaceRunningCosts          extends JourneyName("expenses-workplace-running-costs")
  case object RepairsAndMaintenanceCosts     extends JourneyName("expenses-repairs-and-maintenance")
  case object AdvertisingOrMarketing         extends JourneyName("expenses-advertising-marketing")
  case object OfficeSupplies                 extends JourneyName("expenses-office-supplies")
  case object Entertainment                  extends JourneyName("expenses-entertainment")
  case object StaffCosts                     extends JourneyName("expenses-staff-costs")
  case object Construction                   extends JourneyName("expenses-construction")
  case object ProfessionalFees               extends JourneyName("expenses-professional-fees")
  case object Interest                       extends JourneyName("expenses-interest")
  case object OtherExpenses                  extends JourneyName("expenses-other-expenses")
  case object FinancialCharges               extends JourneyName("expenses-financial-charges")
  case object IrrecoverableDebts             extends JourneyName("expenses-irrecoverable-debts")
  case object Depreciation                   extends JourneyName("expenses-depreciation")
  case object CapitalAllowancesTailoring     extends JourneyName("capital-allowances-tailoring")
  case object ZeroEmissionCars               extends JourneyName("capital-allowances-zero-emission-cars")
  case object ZeroEmissionGoodsVehicle       extends JourneyName("capital-allowances-zero-emission-goods-vehicle")
  case object BalancingAllowance             extends JourneyName("capital-allowances-balancing-allowance")
  case object BalancingCharge                extends JourneyName("capital-allowances-balancing-charge")
  case object WritingDownAllowance           extends JourneyName("capital-allowances-writing-down-allowance")
  case object AnnualInvestmentAllowance      extends JourneyName("capital-allowances-annual-investment-allowance")
  case object SpecialTaxSites                extends JourneyName("capital-allowances-special-tax-sites")
  case object StructuresBuildings            extends JourneyName("capital-allowances-structures-buildings")
  case object ProfitOrLoss                   extends JourneyName("profit-or-loss")
  case object NationalInsuranceContributions extends JourneyName("national-insurance-contributions")

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
