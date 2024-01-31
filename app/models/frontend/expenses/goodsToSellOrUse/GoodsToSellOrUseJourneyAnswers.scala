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

package models.frontend.expenses.goodsToSellOrUse

import models.connector.Api1786ExpensesResponseParser.noneFound
import models.connector.api_1786
import models.database.expenses.TaxiMinicabOrRoadHaulageDb
import play.api.libs.json._

case class GoodsToSellOrUseJourneyAnswers(goodsToSellOrUseAmount: BigDecimal, disallowableGoodsToSellOrUseAmount: Option[BigDecimal])

object GoodsToSellOrUseJourneyAnswers {
  implicit val formats: OFormat[GoodsToSellOrUseJourneyAnswers] = Json.format[GoodsToSellOrUseJourneyAnswers]
}

case class GoodsToSellOrUseAnswers(taxiMinicabOrRoadHaulage: TaxiMinicabOrRoadHaulage,
                                   goodsToSellOrUseAmount: BigDecimal,
                                   disallowableGoodsToSellOrUseAmount: Option[BigDecimal])

object GoodsToSellOrUseAnswers {
  implicit val formats: OFormat[GoodsToSellOrUseAnswers] = Json.format[GoodsToSellOrUseAnswers]

  def apply(dbTaxiAnswer: TaxiMinicabOrRoadHaulageDb, periodicSummaryDetails: api_1786.SuccessResponseSchema): GoodsToSellOrUseAnswers =
    GoodsToSellOrUseAnswers(
      taxiMinicabOrRoadHaulage = dbTaxiAnswer.taxiMinicabOrRoadHaulage,
      goodsToSellOrUseAmount = periodicSummaryDetails.financials.deductions.flatMap(_.costOfGoods.map(_.amount)).getOrElse(noneFound),
      disallowableGoodsToSellOrUseAmount = periodicSummaryDetails.financials.deductions.flatMap(_.costOfGoods.flatMap(_.disallowableAmount))
    )
}
