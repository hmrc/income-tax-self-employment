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

package models.connector.api_1867

import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

sealed trait ReliefClaimType {
  val value: String
}

trait SelfEmploymentClaim
trait PropertyClaim

case object CarryForward extends ReliefClaimType with SelfEmploymentClaim {
  val value: String = "CF"
}

case object CarrySideways extends ReliefClaimType with SelfEmploymentClaim {
  val value: String = "CSGI"
}

case object CarryForwardsToCarrySideways extends ReliefClaimType with PropertyClaim {
  val value: String = "CFCSGI"
}

case object CarrySidewaysFHL extends ReliefClaimType with PropertyClaim {
  val value: String = "CSFHL"
}

object ReliefClaimType {
  implicit val format: Format[ReliefClaimType] = Format(
    {
      case JsString(CarryForward.value)                 => JsSuccess(CarryForward)
      case JsString(CarrySideways.value)                => JsSuccess(CarrySideways)
      case JsString(CarryForwardsToCarrySideways.value) => JsSuccess(CarryForwardsToCarrySideways)
      case JsString(CarrySidewaysFHL.value)             => JsSuccess(CarrySidewaysFHL)
      case invalid                                      => JsError(s"Invalid relief claim type: $invalid")
    },
    reliefClaimType => JsString(reliefClaimType.value)
  )
}
