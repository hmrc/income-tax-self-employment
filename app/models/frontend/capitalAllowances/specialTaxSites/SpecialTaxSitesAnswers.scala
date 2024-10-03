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

import models.connector.api_1802.request.AnnualAllowances
import models.connector.{api_1803, dateFormatter}
import models.database.capitalAllowances.SpecialTaxSitesDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Format, Json}
import utils.Logging

import java.time.LocalDate

case class SpecialTaxSitesAnswers(
    specialTaxSites: Boolean,
    newSpecialTaxSites: Option[List[NewSpecialTaxSite]],
    doYouHaveAContinuingClaim: Option[Boolean],                // TODO, we ignore this question until business decide what to do
    continueClaimingAllowanceForExistingSite: Option[Boolean], // TODO, we ignore this question until business decide what to do
    existingSiteClaimingAmount: Option[BigDecimal]             // TODO, we ignore this question until business decide what to do
) extends FrontendAnswers[SpecialTaxSitesDb] {
  def toDbModel: Option[SpecialTaxSitesDb] = Some(
    SpecialTaxSitesDb(
      specialTaxSites,
      newSpecialTaxSites.map(sites => sites.flatMap(_.toDbModel))
    ))

  override def toDownStreamAnnualAllowances(current: Option[AnnualAllowances]): AnnualAllowances = {
    val enhancedStructuredBuildingAllowance = if (specialTaxSites) {
      val updated = newSpecialTaxSites.getOrElse(Nil).map { site =>
        site.toBuildingAllowance
      }
      Some(updated.flatten)
    } else {
      None
    }

    current
      .getOrElse(AnnualAllowances.empty)
      .copy(enhancedStructuredBuildingAllowance = enhancedStructuredBuildingAllowance)
  }
}

object SpecialTaxSitesAnswers extends Logging {
  implicit val format: Format[SpecialTaxSitesAnswers] = Json.format[SpecialTaxSitesAnswers]

  def apply(dbModel: SpecialTaxSitesDb, annualSummaries: api_1803.SuccessResponseSchema): SpecialTaxSitesAnswers = {
    val enhancedStructuredBuildingAllowance = annualSummaries.annualAllowances.flatMap(_.enhancedStructuredBuildingAllowance).getOrElse(Nil)
    val sitesFromDb                         = dbModel.newSpecialTaxSites

    // TODO Get back to this code at the end once we figure out what we do when data change out of our sight (via Software etc.)
    if (enhancedStructuredBuildingAllowance.size != sitesFromDb.size) {
      logger.warn("Mismatch between the number of special tax sites in the database and the number of special tax sites in the response from the API")
    }

    val newSpecialTaxSites = sitesFromDb.getOrElse(Nil).zip(enhancedStructuredBuildingAllowance).map { case (site, buildingAllowance) =>
      NewSpecialTaxSite(
        contractForBuildingConstruction = site.contractForBuildingConstruction,
        contractStartDate = site.contractStartDate,
        constructionStartDate = site.constructionStartDate,
        qualifyingUseStartDate = buildingAllowance.firstYear.map(_.qualifyingDate).map(date => LocalDate.parse(date, dateFormatter)),
        specialTaxSiteLocation = Some(SpecialTaxSiteLocation.apply(buildingAllowance.building)),
        newSiteClaimingAmount = buildingAllowance.firstYear.map(_.qualifyingAmountExpenditure)
      )
    }

    new SpecialTaxSitesAnswers(
      specialTaxSites = dbModel.specialTaxSites,
      newSpecialTaxSites = Some(newSpecialTaxSites),
      doYouHaveAContinuingClaim = None,
      continueClaimingAllowanceForExistingSite = None,
      existingSiteClaimingAmount = None
    )
  }
}
