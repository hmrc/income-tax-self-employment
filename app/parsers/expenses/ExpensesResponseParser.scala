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

package parsers.expenses

import models.connector._
import models.connector.api_1786.SuccessResponseSchema
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import utils.ResponseParser

trait ExpensesResponseParser[Result] extends ResponseParser[api_1786.SuccessResponseSchema, Result] {
  override def parse(response: api_1786.SuccessResponseSchema): Result
}

object ExpensesResponseParser {
  implicit val goodsToSellOrUseParser: ExpensesResponseParser[GoodsToSellOrUseJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      GoodsToSellOrUseJourneyAnswers(
        goodsToSellOrUseAmount = response.financials.deductions.flatMap(_.costOfGoods.map(_.amount)).getOrElse(0), // TODO: What if it's None?
        disallowableGoodsToSellOrUseAmount = response.financials.deductions.flatMap(_.costOfGoods.flatMap(_.disallowableAmount))
      )

  implicit val officeSuppliesParser: ExpensesResponseParser[OfficeSuppliesJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      OfficeSuppliesJourneyAnswers(
        officeSuppliesAmount = response.financials.deductions.flatMap(_.adminCosts.map(_.amount)).getOrElse(0), // TODO: What if it's None?
        officeSuppliesDisallowableAmount = response.financials.deductions.flatMap(_.adminCosts.flatMap(_.disallowableAmount))
      )
}
