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

package models.database.expenses.travel

import play.api.libs.json.{Format, JsError, JsString, JsSuccess}

sealed trait ExpenseMethod
case object FlatRate    extends ExpenseMethod
case object ActualCosts extends ExpenseMethod

object ExpenseMethod {

  private val flatRate: String    = "FlatRate"
  private val actualCosts: String = "ActualCosts"

  implicit val format: Format[ExpenseMethod] = Format(
    {
      case JsString(`flatRate`)    => JsSuccess(FlatRate)
      case JsString(`actualCosts`) => JsSuccess(ActualCosts)
      case _                       => JsError("Invalid ExpenseMethod")
    },
    {
      case FlatRate    => JsString(`flatRate`)
      case ActualCosts => JsString(`actualCosts`)
    }
  )

}
