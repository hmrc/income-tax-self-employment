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

import models.connector.api_1786
import play.api.libs.json.{Json, OFormat}

case class IncomePrepopAnswers(turnoverIncome: Option[BigDecimal], otherIncome: Option[BigDecimal])

object IncomePrepopAnswers {
  implicit val formats: OFormat[IncomePrepopAnswers] = Json.format[IncomePrepopAnswers]

  def apply(periodicSummaryDetails: api_1786.SuccessResponseSchema): IncomePrepopAnswers =
    IncomePrepopAnswers(
      turnoverIncome = periodicSummaryDetails.financials.incomes.flatMap(_.turnover),
      otherIncome = periodicSummaryDetails.financials.incomes.flatMap(_.other)
    )
}
