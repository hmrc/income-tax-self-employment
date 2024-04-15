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

package models.frontend.prepop

import models.connector.api_1803
import models.connector.api_1803.AnnualAdjustmentsType
import play.api.libs.json.{Json, OFormat}

case class AdjustmentsPrepopAnswers(includedNonTaxableProfits: Option[BigDecimal],
                                    basisAdjustment: Option[BigDecimal],
                                    overlapReliefUsed: Option[BigDecimal],
                                    accountingAdjustment: Option[BigDecimal],
                                    averagingAdjustment: Option[BigDecimal],
                                    lossBroughtForward: Option[BigDecimal],
                                    outstandingBusinessIncome: Option[BigDecimal],
                                    balancingChargeBpra: Option[BigDecimal],
                                    balancingChargeOther: Option[BigDecimal],
                                    goodsAndServicesOwnUse: Option[BigDecimal]) {
  def toAnnualAdjustmentsType: AnnualAdjustmentsType = AnnualAdjustmentsType(
    this.includedNonTaxableProfits,
    this.basisAdjustment,
    this.overlapReliefUsed,
    this.accountingAdjustment,
    this.averagingAdjustment,
    this.lossBroughtForward,
    this.outstandingBusinessIncome,
    this.balancingChargeBpra,
    this.balancingChargeOther,
    this.goodsAndServicesOwnUse
  )
}

object AdjustmentsPrepopAnswers {
  implicit val formats: OFormat[AdjustmentsPrepopAnswers] = Json.format[AdjustmentsPrepopAnswers]

  def apply(annualSubmissionDetails: api_1803.SuccessResponseSchema): AdjustmentsPrepopAnswers =
    AdjustmentsPrepopAnswers(
      includedNonTaxableProfits = annualSubmissionDetails.annualAdjustments.flatMap(_.includedNonTaxableProfits),
      basisAdjustment = annualSubmissionDetails.annualAdjustments.flatMap(_.basisAdjustment),
      overlapReliefUsed = annualSubmissionDetails.annualAdjustments.flatMap(_.overlapReliefUsed),
      accountingAdjustment = annualSubmissionDetails.annualAdjustments.flatMap(_.accountingAdjustment),
      averagingAdjustment = annualSubmissionDetails.annualAdjustments.flatMap(_.averagingAdjustment),
      lossBroughtForward = annualSubmissionDetails.annualAdjustments.flatMap(_.lossBroughtForward),
      outstandingBusinessIncome = annualSubmissionDetails.annualAdjustments.flatMap(_.outstandingBusinessIncome),
      balancingChargeBpra = annualSubmissionDetails.annualAdjustments.flatMap(_.balancingChargeBpra),
      balancingChargeOther = annualSubmissionDetails.annualAdjustments.flatMap(_.balancingChargeOther),
      goodsAndServicesOwnUse = annualSubmissionDetails.annualAdjustments.flatMap(_.goodsAndServicesOwnUse)
    )

  def emptyAnswers: AdjustmentsPrepopAnswers = AdjustmentsPrepopAnswers(None, None, None, None, None, None, None, None, None, None)
}
