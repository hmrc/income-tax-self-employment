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

import models.database.capitalAllowances.ZeroEmissionCarsDb
import play.api.libs.json.{Format, Json, OFormat}

case class ZeroEmissionCarsJourneyAnswers(zeroEmissionsCarAllowance: BigDecimal)

object ZeroEmissionCarsJourneyAnswers {
  implicit val formats: OFormat[ZeroEmissionCarsJourneyAnswers] = Json.format[ZeroEmissionCarsJourneyAnswers]
}

case class ZeroEmissionCarsAnswers(zeroEmissionCarsUsedForWork: Boolean,
                                   zeroEmissionCarsAllowance: ZeroEmissionCarsAllowance,
                                   zeroEmissionCarsTotalCostOfCar: BigDecimal,
                                   zeroEmissionCarsOnlyForSelfEmployment: ZecOnlyForSelfEmployment,
                                   zeroEmissionCarsUsedOutsideSE: ZecUseOutsideSE,
                                   zeroEmissionCarsUsedOutsideSEPercentage: Int,
                                   zecHowMuchDoYouWantToClaim: ZecHowMuchDoYouWantToClaim,
                                   zeroEmissionCarsClaimAmount: BigDecimal) {

  def toApiSubmissionModel: ZeroEmissionCarsJourneyAnswers = ZeroEmissionCarsJourneyAnswers(zeroEmissionsCarAllowance = zeroEmissionCarsClaimAmount)

  def toDbModel: ZeroEmissionCarsDb = ZeroEmissionCarsDb(
    zeroEmissionCarsUsedForWork,
    zeroEmissionCarsAllowance,
    zeroEmissionCarsTotalCostOfCar,
    zeroEmissionCarsOnlyForSelfEmployment,
    zeroEmissionCarsUsedOutsideSE,
    zeroEmissionCarsUsedOutsideSEPercentage,
    zecHowMuchDoYouWantToClaim
  )
}

object ZeroEmissionCarsAnswers {
  implicit val formats: Format[ZeroEmissionCarsAnswers] = Json.format[ZeroEmissionCarsAnswers]
}
