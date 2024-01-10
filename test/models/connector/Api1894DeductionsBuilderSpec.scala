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

package models.connector

import cats.implicits._
import gens.ExpensesJourneyAnswersGen._
import gens.{bigDecimalGen, genOne}
import models.connector.api_1894.request.{
  Deductions,
  SelfEmploymentDeductionsDetailAllowablePosNegType,
  SelfEmploymentDeductionsDetailPosNegType,
  SelfEmploymentDeductionsDetailType
}
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
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.AsOneTotalAnswers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

class Api1894DeductionsBuilderSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {
  type PNT = SelfEmploymentDeductionsDetailPosNegType
  type DT  = SelfEmploymentDeductionsDetailType

  "build SelfEmploymentDeductionsDetailPosNegType" should {
    val deductions = Deductions.empty
    case class TestCase[A: Api1894DeductionsBuilder](answers: A, expected: Deductions) {
      def actualDeductions: Deductions = implicitly[Api1894DeductionsBuilder[A]].build(answers)
    }

    val bigDecimal                               = genOne(bigDecimalGen)
    val goodsToSellOrUseJourneyAnswers           = genOne(goodsToSellOrUseJourneyAnswersGen)
    val repairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
    val advertisingOrMarketingJourneyAnswers     = genOne(advertisingOrMarketingJourneyAnswersGen)
    val officeSuppliesJourneyAnswers             = genOne(officeSuppliesJourneyAnswersGen)
    val entertainmentJourneyAnswers              = genOne(entertainmentJourneyAnswersGen)
    val staffCostsJourneyAnswers                 = genOne(staffCostsJourneyAnswersGen)
    val constructionJourneyAnswers               = genOne(constructionJourneyAnswersGen)
    val professionalFeesJourneyAnswers           = genOne(professionalFeesJourneyAnswersGen)
    val interestJourneyAnswers                   = genOne(interestJourneyAnswersGen)
    val depreciationCostsJourneyAnswers          = genOne(depreciationCostsJourneyAnswersGen)
    val otherExpensesJourneyAnswers              = genOne(otherExpensesJourneyAnswersGen)
    val financialChargesJourneyAnswers           = genOne(financialChargesJourneyAnswersGen)
    val irrecoverableDebtsJourneyAnswers         = genOne(irrecoverableDebtsJourneyAnswersGen)

    val cases = Table(
      "testCase",
      TestCase[AsOneTotalAnswers](
        AsOneTotalAnswers(bigDecimal),
        deductions.copy(simplifiedExpenses = bigDecimal.some)
      ),
      TestCase[GoodsToSellOrUseJourneyAnswers](
        goodsToSellOrUseJourneyAnswers,
        deductions.copy(costOfGoods = new PNT(
          goodsToSellOrUseJourneyAnswers.goodsToSellOrUseAmount.some,
          goodsToSellOrUseJourneyAnswers.disallowableGoodsToSellOrUseAmount
        ).some)
      ),
      TestCase[RepairsAndMaintenanceCostsJourneyAnswers](
        repairsAndMaintenanceCostsJourneyAnswers,
        deductions.copy(maintenanceCosts = new PNT(
          repairsAndMaintenanceCostsJourneyAnswers.repairsAndMaintenanceAmount.some,
          repairsAndMaintenanceCostsJourneyAnswers.repairsAndMaintenanceDisallowableAmount
        ).some)
      ),
      TestCase[AdvertisingOrMarketingJourneyAnswers](
        advertisingOrMarketingJourneyAnswers,
        deductions.copy(advertisingCosts = new DT(
          advertisingOrMarketingJourneyAnswers.advertisingOrMarketingAmount.some,
          advertisingOrMarketingJourneyAnswers.advertisingOrMarketingDisallowableAmount
        ).some)
      ),
      TestCase[OfficeSuppliesJourneyAnswers](
        officeSuppliesJourneyAnswers,
        deductions.copy(adminCosts = new DT(
          officeSuppliesJourneyAnswers.officeSuppliesAmount.some,
          officeSuppliesJourneyAnswers.officeSuppliesDisallowableAmount
        ).some)
      ),
      TestCase[EntertainmentJourneyAnswers](
        entertainmentJourneyAnswers,
        deductions.copy(businessEntertainmentCosts = new DT(
          None,
          entertainmentJourneyAnswers.entertainmentAmount.some
        ).some)
      ),
      TestCase[StaffCostsJourneyAnswers](
        staffCostsJourneyAnswers,
        deductions.copy(staffCosts = new DT(
          staffCostsJourneyAnswers.staffCostsAmount.some,
          staffCostsJourneyAnswers.staffCostsDisallowableAmount
        ).some)
      ),
      TestCase[ConstructionJourneyAnswers](
        constructionJourneyAnswers,
        deductions.copy(constructionIndustryScheme = new DT(
          constructionJourneyAnswers.constructionIndustryAmount.some,
          constructionJourneyAnswers.constructionIndustryDisallowableAmount
        ).some)
      ),
      TestCase[ProfessionalFeesJourneyAnswers](
        professionalFeesJourneyAnswers,
        deductions.copy(professionalFees = SelfEmploymentDeductionsDetailAllowablePosNegType(
          professionalFeesJourneyAnswers.professionalFeesAmount.some,
          professionalFeesJourneyAnswers.professionalFeesDisallowableAmount
        ).some)
      ),
      TestCase[InterestJourneyAnswers](
        interestJourneyAnswers,
        deductions.copy(interest = new PNT(
          interestJourneyAnswers.interestAmount.some,
          interestJourneyAnswers.interestDisallowableAmount
        ).some)
      ),
      TestCase[DepreciationCostsJourneyAnswers](
        depreciationCostsJourneyAnswers,
        deductions.copy(depreciation = new PNT(
          None,
          depreciationCostsJourneyAnswers.depreciationDisallowableAmount.some
        ).some)
      ),
      TestCase[OtherExpensesJourneyAnswers](
        otherExpensesJourneyAnswers,
        deductions.copy(other = new DT(
          otherExpensesJourneyAnswers.otherExpensesAmount.some,
          otherExpensesJourneyAnswers.otherExpensesDisallowableAmount
        ).some)
      ),
      TestCase[FinancialChargesJourneyAnswers](
        financialChargesJourneyAnswers,
        deductions.copy(financialCharges = new PNT(
          financialChargesJourneyAnswers.financialChargesAmount.some,
          financialChargesJourneyAnswers.financialChargesDisallowableAmount
        ).some)
      ),
      TestCase[IrrecoverableDebtsJourneyAnswers](
        irrecoverableDebtsJourneyAnswers,
        deductions.copy(badDebt = new PNT(
          irrecoverableDebtsJourneyAnswers.irrecoverableDebtsAmount.some,
          irrecoverableDebtsJourneyAnswers.irrecoverableDebtsDisallowableAmount
        ).some)
      )
    )

    "convert from journey to downstream object" in {
      forAll(cases) { testCase =>
        assert(testCase.actualDeductions === testCase.expected)
      }
    }
  }
}
