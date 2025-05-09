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

package models.commonTaskList

import models.common.{Enumerable, WithName}
import play.api.libs.json.{Json, OWrites, Reads}

trait SectionTitle extends Enumerable.Implicits

object SectionTitle extends SectionTitle {

  case class SelfEmploymentTitle() extends WithName("SelfEmployment") with SectionTitle

  object SelfEmploymentTitle {
    implicit val nonStrictReads: Reads[SelfEmploymentTitle] = Reads.pure(SelfEmploymentTitle())
    implicit val writes: OWrites[SelfEmploymentTitle]       = OWrites[SelfEmploymentTitle](_ => Json.obj())
  }

  case class ExpensesTitle() extends WithName("Expenses") with SectionTitle

  object ExpensesTitle {
    implicit val nonStrictReads: Reads[ExpensesTitle] = Reads.pure(ExpensesTitle())
    implicit val writes: OWrites[ExpensesTitle]       = OWrites[ExpensesTitle](_ => Json.obj())
  }

  case class CapitalAllowancesTitle() extends WithName("CapitalAllowances") with SectionTitle

  object CapitalAllowancesTitle {
    implicit val nonStrictReads: Reads[CapitalAllowancesTitle] = Reads.pure(CapitalAllowancesTitle())
    implicit val writes: OWrites[CapitalAllowancesTitle]       = OWrites[CapitalAllowancesTitle](_ => Json.obj())
  }

  case class AdjustmentsTitle() extends WithName("Adjustments") with SectionTitle

  val values: Seq[SectionTitle] = Seq(
    SelfEmploymentTitle(),
    ExpensesTitle(),
    CapitalAllowancesTitle(),
    AdjustmentsTitle()
  )

  implicit val enumerable: Enumerable[SectionTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
