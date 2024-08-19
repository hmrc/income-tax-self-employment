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
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues._

class AmendSEPeriodSummaryRequestBodySpec extends AnyWordSpecLike with TypeCheckedTripleEquals {
  private val data = AmendSEPeriodSummaryRequestBodyTestData.sample

  "update deductions" should {
    "updating an expense should create a new Deduction if it does not exist before" in {
      val data = AmendSEPeriodSummaryRequestBody(None, None)
      val answers = OfficeSuppliesJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateOfficeSupplies(answers)
      assert(updated.deductions.value.adminCosts.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateOfficeSupplies" in {
      val answers = OfficeSuppliesJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateOfficeSupplies(answers)
      assert(updated.deductions.value.adminCosts.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateGoodsToSell" in {
      val answers = GoodsToSellOrUseJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateGoodsToSell(answers)
      assert(updated.deductions.value.costOfGoods.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }

    "updateRepairsAndMaintenance" in {
      val answers = RepairsAndMaintenanceCostsJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateRepairsAndMaintenance(answers)
      assert(updated.deductions.value.maintenanceCosts.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }

    "updateWorkplaceRunningCosts" in {
      val answers = WorkplaceRunningCostsJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateWorkplaceRunningCosts(answers)
      assert(updated.deductions.value.premisesRunningCosts.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }

    "updateAdvertisingOrMarketing" in {
      val answers = AdvertisingOrMarketingJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateAdvertisingOrMarketing(answers)
      assert(updated.deductions.value.advertisingCosts.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateEntertainmentCosts" in {
      val answers = EntertainmentJourneyAnswers(100.0)
      val updated = data.updateEntertainmentCosts(answers)
      assert(updated.deductions.value.businessEntertainmentCosts.value === SelfEmploymentDeductionsDetailType(100.0, None))
    }

    "updateStaffCosts" in {
      val answers = StaffCostsJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateStaffCosts(answers)
      assert(updated.deductions.value.staffCosts.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateConstructionIndustrySubcontractors" in {
      val answers = ConstructionJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateConstructionIndustrySubcontractors(answers)
      assert(updated.deductions.value.constructionIndustryScheme.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateProfessionalFees" in {
      val answers = ProfessionalFeesJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateProfessionalFees(answers)
      assert(updated.deductions.value.professionalFees.value === SelfEmploymentDeductionsDetailAllowablePosNegType(Some(100.0), Some(200.0)))
    }

    "updateFinancialCharges" in {
      val answers = FinancialChargesJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateFinancialCharges(answers)
      assert(updated.deductions.value.financialCharges.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }

    "updateBadDebts" in {
      val answers = IrrecoverableDebtsJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateBadDebts(answers)
      assert(updated.deductions.value.badDebt.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }

    "updateDepreciationCosts" in {
      val answers = DepreciationCostsJourneyAnswers(100.0)
      val updated = data.updateDepreciationCosts(answers)
      assert(updated.deductions.value.depreciation.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), None))
    }

    "updateOtherExpenses" in {
      val answers = OtherExpensesJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateOtherExpenses(answers)
      assert(updated.deductions.value.other.value === SelfEmploymentDeductionsDetailType(100.0, Some(200.0)))
    }

    "updateInterest" in {
      val answers = InterestJourneyAnswers(100.0, Some(200.0))
      val updated = data.updateInterest(answers)
      assert(updated.deductions.value.interest.value === SelfEmploymentDeductionsDetailPosNegType(Some(100.0), Some(200.0)))
    }
  }
}
