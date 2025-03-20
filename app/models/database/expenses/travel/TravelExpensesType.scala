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

sealed trait TravelExpenseType

case object OwnVehicles     extends TravelExpenseType
case object LeasedVehicles  extends TravelExpenseType
case object PublicTransport extends TravelExpenseType

object TravelExpenseType {

  private val ownVehicles: String     = "OwnVehicles"
  private val leasedVehicles: String  = "LeasedVehicles"
  private val publicTransport: String = "PublicTransport"

  implicit val format: Format[TravelExpenseType] = Format[TravelExpenseType](
    {
      case JsString(`ownVehicles`)     => JsSuccess(OwnVehicles)
      case JsString(`leasedVehicles`)  => JsSuccess(LeasedVehicles)
      case JsString(`publicTransport`) => JsSuccess(PublicTransport)
      case _                           => JsError("Invalid TravelExpenseType")
    },
    {
      case OwnVehicles     => JsString(`ownVehicles`)
      case LeasedVehicles  => JsString(`leasedVehicles`)
      case PublicTransport => JsString(`publicTransport`)
    }
  )

}
