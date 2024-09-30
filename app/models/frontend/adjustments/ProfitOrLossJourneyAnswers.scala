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

package models.frontend.adjustments

import models.common.JourneyContextWithNino
import models.connector.api_1500
import models.connector.api_1500.LossType
import models.connector.api_1501
import models.connector.api_1802.request.{AnnualAdjustments, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803
import models.connector.api_1803.AnnualAdjustmentsType
import models.database.adjustments.ProfitOrLossDb
import play.api.libs.json._

case class ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse: Boolean,     // db
                                      goodsAndServicesAmount: Option[BigDecimal], // adjustments > goodsAndServicesOwnUse API 1802
                                      previousUnusedLosses: Boolean,              // db
                                      unusedLossAmount: Option[BigDecimal],       // lossAmount API 1500
                                      whichYearIsLossReported: Option[WhichYearIsLossReported]) { // taxYearBroughtForwardFrom API 1500
  def toDbAnswers: ProfitOrLossDb = ProfitOrLossDb(goodsAndServicesForYourOwnUse, previousUnusedLosses)

  def toAnnualSummariesData(ctx: JourneyContextWithNino,
                            existingAnnualSummaries: api_1803.SuccessResponseSchema): CreateAmendSEAnnualSubmissionRequestData = {
    val existingAdjustments: Option[AnnualAdjustmentsType] = existingAnnualSummaries.annualAdjustments

    val updatedAdjustments: Option[AnnualAdjustments] = (existingAdjustments, goodsAndServicesAmount) match {
      case (Some(existing), _)  => Some(existing.toApi1802AnnualAdjustments.copy(goodsAndServicesOwnUse = goodsAndServicesAmount))
      case (None, Some(amount)) => Some(AnnualAdjustmentsType.empty.toApi1802AnnualAdjustments.copy(goodsAndServicesOwnUse = Some(amount)))
      case (None, None)         => None
    }

    val submissionBody = CreateAmendSEAnnualSubmissionRequestBody(
      updatedAdjustments,
      existingAnnualSummaries.annualAllowances.map(_.toApi1802AnnualAllowance),
      existingAnnualSummaries.maybeConvertNonFinancialsTypeToNonFinancials
    )
    CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, submissionBody)
  }
}

object ProfitOrLossJourneyAnswers {
  implicit val formats: OFormat[ProfitOrLossJourneyAnswers] = Json.format[ProfitOrLossJourneyAnswers]

  def toCreateBroughtForwardLossData(ctx: JourneyContextWithNino,
                                     unusedLossAmount: BigDecimal,
                                     whichYearIsLossReported: WhichYearIsLossReported): api_1500.CreateBroughtForwardLossRequestData = {
    val updatedBroughtForwardLossBody = api_1500.CreateBroughtForwardLossRequestBody(
      incomeSourceId = ctx.businessId.value,
      lossType = LossType.Income,
      broughtForwardLossAmount = unusedLossAmount,
      taxYearBroughtForwardFrom = whichYearIsLossReported.apiTaxYear
    )
    api_1500.CreateBroughtForwardLossRequestData(ctx.nino, updatedBroughtForwardLossBody)
  }

  def toUpdateBroughtForwardLossData(ctx: JourneyContextWithNino, unusedLossAmount: BigDecimal): api_1501.UpdateBroughtForwardLossRequestData = {
    val updatedBroughtForwardLossBody = api_1501.UpdateBroughtForwardLossRequestBody(
      updatedBroughtForwardLossAmount = unusedLossAmount
    )
    api_1501.UpdateBroughtForwardLossRequestData(ctx.nino, ctx.businessId, updatedBroughtForwardLossBody)
  }
}
