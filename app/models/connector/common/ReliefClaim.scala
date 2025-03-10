/*
 * Copyright 2025 HM Revenue & Customs
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

package models.connector.common

import models.connector.ReliefClaimType
import play.api.libs.json.{Format, Json}

import java.time.LocalDateTime

case class ReliefClaim(incomeSourceId: String,
                       incomeSourceType: Option[IncomeSourceType] = None,
                       reliefClaimed: ReliefClaimType,
                       taxYearClaimedFor: String,
                       claimId: String,
                       sequence: Option[Int] = None,
                       submissionDate: LocalDateTime) {

  // This field is only present for property claims
  def isSelfEmploymentClaim: Boolean = incomeSourceType.isEmpty

  def isPropertyClaim: Boolean = incomeSourceType.isDefined

}

object ReliefClaim {
  implicit val format: Format[ReliefClaim] = Json.format[ReliefClaim]
}
