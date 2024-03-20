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

package models.connector

import models.connector.api_1802.request.AnnualAllowances
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceJourneyAnswers
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceJourneyAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.ElectricVehicleChargePointsJourneyAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsJourneyAnswers

trait Api1802AnnualAllowancesBuilder[A] {
  def build(answers: A): AnnualAllowances
}

/** Set of converters which convert from a journey answer to a Downstream objects.
  */
object Api1802AnnualAllowancesBuilder {

  implicit val zeroEmissionCars: Api1802AnnualAllowancesBuilder[ZeroEmissionCarsJourneyAnswers] =
    (answers: ZeroEmissionCarsJourneyAnswers) => AnnualAllowances.empty.copy(zeroEmissionsCarAllowance = Some(answers.zeroEmissionsCarAllowance))

  implicit val electricVehicleChargePoints: Api1802AnnualAllowancesBuilder[ElectricVehicleChargePointsJourneyAnswers] =
    (answers: ElectricVehicleChargePointsJourneyAnswers) =>
      AnnualAllowances.empty.copy(electricChargePointAllowance = Some(answers.electricChargePointAllowance))

  implicit val balancingAllowance: Api1802AnnualAllowancesBuilder[BalancingAllowanceJourneyAnswers] =
    (answers: BalancingAllowanceJourneyAnswers) => AnnualAllowances.empty.copy(allowanceOnSales = Some(answers.allowanceOnSales))

  implicit val annualInvestmentAllowance: Api1802AnnualAllowancesBuilder[AnnualInvestmentAllowanceJourneyAnswers] =
    (answers: AnnualInvestmentAllowanceJourneyAnswers) =>
      AnnualAllowances.empty.copy(annualInvestmentAllowance = Some(answers.annualInvestmentAllowance))

}
