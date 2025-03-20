/*
 * Copyright 2025 HM Revenue & Customs
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

package models.database.expenses.travel

import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

sealed trait VehicleType
case object CarOrGoodsVehicle extends VehicleType
case object Motorcycle        extends VehicleType

object VehicleType {

  private val carOrGoodsVehicle: String = "CarOrGoodsVehicle"
  private val motorcycle: String        = "Motorcycle"

  implicit val format: Format[VehicleType] = Format(
    {
      case JsString(`carOrGoodsVehicle`) => JsSuccess(CarOrGoodsVehicle)
      case JsString(`motorcycle`)        => JsSuccess(Motorcycle)
      case _                             => JsError("Invalid VehicleType")
    },
    {
      case CarOrGoodsVehicle => JsString(`carOrGoodsVehicle`)
      case Motorcycle        => JsString(`motorcycle`)
    }
  )

}
