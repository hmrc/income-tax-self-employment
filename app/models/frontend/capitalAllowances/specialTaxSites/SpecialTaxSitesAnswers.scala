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
import models.database.capitalAllowances.SpecialTaxSitesDb
import play.api.libs.json.{Format, Json}

case class SpecialTaxSitesAnswers(specialTaxSites: Boolean,
                                  newSpecialTaxSites: List[NewSpecialTaxSite],
                                  haveYouUsedStsAllowanceBefore: Boolean,
                                  continueClaimingAllowanceForExistingSite: Boolean,
                                  existingSiteClaimingAmount: BigDecimal) {
  def toDbModel: SpecialTaxSitesDb = SpecialTaxSitesDb(
    specialTaxSites,
    newSpecialTaxSites.map(_.toDbModel),
    haveYouUsedStsAllowanceBefore,
    continueClaimingAllowanceForExistingSite
  )

  def toDownStream(current: Option[AnnualAllowances]): AnnualAllowances = {
    val enhancedStructuredBuildingAllowance = if (specialTaxSites) {
      val updated = newSpecialTaxSites.map { site =>
        site.toBuildingAllowance(existingSiteClaimingAmount)
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

object SpecialTaxSitesAnswers {
  implicit val formats: Format[SpecialTaxSitesAnswers] = Json.format[SpecialTaxSitesAnswers]
}
