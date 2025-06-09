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

package data.journeyAnswersData

import models.database.expenses.WorkplaceRunningCostsDb
import models.frontend.expenses.workplaceRunningCosts._

object WorkplaceRunningCostsTestAnswers {

  val workplaceRunningCostsAnswers: WorkplaceRunningCostsAnswers = WorkplaceRunningCostsAnswers(
    moreThan25Hours = Some(false),
    wfhHours25To50 = Some(3015),
    wfhHours51To100 = None,
    wfhHours101Plus = Some(6820),
    wfhFlatRateOrActualCosts = Some(WfhFlatRateOrActualCosts.ActualCosts),
    wfhClaimingAmount = Some(6168.04),
    liveAtBusinessPremises = Some(true),
    businessPremisesAmount = Some(4976.55),
    businessPremisesDisallowableAmount = Some(10000.00),
    livingAtBusinessPremisesOnePerson = Some(1),
    livingAtBusinessPremisesTwoPeople = Some(10000),
    livingAtBusinessPremisesThreePlusPeople = None,
    wfbpFlatRateOrActualCosts = Some(WfbpFlatRateOrActualCosts.ActualCosts),
    wfbpClaimingAmount = Some(10000.00)
  )

  val workplaceRunningCostsJourneyAnswers: WorkplaceRunningCostsJourneyAnswers = WorkplaceRunningCostsJourneyAnswers(
    wfhPremisesRunningCosts = 21144.59,
    wfbpPremisesRunningCostsDisallowable = Some(10000.00)
  )

  val workplaceRunningCostsDb: WorkplaceRunningCostsDb = WorkplaceRunningCostsDb(
    moreThan25Hours = Some(false),
    wfhHours25To50 = Some(3015),
    wfhHours51To100 = None,
    wfhHours101Plus = Some(6820),
    wfhFlatRateOrActualCosts = Some(WfhFlatRateOrActualCosts.ActualCosts),
    wfhClaimingAmount = Some(6168.04),
    liveAtBusinessPremises = Some(true),
    businessPremisesAmount = Some(4976.55),
    wfbpExpensesAreDisallowable = true,
    livingAtBusinessPremisesOnePerson = Some(1),
    livingAtBusinessPremisesTwoPeople = Some(10000),
    livingAtBusinessPremisesThreePlusPeople = None,
    wfbpFlatRateOrActualCosts = Some(WfbpFlatRateOrActualCosts.ActualCosts),
    wfbpClaimingAmount = Some(10000.00)
  )
}
