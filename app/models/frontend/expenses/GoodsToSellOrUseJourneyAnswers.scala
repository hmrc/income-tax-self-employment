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

package models.frontend.expenses

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._

case class GoodsToSellOrUseJourneyAnswers(amount: BigDecimal, disallowableAmount: Option[BigDecimal])

object GoodsToSellOrUseJourneyAnswers {
  implicit val reads: Reads[GoodsToSellOrUseJourneyAnswers] = (
    (JsPath \ "goodsToSellOrUseAmount").read[BigDecimal] and
      (JsPath \ "disallowableGoodsToSellOrUseAmount").readNullable[BigDecimal]
  )(GoodsToSellOrUseJourneyAnswers.apply _)

  implicit val writes: OWrites[GoodsToSellOrUseJourneyAnswers] = (
    (JsPath \ "deductions" \ "costOfGoods" \ "amount").write[BigDecimal] and
      (JsPath \ "deductions" \ "costOfGoods" \ "disallowableAmount").writeNullable[BigDecimal]
  )(unlift(GoodsToSellOrUseJourneyAnswers.unapply))
}
