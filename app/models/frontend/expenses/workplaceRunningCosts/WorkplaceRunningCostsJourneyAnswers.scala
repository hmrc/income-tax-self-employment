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

package models.frontend.expenses.workplaceRunningCosts

import models.connector.api_1786
import models.database.expenses.WorkplaceRunningCostsDb
import play.api.libs.json.{Json, OFormat}

case class WorkplaceRunningCostsJourneyAnswers(wfhPremisesRunningCosts: BigDecimal, wfbpPremisesRunningCostsDisallowable: Option[BigDecimal])

object WorkplaceRunningCostsJourneyAnswers {
  implicit val formats: OFormat[WorkplaceRunningCostsJourneyAnswers] = Json.format[WorkplaceRunningCostsJourneyAnswers]
}

case class WorkplaceRunningCostsAnswers(moreThan25Hours: Option[MoreThan25Hours],
                                        wfhHours25To50: Option[Int],
                                        wfhHours51To100: Option[Int],
                                        wfhHours101Plus: Option[Int],
                                        wfhFlatRateOrActualCosts: Option[WfhFlatRateOrActualCosts],
                                        wfhClaimingAmount: Option[BigDecimal],
                                        liveAtBusinessPremises: Option[LiveAtBusinessPremises],
                                        businessPremisesAmount: Option[BigDecimal],
                                        businessPremisesDisallowableAmount: Option[BigDecimal],
                                        livingAtBusinessPremisesOnePerson: Option[Int],
                                        livingAtBusinessPremisesTwoPeople: Option[Int],
                                        livingAtBusinessPremisesThreePlusPeople: Option[Int],
                                        wfbpFlatRateOrActualCosts: Option[WfbpFlatRateOrActualCosts],
                                        wfbpClaimingAmount: Option[BigDecimal]) {

  def toApiSubmissionModel: WorkplaceRunningCostsJourneyAnswers = {
    val wfhPremisesRunningCosts: BigDecimal = Seq(wfhClaimingAmount, businessPremisesAmount, wfbpClaimingAmount).flatten.sum
    WorkplaceRunningCostsJourneyAnswers(
      wfhPremisesRunningCosts = wfhPremisesRunningCosts,
      wfbpPremisesRunningCostsDisallowable = businessPremisesDisallowableAmount
    )
  }

  def toDbModel: WorkplaceRunningCostsDb =
    WorkplaceRunningCostsDb(
      moreThan25Hours,
      wfhHours25To50,
      wfhHours51To100,
      wfhHours101Plus,
      wfhFlatRateOrActualCosts,
      wfhClaimingAmount,
      liveAtBusinessPremises,
      businessPremisesAmount,
      businessPremisesDisallowableAmount.isDefined,
      livingAtBusinessPremisesOnePerson,
      livingAtBusinessPremisesTwoPeople,
      livingAtBusinessPremisesThreePlusPeople,
      wfbpFlatRateOrActualCosts,
      wfbpClaimingAmount
    )
}

object WorkplaceRunningCostsAnswers {
  implicit val formats: OFormat[WorkplaceRunningCostsAnswers] = Json.format[WorkplaceRunningCostsAnswers]

  def apply(dbAnswers: WorkplaceRunningCostsDb, periodicSummaryDetails: api_1786.SuccessResponseSchema): WorkplaceRunningCostsAnswers =
    new WorkplaceRunningCostsAnswers(
      moreThan25Hours = dbAnswers.moreThan25Hours,
      wfhHours25To50 = dbAnswers.wfhHours25To50,
      wfhHours51To100 = dbAnswers.wfhHours51To100,
      wfhHours101Plus = dbAnswers.wfhHours101Plus,
      wfhFlatRateOrActualCosts = dbAnswers.wfhFlatRateOrActualCosts,
      wfhClaimingAmount = dbAnswers.wfhClaimingAmount,
      liveAtBusinessPremises = dbAnswers.liveAtBusinessPremises,
      businessPremisesAmount = dbAnswers.businessPremisesAmount,
      businessPremisesDisallowableAmount = if (dbAnswers.wfbpExpensesAreDisallowable) {
        periodicSummaryDetails.financials.deductions.flatMap(_.premisesRunningCosts.flatMap(_.disallowableAmount))
      } else {
        None
      },
      livingAtBusinessPremisesOnePerson = dbAnswers.livingAtBusinessPremisesOnePerson,
      livingAtBusinessPremisesTwoPeople = dbAnswers.livingAtBusinessPremisesTwoPeople,
      livingAtBusinessPremisesThreePlusPeople = dbAnswers.livingAtBusinessPremisesThreePlusPeople,
      wfbpFlatRateOrActualCosts = dbAnswers.wfbpFlatRateOrActualCosts,
      wfbpClaimingAmount = dbAnswers.wfbpClaimingAmount
    )
}
