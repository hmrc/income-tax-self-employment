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

package models.database.capitalAllowances

import models.frontend.capitalAllowances.zeroEmissionCars.{
  ZecHowMuchDoYouWantToClaim,
  ZecOnlyForSelfEmployment,
  ZecUseOutsideSE,
  ZeroEmissionCarsAllowance
}
import play.api.libs.json.{Json, OFormat}

final case class ZeroEmissionCarsDb(zeroEmissionCarsUsedForWork: Boolean,
                                    zeroEmissionCarsAllowance: ZeroEmissionCarsAllowance,
                                    zeroEmissionCarsTotalCostOfCar: BigDecimal,
                                    zeroEmissionCarsOnlyForSelfEmployment: ZecOnlyForSelfEmployment,
                                    zeroEmissionCarsUsedOutsideSE: ZecUseOutsideSE,
                                    zeroEmissionCarsUsedOutsideSEPercentage: Int,
                                    zecHowMuchDoYouWantToClaim: ZecHowMuchDoYouWantToClaim)

object ZeroEmissionCarsDb {
  implicit val format: OFormat[ZeroEmissionCarsDb] = Json.format[ZeroEmissionCarsDb]
}
