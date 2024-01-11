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

package models.connector.api_1803

import play.api.libs.json._

/** Represents the Swagger definition for annualNonFinancialsType.
  * @param businessDetailsChangedRecently
  *   The details of the business recently changed
  * @param exemptFromPayingClass4Nics
  *   The individual is exempt from paying NI contributions towards Class 4
  * @param class4NicsExemptionReason
  *   Exemption Reason - 001 - Non Resident, 002 - Trustee, 003 - Diver, 004 - Employed earner taxed under ITTOIA 2005, 005 - Over state pension age,
  *   006 - Under 16
  */
case class AnnualNonFinancialsType(
    businessDetailsChangedRecently: Option[Boolean],
    exemptFromPayingClass4Nics: Option[Boolean],
    class4NicsExemptionReason: Option[AnnualNonFinancialsType.Class4NicsExemptionReason.Value]
)

object AnnualNonFinancialsType {
  implicit lazy val annualNonFinancialsTypeJsonFormat: Format[AnnualNonFinancialsType] = Json.format[AnnualNonFinancialsType]

  // noinspection TypeAnnotation
  object Class4NicsExemptionReason extends Enumeration {
    val _001 = Value("001")
    val _002 = Value("002")
    val _003 = Value("003")
    val _004 = Value("004")
    val _005 = Value("005")
    val _006 = Value("006")

    type Class4NicsExemptionReason = Value
    implicit lazy val Class4NicsExemptionReasonJsonFormat: Format[Value] = Format(Reads.enumNameReads(this), Writes.enumNameWrites[this.type])
  }
}
