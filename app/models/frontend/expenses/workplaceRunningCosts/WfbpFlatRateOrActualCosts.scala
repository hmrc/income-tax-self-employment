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

package models.frontend.expenses.workplaceRunningCosts

import models.common.{Enumerable, WithName}

sealed trait WfbpFlatRateOrActualCosts

object WfbpFlatRateOrActualCosts extends Enumerable.Implicits {

  case object FlatRate    extends WithName("flatRate") with WfbpFlatRateOrActualCosts
  case object ActualCosts extends WithName("actualCosts") with WfbpFlatRateOrActualCosts

  val values: Seq[WfbpFlatRateOrActualCosts] = Seq(
    FlatRate,
    ActualCosts
  )

  implicit val enumerable: Enumerable[WfbpFlatRateOrActualCosts] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
