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

package models.frontend.expenses

import models.common.{Enumerable, WithName}

sealed trait DisallowableIrrecoverableDebts

object DisallowableIrrecoverableDebts extends Enumerable.Implicits {

  case object Yes extends WithName("yes") with DisallowableIrrecoverableDebts
  case object No  extends WithName("no") with DisallowableIrrecoverableDebts

  val values: Seq[DisallowableIrrecoverableDebts] = Seq(
    Yes,
    No
  )


  implicit val enumerable: Enumerable[DisallowableIrrecoverableDebts] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
