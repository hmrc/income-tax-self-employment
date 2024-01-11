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

package models.connector.api_1895.request

import play.api.libs.json.{Format, Json}

/** Represents the Swagger definition for selfEmploymentDeductionsDetailPosNegType.
  * @param amount
  *   positive or negative money
  * @param disallowableAmount
  *   positive or negative money
  */

case class SelfEmploymentDeductionsDetailPosNegType(amount: Option[BigDecimal], disallowableAmount: Option[BigDecimal])

object SelfEmploymentDeductionsDetailPosNegType {

  implicit lazy val selfEmploymentDeductionsDetailPosNegTypeJsonFormat: Format[SelfEmploymentDeductionsDetailPosNegType] =
    Json.format[SelfEmploymentDeductionsDetailPosNegType]

}
