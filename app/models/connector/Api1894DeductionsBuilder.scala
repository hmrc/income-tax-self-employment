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

package models.connector

import models.connector.api_1894.request._
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
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsJourneyAnswers

trait Api1894DeductionsBuilder[A] {
  def build(answers: A): Deductions
}

/** Set of converters which convert from a journey answer to a Downstream objects.
  */
object Api1894DeductionsBuilder {

  implicit val expensesTotalAmount: Api1894DeductionsBuilder[ExpensesTailoringAnswers.AsOneTotalAnswers] =
    (answers: ExpensesTailoringAnswers.AsOneTotalAnswers) =>
      Deductions.empty.copy(
        simplifiedExpenses = Some(answers.totalAmount)
      )

  implicit val goodsToSellOrUse: Api1894DeductionsBuilder[GoodsToSellOrUseJourneyAnswers] =
    (answers: GoodsToSellOrUseJourneyAnswers) =>
      Deductions.empty.copy(
        costOfGoods = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.goodsToSellOrUseAmount), answers.disallowableGoodsToSellOrUseAmount)
        )
      )

  implicit val workplaceRunningCosts: Api1894DeductionsBuilder[WorkplaceRunningCostsJourneyAnswers] =
    (answers: WorkplaceRunningCostsJourneyAnswers) =>
      Deductions.empty.copy(
        premisesRunningCosts = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.wfhPremisesRunningCosts), answers.wfbpPremisesRunningCostsDisallowable)
        )
      )

  implicit val repairsAndMaintenanceCosts: Api1894DeductionsBuilder[RepairsAndMaintenanceCostsJourneyAnswers] =
    (answers: RepairsAndMaintenanceCostsJourneyAnswers) =>
      Deductions.empty.copy(
        maintenanceCosts = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.repairsAndMaintenanceAmount), answers.repairsAndMaintenanceDisallowableAmount)
        )
      )

  implicit val advertisingOrMarketingCosts: Api1894DeductionsBuilder[AdvertisingOrMarketingJourneyAnswers] =
    (answers: AdvertisingOrMarketingJourneyAnswers) =>
      Deductions.empty.copy(
        advertisingCosts = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.advertisingOrMarketingAmount), answers.advertisingOrMarketingDisallowableAmount)
        )
      )

  implicit val officeSupplies: Api1894DeductionsBuilder[OfficeSuppliesJourneyAnswers] =
    (answers: OfficeSuppliesJourneyAnswers) =>
      Deductions.empty.copy(
        adminCosts = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.officeSuppliesAmount), answers.officeSuppliesDisallowableAmount)
        )
      )

  implicit val entertainmentCosts: Api1894DeductionsBuilder[EntertainmentJourneyAnswers] =
    (answers: EntertainmentJourneyAnswers) =>
      Deductions.empty.copy(
        businessEntertainmentCosts = Some(
          SelfEmploymentDeductionsDetailType(None, Some(answers.entertainmentAmount))
        )
      )

  implicit val staffCosts: Api1894DeductionsBuilder[StaffCostsJourneyAnswers] =
    (answers: StaffCostsJourneyAnswers) =>
      Deductions.empty.copy(
        staffCosts = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.staffCostsAmount), answers.staffCostsDisallowableAmount)
        )
      )

  implicit val construction: Api1894DeductionsBuilder[ConstructionJourneyAnswers] =
    (answers: ConstructionJourneyAnswers) =>
      Deductions.empty.copy(
        constructionIndustryScheme = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.constructionIndustryAmount), answers.constructionIndustryDisallowableAmount)
        )
      )

  implicit val professionalFees: Api1894DeductionsBuilder[ProfessionalFeesJourneyAnswers] =
    (answers: ProfessionalFeesJourneyAnswers) =>
      Deductions.empty.copy(
        professionalFees = Some(
          SelfEmploymentDeductionsDetailAllowablePosNegType(Some(answers.professionalFeesAmount), answers.professionalFeesDisallowableAmount)
        )
      )

  implicit val interest: Api1894DeductionsBuilder[InterestJourneyAnswers] =
    (answers: InterestJourneyAnswers) =>
      Deductions.empty.copy(
        interest = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.interestAmount), answers.interestDisallowableAmount)
        )
      )

  implicit val depreciationCosts: Api1894DeductionsBuilder[DepreciationCostsJourneyAnswers] =
    (answers: DepreciationCostsJourneyAnswers) =>
      Deductions.empty.copy(
        depreciation = Some(
          SelfEmploymentDeductionsDetailPosNegType(None, Some(answers.depreciationDisallowableAmount))
        )
      )

  implicit val otherExpenses: Api1894DeductionsBuilder[OtherExpensesJourneyAnswers] =
    (answers: OtherExpensesJourneyAnswers) =>
      Deductions.empty.copy(other =
        Some(SelfEmploymentDeductionsDetailType(Some(answers.otherExpensesAmount), answers.otherExpensesDisallowableAmount)))

  implicit val financialCharges: Api1894DeductionsBuilder[FinancialChargesJourneyAnswers] =
    (answers: FinancialChargesJourneyAnswers) =>
      Deductions.empty.copy(financialCharges =
        Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.financialChargesAmount), answers.financialChargesDisallowableAmount)))

  implicit val badDebt: Api1894DeductionsBuilder[IrrecoverableDebtsJourneyAnswers] =
    (answers: IrrecoverableDebtsJourneyAnswers) =>
      Deductions.empty.copy(badDebt =
        Some(SelfEmploymentDeductionsDetailPosNegType(Some(answers.irrecoverableDebtsAmount), answers.irrecoverableDebtsDisallowableAmount)))
}
