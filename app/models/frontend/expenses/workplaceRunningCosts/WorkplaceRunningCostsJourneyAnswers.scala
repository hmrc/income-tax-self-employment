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

import models.connector.Api1786ExpensesResponseParser.noneFound
import models.database.expenses.WorkplaceRunningCostsDb
import play.api.libs.json.{Json, OFormat}

case class WorkplaceRunningCostsJourneyAnswers(wfhPremisesRunningCosts: BigDecimal, wfbpPremisesRunningCostsDisallowable: Option[BigDecimal])

object WorkplaceRunningCostsJourneyAnswers {
  implicit val formats: OFormat[WorkplaceRunningCostsJourneyAnswers] = Json.format[WorkplaceRunningCostsJourneyAnswers]
}

case class WorkplaceRunningCostsAnswers(moreThan25Hours: Boolean,
                                        monthsWfh25to50Hours: Option[BigDecimal],
                                        monthsWfh51to100Hours: Option[BigDecimal],
                                        monthsWfh101orMoreHours: Option[BigDecimal],
                                        wfhFlatRateOrActual: Boolean,
                                        wfhClaimAmount: Option[BigDecimal],
                                        liveAtBusinessPremises: LiveAtBusinessPremises,
                                        businessPremisesAmount: Option[BigDecimal],
                                        disallowableBusinessPremisesAmount: Option[BigDecimal],
                                        monthsOnePersonAtBP: Option[BigDecimal],
                                        monthsTwoPeopleAtBP: Option[BigDecimal],
                                        monthsThreeOrMorePeopleAtBP: Option[BigDecimal],
                                        businessPremisesFlatRateOrActual: Option[Boolean],
                                        personalUseAmount: Option[BigDecimal]) {

  def toApiSubmissionModel: WorkplaceRunningCostsJourneyAnswers = {
    val wfhPremisesRunningCosts: BigDecimal = (wfhClaimAmount, personalUseAmount) match {
      case (Some(wca), Some(pua)) => wca + pua
      case (Some(wca), None)      => wca
      case (None, Some(pua))      => pua
      case (None, None)           => noneFound
    }
    WorkplaceRunningCostsJourneyAnswers(
      wfhPremisesRunningCosts = wfhPremisesRunningCosts,
      wfbpPremisesRunningCostsDisallowable = disallowableBusinessPremisesAmount
    )
  }

  def toDbModel: WorkplaceRunningCostsDb =
    WorkplaceRunningCostsDb(
      moreThan25Hours,
      monthsWfh25to50Hours,
      monthsWfh51to100Hours,
      monthsWfh101orMoreHours,
      wfhFlatRateOrActual,
      liveAtBusinessPremises,
      businessPremisesAmount,
      disallowableBusinessPremisesAmount,
      monthsOnePersonAtBP,
      monthsTwoPeopleAtBP,
      monthsThreeOrMorePeopleAtBP,
      businessPremisesFlatRateOrActual
    )
}

object WorkplaceRunningCostsAnswers {
  implicit val formats: OFormat[WorkplaceRunningCostsAnswers] = Json.format[WorkplaceRunningCostsAnswers]

}
