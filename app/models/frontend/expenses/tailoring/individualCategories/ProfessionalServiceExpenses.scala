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

package models.frontend.expenses.tailoring.individualCategories

import models.common.{Enumerable, WithName}

sealed trait ProfessionalServiceExpenses

object ProfessionalServiceExpenses extends Enumerable.Implicits {

  case object Staff            extends WithName("staff") with ProfessionalServiceExpenses
  case object Construction     extends WithName("construction") with ProfessionalServiceExpenses
  case object ProfessionalFees extends WithName("professionalFees") with ProfessionalServiceExpenses
  case object No               extends WithName("no") with ProfessionalServiceExpenses
  case object CheckboxDivider  extends WithName("or") with ProfessionalServiceExpenses

  val values: Seq[ProfessionalServiceExpenses] = Seq(
    Staff,
    Construction,
    ProfessionalFees,
    CheckboxDivider,
    No
  )

  implicit val enumerable: Enumerable[ProfessionalServiceExpenses] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
