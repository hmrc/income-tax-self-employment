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

package models.database.capitalAllowances

import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE}
import play.api.libs.json.{Json, OFormat}

final case class ZeroEmissionGoodsVehicleDb(zeroEmissionGoodsVehicle: Boolean,
                                            zegvAllowance: Option[Boolean],
                                            zegvTotalCostOfVehicle: Option[BigDecimal],
                                            zegvOnlyForSelfEmployment: Option[Boolean],
                                            zegvUsedOutsideSE: Option[ZegvUseOutsideSE],
                                            zegvUsedOutsideSEPercentage: Option[Int],
                                            zegvHowMuchDoYouWantToClaim: Option[ZegvHowMuchDoYouWantToClaim])

object ZeroEmissionGoodsVehicleDb {
  implicit val format: OFormat[ZeroEmissionGoodsVehicleDb] = Json.format[ZeroEmissionGoodsVehicleDb]
}
