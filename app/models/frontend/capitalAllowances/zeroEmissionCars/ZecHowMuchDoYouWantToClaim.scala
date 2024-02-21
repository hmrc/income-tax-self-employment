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

package models.frontend.capitalAllowances.zeroEmissionCars

import models.common.{Enumerable, WithName}

sealed trait ZecHowMuchDoYouWantToClaim
object ZecHowMuchDoYouWantToClaim extends Enumerable.Implicits {

  case object FullCost    extends WithName("fullCost") with ZecHowMuchDoYouWantToClaim
  case object LowerAmount extends WithName("lowerAmount") with ZecHowMuchDoYouWantToClaim

  val values: Seq[ZecHowMuchDoYouWantToClaim] = Seq(
    FullCost,
    LowerAmount
  )

  implicit val enumerable: Enumerable[ZecHowMuchDoYouWantToClaim] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
