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

package models.database.expenses

import models.frontend.expenses.workplaceRunningCosts.{LiveAtBusinessPremises, MoreThan25Hours, WfbpFlatRateOrActualCosts, WfhFlatRateOrActualCosts}
import play.api.libs.json.{Json, OFormat}

final case class WorkplaceRunningCostsDb(moreThan25Hours: Option[MoreThan25Hours],
                                         wfhHours25To50: Option[Int],
                                         wfhHours51To100: Option[Int],
                                         wfhHours101Plus: Option[Int],
                                         wfhFlatRateOrActualCosts: Option[WfhFlatRateOrActualCosts],
                                         wfhClaimingAmount: Option[BigDecimal],
                                         liveAtBusinessPremises: Option[LiveAtBusinessPremises],
                                         businessPremisesAmount: Option[BigDecimal],
                                         wfbpExpensesAreDisallowable: Boolean,
                                         livingAtBusinessPremisesOnePerson: Option[Int],
                                         livingAtBusinessPremisesTwoPeople: Option[Int],
                                         livingAtBusinessPremisesThreePlusPeople: Option[Int],
                                         wfbpFlatRateOrActualCosts: Option[WfbpFlatRateOrActualCosts],
                                         wfbpClaimingAmount: Option[BigDecimal])

object WorkplaceRunningCostsDb {
  implicit val format: OFormat[WorkplaceRunningCostsDb] = Json.format[WorkplaceRunningCostsDb]
}
