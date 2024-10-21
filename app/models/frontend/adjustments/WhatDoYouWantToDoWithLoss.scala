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

package models.frontend.adjustments

import models.common.{Enumerable, WithName}

sealed trait WhatDoYouWantToDoWithLoss

object WhatDoYouWantToDoWithLoss extends Enumerable.Implicits {

  case object DeductFromOtherTypes extends WithName("deductFromOtherTypes") with WhatDoYouWantToDoWithLoss
  case object CarryItForward       extends WithName("carryItForward") with WhatDoYouWantToDoWithLoss

  val values: List[WhatDoYouWantToDoWithLoss] = List(
    DeductFromOtherTypes,
    CarryItForward
  )

  implicit val enumerable: Enumerable[WhatDoYouWantToDoWithLoss] = Enumerable(values.map(v => v.toString -> v): _*)

}
