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

package models.frontend.expenses.tailoring

import models.common.{Enumerable, WithName}

sealed trait ExpensesCategories

object ExpensesCategories extends Enumerable.Implicits {

  case object TotalAmount          extends WithName("totalAmount") with ExpensesCategories
  case object IndividualCategories extends WithName("individualCategories") with ExpensesCategories
  case object NoExpenses           extends WithName("noExpenses") with ExpensesCategories

  val values: Seq[ExpensesCategories] = Seq(
    TotalAmount,
    IndividualCategories,
    NoExpenses
  )

  implicit val enumerable: Enumerable[ExpensesCategories] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
