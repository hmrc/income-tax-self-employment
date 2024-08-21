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

package models.frontend.nics

import models.common.BusinessId
import play.api.libs.json.{Format, Json}

case class NICsClass4Answers(class4NICs: Boolean,
                             class4ExemptionReason: Option[ExemptionReason],
                             class4DivingExempt: Option[List[BusinessId]],
                             class4NonDivingExempt: Option[List[BusinessId]])

object NICsClass4Answers {
  implicit val formats: Format[NICsClass4Answers] = Json.format[NICsClass4Answers]
}
