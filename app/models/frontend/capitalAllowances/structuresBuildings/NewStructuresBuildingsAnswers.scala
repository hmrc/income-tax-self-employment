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

import models.connector.api_1802.request.AnnualAllowances
import models.connector.{api_1803, dateFormatter}
import models.database.capitalAllowances.NewStructuresBuildingsDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

final case class NewStructuresBuildingsAnswers(
    structuresBuildingsAllowance: Boolean,
    structuresBuildingsEligibleClaim: Option[Boolean],
    structuresBuildingsPreviousClaimUse: Option[Boolean],
    structuresBuildingsClaimed: Option[Boolean],
    newStructuresBuildings: Option[List[NewStructureBuilding]]
) extends FrontendAnswers[NewStructuresBuildingsDb] {
  def toDbModel: Option[NewStructuresBuildingsDb] = Some(
    NewStructuresBuildingsDb(
      structuresBuildingsAllowance,
      structuresBuildingsEligibleClaim,
      structuresBuildingsPreviousClaimUse,
      structuresBuildingsClaimed
    ))

  override def toDownStreamAnnualAllowances(current: Option[AnnualAllowances]): AnnualAllowances = {
    val buildingAllowance = if (structuresBuildingsAllowance) {
      val updated = newStructuresBuildings.getOrElse(Nil).map { structure =>
        structure.toBuildingAllowance
      }
      Some(updated.flatten)
    } else {
      None
    }

    current
      .getOrElse(AnnualAllowances.empty)
      .copy(structuredBuildingAllowance = buildingAllowance)
  }
}

object NewStructuresBuildingsAnswers {
  implicit val format: Format[NewStructuresBuildingsAnswers] = Json.format[NewStructuresBuildingsAnswers]

  def apply(dbModel: NewStructuresBuildingsDb, annualSummaries: api_1803.SuccessResponseSchema): NewStructuresBuildingsAnswers = {
    val structuredBuildingAllowance = annualSummaries.annualAllowances.flatMap(_.structuredBuildingAllowance)
    val buildings = structuredBuildingAllowance.map(_.map { allowance =>
      NewStructureBuilding(
        allowance.firstYear.map(_.qualifyingDate).map(date => LocalDate.parse(date, dateFormatter)),
        Some(StructuresBuildingsLocation.apply(allowance.building)),
        Some(allowance.amount)
      )
    })

    new NewStructuresBuildingsAnswers(
      structuresBuildingsAllowance = dbModel.structuresBuildingsAllowance,
      structuresBuildingsEligibleClaim = dbModel.structuresBuildingsEligibleClaim,
      structuresBuildingsPreviousClaimUse = dbModel.structuresBuildingsPreviousClaimUse,
      structuresBuildingsClaimed = dbModel.structuresBuildingsClaimed,
      newStructuresBuildings = buildings
    )
  }
}
