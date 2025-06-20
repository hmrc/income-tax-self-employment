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

package models.common

/** @param businessId
  *   also called incomeSourceId
  */
case class JourneyContextWithNino(taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, nino: Nino) {
  def toJourneyContext(journeyName: JourneyName): JourneyContext = JourneyContext(taxYear, businessId, mtditid, journeyName)
  def apply(newId: BusinessId): JourneyContextWithNino           = this.copy(businessId = newId)
}

/** @param businessId
  *   also called incomeSourceId
  */
case class JourneyContext(taxYear: TaxYear, _businessId: BusinessId, mtditid: Mtditid, journey: JourneyName) {
  val businessId: BusinessId = journey match {
    case _ => _businessId
  }
}
