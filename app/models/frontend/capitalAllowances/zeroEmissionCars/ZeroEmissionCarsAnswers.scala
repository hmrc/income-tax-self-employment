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

package models.frontend.capitalAllowances.zeroEmissionCars

import models.connector.api_1802.request.AnnualAllowances
import models.connector.api_1803
import models.database.capitalAllowances.ZeroEmissionCarsDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Json, OFormat}

final case class ZeroEmissionCarsAnswers(zeroEmissionCars: Boolean,
                                         zecAllowance: Option[Boolean],
                                         zecTotalCostOfCar: Option[BigDecimal],
                                         zecOnlyForSelfEmployment: Option[Boolean],
                                         zecUsedOutsideSE: Option[ZecUseOutsideSE],
                                         zecUsedOutsideSEPercentage: Option[Int],
                                         zecHowMuchDoYouWantToClaim: Option[ZecHowMuchDoYouWantToClaim],
                                         zecClaimAmount: Option[BigDecimal])
    extends FrontendAnswers[ZeroEmissionCarsDb] {

  def toDbModel: Option[ZeroEmissionCarsDb] = Some(
    ZeroEmissionCarsDb(
      zeroEmissionCars,
      zecAllowance,
      zecTotalCostOfCar,
      zecOnlyForSelfEmployment,
      zecUsedOutsideSE,
      zecUsedOutsideSEPercentage,
      zecHowMuchDoYouWantToClaim
    ))

  def toDownStream(current: Option[AnnualAllowances]): AnnualAllowances =
    current.getOrElse(AnnualAllowances.empty).copy(zeroEmissionsCarAllowance = zecClaimAmount)
}

object ZeroEmissionCarsAnswers {
  implicit val formats: OFormat[ZeroEmissionCarsAnswers] = Json.format[ZeroEmissionCarsAnswers]

  def apply(dbAnswers: ZeroEmissionCarsDb, annualSummaries: api_1803.SuccessResponseSchema): ZeroEmissionCarsAnswers =
    new ZeroEmissionCarsAnswers(
      zeroEmissionCars = dbAnswers.zeroEmissionCars,
      zecAllowance = dbAnswers.zecAllowance,
      zecTotalCostOfCar = dbAnswers.zecTotalCostOfCar,
      zecOnlyForSelfEmployment = dbAnswers.zecOnlyForSelfEmployment,
      zecUsedOutsideSE = dbAnswers.zecUsedOutsideSE,
      zecUsedOutsideSEPercentage = dbAnswers.zecUsedOutsideSEPercentage,
      zecHowMuchDoYouWantToClaim = dbAnswers.zecHowMuchDoYouWantToClaim,
      zecClaimAmount = if (dbAnswers.zeroEmissionCars && dbAnswers.zecAllowance.getOrElse(false)) {
        annualSummaries.annualAllowances.flatMap(_.zeroEmissionsCarAllowance)
      } else {
        None
      }
    )
}
