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

package models.connector.api_1786

import models.connector.api_1895.request.AmendSEPeriodSummaryRequestBody
import models.connector.api_1894
import play.api.libs.json._

/** Represents the Swagger definition for financialsType.
  */
case class FinancialsType(
    deductions: Option[DeductionsType],
    incomes: Option[IncomesType]
) {
  def toApi1895: AmendSEPeriodSummaryRequestBody =
    AmendSEPeriodSummaryRequestBody(
      incomes.map(_.toApi1895),
      deductions.map(_.toApi1895)
    )

  def toApi1894: api_1894.request.FinancialsType = api_1894.request.FinancialsType(
    incomes.map(_.toApi1894),
    deductions.map(_.toApi1894)
  )
}

object FinancialsType {
  implicit lazy val financialsTypeJsonFormat: Format[FinancialsType] = Json.format[FinancialsType]
}
