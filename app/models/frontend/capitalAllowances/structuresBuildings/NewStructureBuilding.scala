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

package models.frontend.capitalAllowances.structuresBuildings

import models.connector.api_1802.request.{BuildingAllowance, FirstYear}
import models.connector.dateFormatter
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class NewStructureBuilding(qualifyingUse: Option[LocalDate] = None,
                                newStructureBuildingLocation: Option[StructuresBuildingsLocation] = None,
                                newStructureBuildingClaimingAmount: Option[BigDecimal] = None) {
  private def toFirstYear: Option[FirstYear] =
    qualifyingUse.map(startDate =>
      FirstYear(
        qualifyingDate = startDate.format(dateFormatter),
        qualifyingAmountExpenditure = BigDecimal(0.0)
      )) // TODO Once the missing page is added remove 0.0

  def toBuildingAllowance: Option[BuildingAllowance] =
    for {
      location <- newStructureBuildingLocation.map(_.toBuilding)
      amount   <- newStructureBuildingClaimingAmount
    } yield BuildingAllowance(amount = amount, firstYear = toFirstYear, building = location)
}

object NewStructureBuilding {
  implicit val formats: Format[NewStructureBuilding] = Json.format[NewStructureBuilding]
}
