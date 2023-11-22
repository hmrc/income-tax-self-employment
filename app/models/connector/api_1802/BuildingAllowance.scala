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

package models.connector.api_1802

import play.api.libs.json.{Json, OFormat}

case class BuildingAllowance(amount: BigDecimal, firstYear: Option[FirstYear], building: Building)

object BuildingAllowance {
  implicit val format: OFormat[BuildingAllowance] = Json.format[BuildingAllowance]
}
