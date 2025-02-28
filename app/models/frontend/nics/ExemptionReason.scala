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

import enumeratum.{Enum, EnumEntry}
import models.connector.api_1803.AnnualNonFinancialsType

sealed abstract class ExemptionReason(override val entryName: String) extends EnumEntry {
  val exemptionCode: String
}

object ExemptionReason extends Enum[ExemptionReason] with utils.PlayJsonEnum[ExemptionReason] {

  val values: IndexedSeq[ExemptionReason] = findValues

  case object TrusteeExecutorAdmin extends ExemptionReason("trusteeExecutorAdmin") {
    override val exemptionCode: String = "002"
  }
  case object DiverDivingInstructor extends ExemptionReason("diverDivingInstructor") {
    override val exemptionCode: String = "003"
  }

  def fromNonFinancialType(exemption: AnnualNonFinancialsType.Class4NicsExemptionReason.Value): Option[ExemptionReason] =
    exemption match {
      case AnnualNonFinancialsType.Class4NicsExemptionReason._003 =>
        Some(ExemptionReason.DiverDivingInstructor)
      case AnnualNonFinancialsType.Class4NicsExemptionReason._002 =>
        Some(ExemptionReason.TrusteeExecutorAdmin)
      case _ =>
        None
    }

}
