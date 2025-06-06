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

package data.journeyAnswersData

import models.database.expenses.TaxiMinicabOrRoadHaulageDb
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}

object GoodsToSellOrUserTestAnswers {

  val goodsToSellOrUseAnswers: GoodsToSellOrUseAnswers = GoodsToSellOrUseAnswers(
    taxiMinicabOrRoadHaulage = true,
    goodsToSellOrUseAmount = 1000.00,
    disallowableGoodsToSellOrUseAmount = Some(3495.82)
  )

  val goodsToSellOrUseJourneyAnswers: GoodsToSellOrUseJourneyAnswers = GoodsToSellOrUseJourneyAnswers(
    goodsToSellOrUseAmount = 1000.00,
    disallowableGoodsToSellOrUseAmount = Some(3495.82)
  )
  val taxiMinicabOrRoadHaulageDb: TaxiMinicabOrRoadHaulageDb = TaxiMinicabOrRoadHaulageDb(true)

}
