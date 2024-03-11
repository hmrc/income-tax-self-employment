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

package models.frontend.capitalAllowances.zeroEmissionGoodsVehicle

import models.database.capitalAllowances.ZeroEmissionGoodsVehicleDb
import play.api.libs.json.{Format, Json}
import models.connector.api_1803

case class ZeroEmissionGoodsVehicleAnswers(zeroEmissionGoodsVehicle: Boolean,
                                           zegvAllowance: Option[Boolean],
                                           zegvTotalCostOfVehicle: Option[BigDecimal],
                                           zegvOnlyForSelfEmployment: Option[Boolean],
                                           zegvUsedOutsideSE: Option[ZegvUseOutsideSE],
                                           zegvUsedOutsideSEPercentage: Option[Int],
                                           zegvHowMuchDoYouWantToClaim: Option[ZegvHowMuchDoYouWantToClaim],
                                           zegvClaimAmount: Option[BigDecimal]) {
  def toDbModel: ZeroEmissionGoodsVehicleDb = ZeroEmissionGoodsVehicleDb(
    zeroEmissionGoodsVehicle,
    zegvAllowance,
    zegvTotalCostOfVehicle,
    zegvOnlyForSelfEmployment,
    zegvUsedOutsideSE,
    zegvUsedOutsideSEPercentage,
    zegvHowMuchDoYouWantToClaim
  )
}

object ZeroEmissionGoodsVehicleAnswers {
  implicit val formats: Format[ZeroEmissionGoodsVehicleAnswers] = Json.format[ZeroEmissionGoodsVehicleAnswers]

  def apply(dbAnswers: ZeroEmissionGoodsVehicleDb, annualSummaries: api_1803.SuccessResponseSchema): ZeroEmissionGoodsVehicleAnswers =
    new ZeroEmissionGoodsVehicleAnswers(
      zeroEmissionGoodsVehicle = dbAnswers.zeroEmissionGoodsVehicle,
      zegvAllowance = dbAnswers.zegvAllowance,
      zegvTotalCostOfVehicle = dbAnswers.zegvTotalCostOfVehicle,
      zegvOnlyForSelfEmployment = dbAnswers.zegvOnlyForSelfEmployment,
      zegvUsedOutsideSE = dbAnswers.zegvUsedOutsideSE,
      zegvUsedOutsideSEPercentage = dbAnswers.zegvUsedOutsideSEPercentage,
      zegvHowMuchDoYouWantToClaim = dbAnswers.zegvHowMuchDoYouWantToClaim,
      zegvClaimAmount = if (dbAnswers.zeroEmissionGoodsVehicle && dbAnswers.zegvAllowance.contains(true)) {
        annualSummaries.annualAllowances.flatMap(_.zeroEmissionGoodsVehicleAllowance)
      } else {
        None
      }
    )

}
