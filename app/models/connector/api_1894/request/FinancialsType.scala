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

package models.connector.api_1894.request

import cats.implicits.catsSyntaxOptionId
import models.connector.Api1894DeductionsBuilder
import play.api.libs.json._

/** Represents the Swagger definition for financialsType.
  */
case class FinancialsType(incomes: Option[IncomesType], deductions: Option[Deductions])

object FinancialsType {
  implicit lazy val financialsTypeJsonFormat: Format[FinancialsType] = Json.format[FinancialsType]

  def fromFrontendModel[A: Api1894DeductionsBuilder](answers: A): FinancialsType = {
    val builder = implicitly[Api1894DeductionsBuilder[A]]
    FinancialsType(None, builder.build(answers).some)
  }
}
