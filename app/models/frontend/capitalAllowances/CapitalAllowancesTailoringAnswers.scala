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

package models.frontend.capitalAllowances

import models.common.{Enumerable, WithName}
import play.api.libs.json.{Format, Json}

case class CapitalAllowancesTailoringAnswers(claimCapitalAllowances: Boolean, selectCapitalAllowances: List[CapitalAllowances])

object CapitalAllowancesTailoringAnswers {
  implicit val formats: Format[CapitalAllowancesTailoringAnswers] = Json.format[CapitalAllowancesTailoringAnswers]
}

sealed trait CapitalAllowances

object CapitalAllowances extends Enumerable.Implicits {

  case object ZeroEmissionCar                       extends WithName("zeroEmissionCar") with CapitalAllowances
  case object ZeroEmissionGoodsVehicle              extends WithName("zeroEmissionGoodsVehicle") with CapitalAllowances
  case object ElectricVehicleChargepoint            extends WithName("electricVehicleChargepoint") with CapitalAllowances
  case object StructuresAndBuildings                extends WithName("structuresAndBuildings") with CapitalAllowances
  case object SpecialTaxSitesStructuresAndBuildings extends WithName("specialTaxSitesStructuresAndBuildings") with CapitalAllowances
  case object AnnualInvestment                      extends WithName("annualInvestment") with CapitalAllowances
  case object WritingDown                           extends WithName("writingDown") with CapitalAllowances
  case object Balancing                             extends WithName("balancing") with CapitalAllowances
  case object BalancingCharge                       extends WithName("balancingCharge") with CapitalAllowances

  val accrualAllowances: List[CapitalAllowances] =
    List(
      ZeroEmissionCar,
      ZeroEmissionGoodsVehicle,
      ElectricVehicleChargepoint,
      StructuresAndBuildings,
      SpecialTaxSitesStructuresAndBuildings,
      AnnualInvestment,
      WritingDown,
      Balancing,
      BalancingCharge
    )

  val cashAllowances: List[CapitalAllowances] =
    List(ZeroEmissionCar, WritingDown, Balancing, BalancingCharge)

  implicit val enumerable: Enumerable[CapitalAllowances] =
    Enumerable(
      (cashAllowances ++ accrualAllowances).distinct
        .map(v => v.toString -> v): _*)

}
