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
import models.frontend.capitalAllowances.Group.{AssetAndAllowances, BuildingsAndStructures, ZeroEmissions}
import play.api.libs.json.{Format, Json}

case class CapitalAllowancesTailoringAnswers(claimAnyCapitalAllowances: Boolean, selectCapitalAllowances: List[CapitalAllowances])

object CapitalAllowancesTailoringAnswers {
  implicit val formats: Format[CapitalAllowancesTailoringAnswers] = Json.format[CapitalAllowancesTailoringAnswers]
}

sealed trait Group

object Group {
  case object ZeroEmissions          extends WithName("zeroEmissions") with Group
  case object BuildingsAndStructures extends WithName("buildingsAndStructures") with Group
  case object AssetAndAllowances     extends WithName("assetAndAllowances") with Group
}

sealed trait AllowanceType {
  val identifier: Group
}

sealed trait CapitalAllowances extends AllowanceType

object CapitalAllowances extends Enumerable.Implicits {

  case object ZeroEmissionCar extends WithName("zeroEmissionCar") with AllowanceType with CapitalAllowances {
    override val identifier: Group = ZeroEmissions
  }
  case object ZeroEmissionGoodsVehicle extends WithName("zeroEmissionGoodsVehicle") with AllowanceType with CapitalAllowances {
    override val identifier: Group = ZeroEmissions
  }
  case object ElectricVehicleChargepoint extends WithName("electricVehicleChargepoint") with AllowanceType with CapitalAllowances {
    override val identifier: Group = ZeroEmissions
  }
  case object StructuresAndBuildings extends WithName("structuresAndBuildings") with AllowanceType with CapitalAllowances {
    override val identifier: Group = BuildingsAndStructures
  }
  case object SpecialTaxSitesStructuresAndBuildings
      extends WithName("specialTaxSitesStructuresAndBuildings")
      with AllowanceType
      with CapitalAllowances {
    override val identifier: Group = BuildingsAndStructures
  }
  case object AnnualInvestment extends WithName("annualInvestment") with AllowanceType with CapitalAllowances {
    override val identifier: Group = AssetAndAllowances
  }
  case object WritingDown extends WithName("writingDown") with AllowanceType with CapitalAllowances {
    override val identifier: Group = AssetAndAllowances
  }
  case object Balancing extends WithName("balancing") with AllowanceType with CapitalAllowances {
    override val identifier: Group = AssetAndAllowances
  }
  case object BalancingCharge extends WithName("balancingCharge") with AllowanceType with CapitalAllowances {
    override val identifier: Group = AssetAndAllowances
  }

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
