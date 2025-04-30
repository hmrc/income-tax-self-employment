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

sealed abstract class JourneyName(override val entryName: String, val collectionOptions: Option[CollectionOptions] = None) extends EnumEntry {
  override def toString: String = entryName

  def isCollectionJourney: Boolean = collectionOptions.isDefined
}

object JourneyName extends Enum[JourneyName] with utils.PlayJsonEnum[JourneyName] {

  val values: IndexedSeq[JourneyName] = findValues

  final case object TradeDetails                   extends JourneyName("trade-details", None)
  final case object IndustrySectors                extends JourneyName("industry-sectors", None)
  final case object SelfEmploymentAbroad           extends JourneyName("self-employment-abroad", None)
  final case object Income                         extends JourneyName("income", None)
  final case object ExpensesTailoring              extends JourneyName("expenses-categories", None)
  final case object GoodsToSellOrUse               extends JourneyName("expenses-goods-to-sell-or-use", None)
  final case object WorkplaceRunningCosts          extends JourneyName("expenses-workplace-running-costs", None)
  final case object RepairsAndMaintenanceCosts     extends JourneyName("expenses-repairs-and-maintenance", None)
  final case object AdvertisingOrMarketing         extends JourneyName("expenses-advertising-marketing", None)
  final case object OfficeSupplies                 extends JourneyName("expenses-office-supplies", None)
  final case object Entertainment                  extends JourneyName("expenses-entertainment", None)
  final case object StaffCosts                     extends JourneyName("expenses-staff-costs", None)
  final case object Construction                   extends JourneyName("expenses-construction", None)
  final case object ProfessionalFees               extends JourneyName("expenses-professional-fees", None)
  final case object Interest                       extends JourneyName("expenses-interest", None)
  final case object OtherExpenses                  extends JourneyName("expenses-other-expenses", None)
  final case object FinancialCharges               extends JourneyName("expenses-financial-charges", None)
  final case object IrrecoverableDebts             extends JourneyName("expenses-irrecoverable-debts", None)
  final case object Depreciation                   extends JourneyName("expenses-depreciation", None)
  final case object CapitalAllowancesTailoring     extends JourneyName("capital-allowances-tailoring", None)
  final case object ZeroEmissionCars               extends JourneyName("capital-allowances-zero-emission-cars", None)
  final case object ZeroEmissionGoodsVehicle       extends JourneyName("capital-allowances-zero-emission-goods-vehicle", None)
  final case object BalancingAllowance             extends JourneyName("capital-allowances-balancing-allowance", None)
  final case object BalancingCharge                extends JourneyName("capital-allowances-balancing-charge", None)
  final case object WritingDownAllowance           extends JourneyName("capital-allowances-writing-down-allowance", None)
  final case object AnnualInvestmentAllowance      extends JourneyName("capital-allowances-annual-investment-allowance", None)
  final case object SpecialTaxSites                extends JourneyName("capital-allowances-special-tax-sites", None)
  final case object StructuresBuildings            extends JourneyName("capital-allowances-structures-buildings", None)
  final case object ProfitOrLoss                   extends JourneyName("profit-or-loss", None)
  final case object NationalInsuranceContributions extends JourneyName("national-insurance-contributions", None)
  final case object TravelExpenses                 extends JourneyName("travel-expenses", None)
  final case object VehicleDetails                 extends JourneyName("vehicle-details", Some(CollectionOptions(minItems = 1)))

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

case class CollectionOptions(minItems: Int, maxItems: Option[Int] = None)
