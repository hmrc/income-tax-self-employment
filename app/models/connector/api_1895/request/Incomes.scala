/*
 * Copyright 2024 HM Revenue & Customs
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

package models.connector.api_1895.request

import play.api.libs.json.{Format, Json}

/** Represents the Swagger definition for incomesType.
  * @param turnover
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  * @param other
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  * @param taxTakenOffTradingIncome
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  */

case class Incomes(turnover: Option[BigDecimal], other: Option[BigDecimal], taxTakenOffTradingIncome: Option[BigDecimal])

object Incomes {
  implicit lazy val incomesTypeJsonFormat: Format[Incomes] = Json.format[Incomes]
}
