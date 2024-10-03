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

package models.connector.api_1802.request

import play.api.libs.json.{Json, OFormat}

case class CreateAmendSEAnnualSubmissionRequestBody(annualAdjustments: Option[AnnualAdjustments],
                                                    annualAllowances: Option[AnnualAllowances],
                                                    annualNonFinancials: Option[AnnualNonFinancials]) {

  def replaceEmptyModelsWithNone: Option[CreateAmendSEAnnualSubmissionRequestBody] = {
    val adjustments = if (annualAdjustments.exists(_.isDefined)) annualAdjustments else None
    val allowances  = if (annualAllowances.exists(_.isDefined)) annualAllowances else None
    CreateAmendSEAnnualSubmissionRequestBody.mkRequest(adjustments, allowances, annualNonFinancials)
  }
}

object CreateAmendSEAnnualSubmissionRequestBody {
  implicit val formats: OFormat[CreateAmendSEAnnualSubmissionRequestBody] = Json.format[CreateAmendSEAnnualSubmissionRequestBody]

  def mkRequest(annualAdjustments: Option[AnnualAdjustments],
                annualAllowances: Option[AnnualAllowances],
                annualNonFinancials: Option[AnnualNonFinancials]): Option[CreateAmendSEAnnualSubmissionRequestBody] = {
    val hasAnnualAdjustments   = annualAdjustments.exists(_.isDefined)
    val hasAnnualAllowances    = annualAllowances.exists(_.isDefined)
    val hasAnnualNonFinancials = annualNonFinancials.isDefined
    val hasAtLeastOneData      = annualAdjustments.exists(_.isDefined) || annualAllowances.exists(_.isDefined) || annualNonFinancials.isDefined

    Option.when(hasAtLeastOneData)(
      CreateAmendSEAnnualSubmissionRequestBody(
        if (hasAnnualAdjustments) annualAdjustments else None,
        if (hasAnnualAllowances) annualAllowances else None,
        if (hasAnnualNonFinancials) annualNonFinancials else None
      )
    )
  }

}
