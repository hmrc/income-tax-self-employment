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

package models.connector.api_1895.request

import models.database.expenses.travel.TravelExpensesDb
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsJourneyAnswers
import play.api.libs.json.{Json, OFormat}

case class AmendSEPeriodSummaryRequestBody(incomes: Option[Incomes], deductions: Option[Deductions]) {

  def returnNoneIfEmpty: Option[AmendSEPeriodSummaryRequestBody] = if (this == AmendSEPeriodSummaryRequestBody.empty) None else Some(this)

  private def getDeductions: Deductions = deductions.getOrElse(Deductions.empty)

  def updateTravelExpenses(answers: TravelExpensesDb): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions.copy(
          travelCosts = Some(SelfEmploymentDeductionsDetailType(answers.totalTravelExpenses.get, answers.disallowableTravelExpenses))
        ))
    )

  def updateOfficeSupplies(answers: OfficeSuppliesJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions.copy(
          adminCosts = Some(SelfEmploymentDeductionsDetailType(answers.officeSuppliesAmount, answers.officeSuppliesDisallowableAmount))
        ))
    )

  def updateGoodsToSell(answers: GoodsToSellOrUseJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            costOfGoods = Some(
              SelfEmploymentDeductionsDetailPosNegType(Some(answers.goodsToSellOrUseAmount), answers.disallowableGoodsToSellOrUseAmount)
            )
          ))
    )

  def updateRepairsAndMaintenance(answers: RepairsAndMaintenanceCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            maintenanceCosts = Some(
              SelfEmploymentDeductionsDetailPosNegType(Some(answers.repairsAndMaintenanceAmount), answers.repairsAndMaintenanceDisallowableAmount)
            )
          ))
    )
  def updateWorkplaceRunningCosts(answers: WorkplaceRunningCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            premisesRunningCosts = Some(
              SelfEmploymentDeductionsDetailPosNegType(Some(answers.wfhPremisesRunningCosts), answers.wfbpPremisesRunningCostsDisallowable)
            )
          ))
    )
  def updateAdvertisingOrMarketing(answers: AdvertisingOrMarketingJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            advertisingCosts = Some(
              SelfEmploymentDeductionsDetailType(answers.advertisingOrMarketingAmount, answers.advertisingOrMarketingDisallowableAmount)
            )
          ))
    )
  def updateEntertainmentCosts(answers: EntertainmentJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            businessEntertainmentCosts = Some(
              SelfEmploymentDeductionsDetailType(answers.entertainmentAmount, None)
            )
          ))
    )
  def updateStaffCosts(answers: StaffCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            staffCosts = Some(
              SelfEmploymentDeductionsDetailType(answers.staffCostsAmount, answers.staffCostsDisallowableAmount)
            )
          ))
    )

  def updateConstructionIndustrySubcontractors(answers: ConstructionJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            constructionIndustryScheme = Some(
              SelfEmploymentDeductionsDetailType(answers.constructionIndustryAmount, answers.constructionIndustryDisallowableAmount)
            )
          ))
    )
  def updateProfessionalFees(answers: ProfessionalFeesJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            professionalFees = Some(
              SelfEmploymentDeductionsDetailAllowablePosNegType(Some(answers.professionalFeesAmount), answers.professionalFeesDisallowableAmount)
            )
          ))
    )
  def updateFinancialCharges(answers: FinancialChargesJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            financialCharges =
              Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.financialChargesAmount), answers.financialChargesDisallowableAmount))
          ))
    )

  def updateBadDebts(answers: IrrecoverableDebtsJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            badDebt =
              Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.irrecoverableDebtsAmount), answers.irrecoverableDebtsDisallowableAmount))
          ))
    )

  def updateDepreciationCosts(answers: DepreciationCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            depreciation = Some(
              SelfEmploymentDeductionsDetailPosNegType(Some(answers.depreciationDisallowableAmount), None)
            )
          ))
    )

  def updateOtherExpenses(answers: OtherExpensesJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            other = Some(SelfEmploymentDeductionsDetailType(answers.otherExpensesAmount, answers.otherExpensesDisallowableAmount))
          ))
    )

  def updateInterest(answers: InterestJourneyAnswers): AmendSEPeriodSummaryRequestBody =
    copy(
      deductions = Some(
        getDeductions
          .copy(
            interest = Some(
              SelfEmploymentDeductionsDetailPosNegType(Some(answers.interestAmount), answers.interestDisallowableAmount)
            )
          ))
    )

}

object AmendSEPeriodSummaryRequestBody {
  implicit val formats: OFormat[AmendSEPeriodSummaryRequestBody] = Json.format[AmendSEPeriodSummaryRequestBody]

  val empty: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(None, None)
}
