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

package models.connector.api_1895.request

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

case class AmendSEPeriodSummaryRequestBody(incomes: Option[Incomes], deductions: Option[Deductions])

object AmendSEPeriodSummaryRequestBody {
  implicit val formats: OFormat[AmendSEPeriodSummaryRequestBody] = Json.format[AmendSEPeriodSummaryRequestBody]

  private def getOrEmptyDeductions(existingFinancial: AmendSEPeriodSummaryRequestBody): Deductions =
    existingFinancial.deductions.getOrElse(Deductions.empty)

  def updateOfficeSupplies(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                           answers: OfficeSuppliesJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          adminCosts = Some(
            SelfEmploymentDeductionsDetailType(answers.officeSuppliesAmount, answers.officeSuppliesDisallowableAmount)
          )
        )
      )
    )
  }

  def updateGoodsToSell(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                        answers: GoodsToSellOrUseJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          costOfGoods = Some(
            SelfEmploymentDeductionsDetailPosNegType(Some(answers.goodsToSellOrUseAmount), answers.disallowableGoodsToSellOrUseAmount)
          )
        )
      )
    )
  }

  def updateRepairsAndMaintenance(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                                  answers: RepairsAndMaintenanceCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          maintenanceCosts = Some(
            SelfEmploymentDeductionsDetailPosNegType(Some(answers.repairsAndMaintenanceAmount), answers.repairsAndMaintenanceDisallowableAmount)
          )
        )
      )
    )
  }
  def updateWorkplaceRunningCosts(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                                  answers: WorkplaceRunningCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          premisesRunningCosts = Some(
            SelfEmploymentDeductionsDetailPosNegType(Some(answers.wfhPremisesRunningCosts), answers.wfbpPremisesRunningCostsDisallowable)
          )
        )
      )
    )
  }
  def updateAdvertisingOrMarketing(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                                   answers: AdvertisingOrMarketingJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          advertisingCosts = Some(
            SelfEmploymentDeductionsDetailType(answers.advertisingOrMarketingAmount, answers.advertisingOrMarketingDisallowableAmount)
          )
        )
      )
    )
  }
  def updateEntertainmentCosts(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                               answers: EntertainmentJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          businessEntertainmentCosts = Some(
            SelfEmploymentDeductionsDetailType(answers.entertainmentAmount, None) // TODO LT Is this correct, I swaped None with amount
          )
        )
      )
    )
  }
  def updateStaffCosts(existingPeriodSummary: AmendSEPeriodSummaryRequestBody, answers: StaffCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          staffCosts = Some(
            SelfEmploymentDeductionsDetailType(answers.staffCostsAmount, answers.staffCostsDisallowableAmount)
          )
        )
      )
    )
  }

  def updateConstructionIndustrySubcontractors(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                                               answers: ConstructionJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          constructionIndustryScheme = Some(
            SelfEmploymentDeductionsDetailType(answers.constructionIndustryAmount, answers.constructionIndustryDisallowableAmount)
          )
        )
      )
    )
  }
  def updateProfessionalFees(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                             answers: ProfessionalFeesJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          professionalFees = Some(
            SelfEmploymentDeductionsDetailAllowablePosNegType(Some(answers.professionalFeesAmount), answers.professionalFeesDisallowableAmount)
          )
        )
      )
    )
  }
  def updateFinancialCharges(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                             answers: FinancialChargesJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          financialCharges =
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.financialChargesAmount), answers.financialChargesDisallowableAmount))
        )
      )
    )
  }

  def updateBadDebts(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                     answers: IrrecoverableDebtsJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          badDebt =
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.irrecoverableDebtsAmount), answers.irrecoverableDebtsDisallowableAmount))
        )
      )
    )
  }

  def updateDepreciationCosts(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                              answers: DepreciationCostsJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          depreciation = Some(
            SelfEmploymentDeductionsDetailPosNegType(None, Some(answers.depreciationDisallowableAmount))
          )
        )
      )
    )
  }

  def updateOtherExpenses(existingPeriodSummary: AmendSEPeriodSummaryRequestBody,
                          answers: OtherExpensesJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          other = Some(SelfEmploymentDeductionsDetailType(answers.otherExpensesAmount, answers.otherExpensesDisallowableAmount))
        )
      )
    )
  }

  def updateInterest(existingPeriodSummary: AmendSEPeriodSummaryRequestBody, answers: InterestJourneyAnswers): AmendSEPeriodSummaryRequestBody = {
    val deductions = getOrEmptyDeductions(existingPeriodSummary)
    existingPeriodSummary.copy(
      deductions = Some(
        deductions.copy(
          interest = Some(
            SelfEmploymentDeductionsDetailPosNegType(Some(answers.interestAmount), answers.interestDisallowableAmount)
          )
        )
      )
    )
  }
}
