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

package models.connector.api_1786

import models.connector.{api_1894, api_1895}
import play.api.libs.json._

/** Represents the Swagger definition for selfEmploymentDeductionsDetailTypeAllowableNegDisPos.
  * @param amount
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  * @param disallowableAmount
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  */
case class SelfEmploymentDeductionsDetailTypeAllowableNegDisPos(
    amount: BigDecimal,
    disallowableAmount: Option[BigDecimal]
) {
  def toApi1895: api_1895.request.SelfEmploymentDeductionsDetailAllowablePosNegType =
    models.connector.api_1895.request.SelfEmploymentDeductionsDetailAllowablePosNegType(
      Some(amount),
      disallowableAmount
    )

  def toApi1894: api_1894.request.SelfEmploymentDeductionsDetailAllowablePosNegType =
    api_1894.request.SelfEmploymentDeductionsDetailAllowablePosNegType(
      Some(amount),
      disallowableAmount
    )
}

object SelfEmploymentDeductionsDetailTypeAllowableNegDisPos {
  implicit lazy val selfEmploymentDeductionsDetailTypeAllowableNegDisPosJsonFormat: Format[SelfEmploymentDeductionsDetailTypeAllowableNegDisPos] =
    Json.format[SelfEmploymentDeductionsDetailTypeAllowableNegDisPos]
}
