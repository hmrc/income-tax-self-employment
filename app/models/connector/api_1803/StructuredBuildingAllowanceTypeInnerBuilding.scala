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

package models.connector.api_1803

import models.connector.api_1802.request.Building
import play.api.libs.json._

/** Post code is mandatory and minimum one of name and number field is required.
  */
case class StructuredBuildingAllowanceTypeInnerBuilding(name: Option[String], number: Option[String], postCode: String) {

  def toBuilding: Building = Building(name, number, postCode)
}

object StructuredBuildingAllowanceTypeInnerBuilding {
  implicit lazy val structuredBuildingAllowanceTypeInnerBuildingJsonFormat: Format[StructuredBuildingAllowanceTypeInnerBuilding] =
    Json.format[StructuredBuildingAllowanceTypeInnerBuilding]
}
