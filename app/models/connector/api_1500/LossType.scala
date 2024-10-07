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

package models.connector.api_1500

import models.common.{Enumerable, WithName}

sealed trait LossType
object LossType extends Enumerable.Implicits {

  case object SelfEmployment       extends WithName("self-employment") with LossType
  case object SelfEmploymentClass4 extends WithName("self-employment-class4") with LossType

  val values: Seq[LossType] = Seq(
    SelfEmployment,
    SelfEmploymentClass4
  )

  implicit val enumerable: Enumerable[LossType] = Enumerable(values.map(v => v.toString -> v): _*)
}
