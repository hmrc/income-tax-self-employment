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
                                    accountingAdjustment: Option[BigDecimal],
                                    averagingAdjustment: Option[BigDecimal],
                                    outstandingBusinessIncome: Option[BigDecimal],
                                    balancingChargeOther: Option[BigDecimal],
                                    goodsAndServicesOwnUse: Option[BigDecimal],
                                    transitionProfitAmount: Option[BigDecimal],
                                    transitionProfitAccelerationAmount: Option[BigDecimal]) {}

object AdjustmentsPrepopAnswers {
  implicit val formats: OFormat[AdjustmentsPrepopAnswers] = Json.format[AdjustmentsPrepopAnswers]

  def apply(annualSubmissionDetails: api_1803.SuccessResponseSchema): AdjustmentsPrepopAnswers =
    AdjustmentsPrepopAnswers(
      includedNonTaxableProfits = annualSubmissionDetails.annualAdjustments.flatMap(_.includedNonTaxableProfits),
      accountingAdjustment = annualSubmissionDetails.annualAdjustments.flatMap(_.accountingAdjustment),
      averagingAdjustment = annualSubmissionDetails.annualAdjustments.flatMap(_.averagingAdjustment),
      outstandingBusinessIncome = annualSubmissionDetails.annualAdjustments.flatMap(_.outstandingBusinessIncome),
      balancingChargeOther = annualSubmissionDetails.annualAdjustments.flatMap(_.balancingChargeOther),
      goodsAndServicesOwnUse = annualSubmissionDetails.annualAdjustments.flatMap(_.goodsAndServicesOwnUse),
      transitionProfitAmount = annualSubmissionDetails.annualAdjustments.flatMap(_.transitionProfitAmount),
      transitionProfitAccelerationAmount = annualSubmissionDetails.annualAdjustments.flatMap(_.transitionProfitAccelerationAmount)
    )

  def emptyAnswers: AdjustmentsPrepopAnswers = AdjustmentsPrepopAnswers(None, None, None, None, None, None, None, None)

  def fromAnnualAdjustmentsType(answers: AnnualAdjustmentsType): AdjustmentsPrepopAnswers = AdjustmentsPrepopAnswers(
    answers.includedNonTaxableProfits,
    answers.accountingAdjustment,
    answers.averagingAdjustment,
    answers.outstandingBusinessIncome,
    answers.balancingChargeOther,
    answers.goodsAndServicesOwnUse,
    answers.transitionProfitAmount,
    answers.transitionProfitAccelerationAmount
  )
}
