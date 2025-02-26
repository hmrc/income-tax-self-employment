/*
 * Copyright 2025 HM Revenue & Customs
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
import models.connector.api_1500.LossType
import models.connector.api_1802.request.AnnualAdjustments
import models.connector.{api_1500, api_1501}
import models.database.adjustments.ProfitOrLossDb
import models.frontend.FrontendAnswers
import play.api.libs.json._

case class ProfitOrLossJourneyAnswers(goodsAndServicesForYourOwnUse: Boolean,     // db
                                      goodsAndServicesAmount: Option[BigDecimal], // adjustments > goodsAndServicesOwnUse API 1802
                                      claimLossRelief: Option[Boolean],           // db
                                      whatDoYouWantToDoWithLoss: Option[Seq[WhatDoYouWantToDoWithLoss]], // API 1505
                                      carryLossForward: Option[Boolean],                                 // API 1505
                                      previousUnusedLosses: Boolean,                                     // db
                                      unusedLossAmount: Option[BigDecimal],                              // lossAmount API 1500
                                      whichYearIsLossReported: Option[WhichYearIsLossReported]) extends FrontendAnswers[ProfitOrLossDb] {

  def toDbModel: Option[ProfitOrLossDb] = Some(ProfitOrLossDb(goodsAndServicesForYourOwnUse, claimLossRelief, previousUnusedLosses))
  def toDbAnswers: ProfitOrLossDb       = ProfitOrLossDb(goodsAndServicesForYourOwnUse, claimLossRelief, previousUnusedLosses)

  override def toDownStreamAnnualAdjustments(current: Option[AnnualAdjustments]): AnnualAdjustments =
    current.getOrElse(AnnualAdjustments.empty).copy(goodsAndServicesOwnUse = goodsAndServicesAmount)
}

object ProfitOrLossJourneyAnswers {
  implicit val formats: OFormat[ProfitOrLossJourneyAnswers] = Json.format[ProfitOrLossJourneyAnswers]

  def toCreateBroughtForwardLossData(ctx: JourneyContextWithNino,
                                     unusedLossAmount: BigDecimal,
                                     whichYearIsLossReported: WhichYearIsLossReported): api_1500.CreateBroughtForwardLossRequestData = {
    val updatedBroughtForwardLossBody = api_1500.CreateBroughtForwardLossRequestBody(
      businessId = ctx.businessId.value,
      typeOfLoss = LossType.SelfEmployment,
      lossAmount = unusedLossAmount,
      taxYearBroughtForwardFrom = whichYearIsLossReported.apiTaxYear
    )
    api_1500.CreateBroughtForwardLossRequestData(ctx.nino, ctx.taxYear, updatedBroughtForwardLossBody)
  }

  def toUpdateBroughtForwardLossData(ctx: JourneyContextWithNino,
                                     lossId: String,
                                     unusedLossAmount: BigDecimal): api_1501.UpdateBroughtForwardLossRequestData = {
    val updatedBroughtForwardLossBody = api_1501.UpdateBroughtForwardLossRequestBody(
      lossAmount = unusedLossAmount
    )
    api_1501.UpdateBroughtForwardLossRequestData(ctx.nino, lossId, updatedBroughtForwardLossBody)
  }

  def toUpdateBroughtForwardLossYearData(ctx: JourneyContextWithNino,
                                         lossId: String,
                                         amount: BigDecimal,
                                         whichYear: String): api_1501.UpdateBroughtForwardLossYear = {
    val body = api_1500.CreateBroughtForwardLossRequestBody(whichYear, LossType.SelfEmployment, ctx.businessId.toString, amount)
    api_1501.UpdateBroughtForwardLossYear(ctx.nino, lossId, ctx.taxYear, body)
  }
}
