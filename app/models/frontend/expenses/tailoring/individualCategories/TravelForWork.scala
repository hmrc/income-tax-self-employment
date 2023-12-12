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

package models.frontend.expenses.tailoring.individualCategories

import models.common.{Enumerable, WithName}

sealed trait TravelForWork

object TravelForWork extends Enumerable.Implicits {

  case object YesAllowable    extends WithName("yesAllowable") with TravelForWork
  case object YesDisallowable extends WithName("yesDisallowable") with TravelForWork
  case object No              extends WithName("no") with TravelForWork

  val values: Seq[TravelForWork] = Seq(
    YesAllowable,
    YesDisallowable,
    No
  )

  implicit val enumerable: Enumerable[TravelForWork] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
