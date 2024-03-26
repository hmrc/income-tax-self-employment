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

package models.frontend.capitalAllowances.specialTaxSites

import models.connector.api_1802.request.{BuildingAllowance, FirstYear}
import models.connector.dateFormatter
import models.database.capitalAllowances.NewSpecialTaxSiteDb
import play.api.libs.json.{Format, Json}

import java.time.LocalDate

case class NewSpecialTaxSite(contractForBuildingConstruction: Option[Boolean],
                             contractStartDate: Option[LocalDate], // TODO: Not mapped yet, waiting for Business where it should be send
                             constructionStartDate: Option[LocalDate], // TODO: Not mapped yet, waiting for Business where it should be send
                             qualifyingUseStartDate: Option[LocalDate],
                             specialTaxSiteLocation: Option[SpecialTaxSiteLocation],
                             newSiteClaimingAmount: Option[BigDecimal]) {

  def toDbModel: NewSpecialTaxSiteDb = NewSpecialTaxSiteDb(
    contractForBuildingConstruction,
    contractStartDate,
    constructionStartDate
  )

  private def toFirstYear: Option[FirstYear] =
    for {
      startDate   <- qualifyingUseStartDate
      claimAmount <- newSiteClaimingAmount
    } yield FirstYear(qualifyingDate = startDate.format(dateFormatter), qualifyingAmountExpenditure = claimAmount)

  def toBuildingAllowance: Option[BuildingAllowance] =
    specialTaxSiteLocation.map(_.toBuilding).map { location =>
      BuildingAllowance(amount = newSiteClaimingAmount.getOrElse(BigDecimal(0)), firstYear = toFirstYear, building = location)
    }

}

object NewSpecialTaxSite {
  implicit val format: Format[NewSpecialTaxSite] = Json.format[NewSpecialTaxSite]
}
