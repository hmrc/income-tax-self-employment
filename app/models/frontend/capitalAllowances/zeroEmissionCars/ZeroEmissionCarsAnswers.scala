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

import models.connector.api_1803
import models.database.capitalAllowances.ZeroEmissionCarsDb
import play.api.libs.json.{Json, OFormat}

case class ZeroEmissionCarsJourneyAnswers(zeroEmissionsCarAllowance: BigDecimal)

object ZeroEmissionCarsJourneyAnswers {
  implicit val formats: OFormat[ZeroEmissionCarsJourneyAnswers] = Json.format[ZeroEmissionCarsJourneyAnswers]
}

case class ZeroEmissionCarsAnswers(zeroEmissionCars: Boolean,
                                   zecAllowance: Option[ZeroEmissionCarsAllowance],
                                   zecTotalCostOfCar: Option[BigDecimal],
                                   zecOnlyForSelfEmployment: Option[ZecOnlyForSelfEmployment],
                                   zecUsedOutsideSE: Option[ZecUseOutsideSE],
                                   zecUsedOutsideSEPercentage: Option[Int],
                                   zecHowMuchDoYouWantToClaim: Option[ZecHowMuchDoYouWantToClaim],
                                   zecClaimAmount: Option[BigDecimal]) {

  def toDbModel: ZeroEmissionCarsDb = ZeroEmissionCarsDb(
    zeroEmissionCars,
    zecAllowance,
    zecTotalCostOfCar,
    zecOnlyForSelfEmployment,
    zecUsedOutsideSE,
    zecUsedOutsideSEPercentage,
    zecHowMuchDoYouWantToClaim
  )
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
      zecClaimAmount = annualSummaries.annualAllowances.flatMap(_.zeroEmissionsCarAllowance)
    )
}
