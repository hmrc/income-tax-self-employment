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

package models.frontend.capitalAllowances.electricVehicleChargePoints

import models.connector.api_1803
import models.database.capitalAllowances.ElectricVehicleChargePointsDb
import play.api.libs.json.{Json, OFormat}

case class ElectricVehicleChargePointsJourneyAnswers(electricChargePointAllowance: BigDecimal)

object ElectricVehicleChargePointsJourneyAnswers {
  implicit val formats: OFormat[ElectricVehicleChargePointsJourneyAnswers] = Json.format[ElectricVehicleChargePointsJourneyAnswers]
}

case class ElectricVehicleChargePointsAnswers(evcpAllowance: Boolean,
                                              chargePointTaxRelief: Option[Boolean],
                                              amountSpentOnEvcp: Option[BigDecimal],
                                              evcpOnlyForSelfEmployment: Option[EvcpOnlyForSelfEmployment],
                                              evcpUsedOutsideSE: Option[EvcpUseOutsideSE],
                                              evcpUsedOutsideSEPercentage: Option[Int],
                                              evcpHowMuchDoYouWantToClaim: Option[EvcpHowMuchDoYouWantToClaim],
                                              evcpClaimAmount: Option[BigDecimal]) {

  def toDbModel: ElectricVehicleChargePointsDb = ElectricVehicleChargePointsDb(
    evcpAllowance,
    chargePointTaxRelief,
    amountSpentOnEvcp,
    evcpOnlyForSelfEmployment,
    evcpUsedOutsideSE,
    evcpUsedOutsideSEPercentage,
    evcpHowMuchDoYouWantToClaim
  )
}

object ElectricVehicleChargePointsAnswers {
  implicit val formats: OFormat[ElectricVehicleChargePointsAnswers] = Json.format[ElectricVehicleChargePointsAnswers]

  def apply(dbAnswers: ElectricVehicleChargePointsDb, annualSummaries: api_1803.SuccessResponseSchema): ElectricVehicleChargePointsAnswers =
    new ElectricVehicleChargePointsAnswers(
      dbAnswers.evcpAllowance,
      dbAnswers.chargePointTaxRelief,
      dbAnswers.amountSpentOnEvcp,
      dbAnswers.evcpOnlyForSelfEmployment,
      dbAnswers.evcpUsedOutsideSE,
      dbAnswers.evcpUsedOutsideSEPercentage,
      dbAnswers.evcpHowMuchDoYouWantToClaim,
      evcpClaimAmount = (dbAnswers.evcpAllowance, dbAnswers.chargePointTaxRelief) match {
        case (true, Some(true)) => annualSummaries.annualAllowances.flatMap(_.electricChargePointAllowance)
        case _                  => None
      }
    )
}
