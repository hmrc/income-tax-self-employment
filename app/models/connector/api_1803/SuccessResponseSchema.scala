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

import models.connector.api_1802.request.{AnnualNonFinancials, CreateAmendSEAnnualSubmissionRequestBody}
import play.api.libs.json._

/** Represents the Swagger definition for successResponseSchema.
  */
case class SuccessResponseSchema(annualAdjustments: Option[AnnualAdjustmentsType],
                                 annualAllowances: Option[AnnualAllowancesType],
                                 annualNonFinancials: Option[AnnualNonFinancialsType]) {
  def hasNICsClassFourData: Boolean = annualNonFinancials.exists(_.exemptFromPayingClass4Nics.isDefined)

  def toRequestBody: CreateAmendSEAnnualSubmissionRequestBody = CreateAmendSEAnnualSubmissionRequestBody(
    annualAdjustments = annualAdjustments.map(_.toApi1802AnnualAdjustments),
    annualAllowances = annualAllowances.map(_.toApi1802AnnualAllowance),
    annualNonFinancials = annualNonFinancials.flatMap(
      _.exemptFromPayingClass4Nics.map(AnnualNonFinancials(_, annualNonFinancials.flatMap(_.class4NicsExemptionReason.map(_.toString)))))
  )
}

object SuccessResponseSchema {
  implicit lazy val successResponseSchemaJsonFormat: Format[SuccessResponseSchema] = Json.format[SuccessResponseSchema]

  val empty: SuccessResponseSchema = SuccessResponseSchema(None, None, None)
}
