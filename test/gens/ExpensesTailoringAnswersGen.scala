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

package gens

import models.frontend.expenses.tailoring.individualCategories._
import models.frontend.expenses.tailoring.{ExpensesCategories, ExpensesTailoringIndividualCategoriesAnswers, ExpensesTailoringNoExpensesAnswers}
import org.scalacheck.Gen

object ExpensesTailoringAnswersGen {
  val expensesCategoriesGen: Gen[ExpensesCategories]                               = Gen.oneOf(ExpensesCategories.values)
  val officeSuppliesGen: Gen[OfficeSupplies]                                       = Gen.oneOf(OfficeSupplies.values)
  val taxiMinicabOrRoadHaulageGen: Gen[TaxiMinicabOrRoadHaulage]                   = Gen.oneOf(TaxiMinicabOrRoadHaulage.values)
  val goodsToSellOrUseGen: Gen[GoodsToSellOrUse]                                   = Gen.oneOf(GoodsToSellOrUse.values)
  val repairsAndMaintenanceGen: Gen[RepairsAndMaintenance]                         = Gen.oneOf(RepairsAndMaintenance.values)
  val workFromHomeGen: Gen[WorkFromHome]                                           = Gen.oneOf(WorkFromHome.values)
  val workFromBusinessPremisesGen: Gen[WorkFromBusinessPremises]                   = Gen.oneOf(WorkFromBusinessPremises.values)
  val travelForWorkGen: Gen[TravelForWork]                                         = Gen.oneOf(TravelForWork.values)
  val advertisingOrMarketingGen: Gen[AdvertisingOrMarketing]                       = Gen.oneOf(AdvertisingOrMarketing.values)
  val entertainmentCostsGen: Gen[EntertainmentCosts]                               = Gen.oneOf(EntertainmentCosts.values)
  val professionalServiceExpensesGen: Gen[ProfessionalServiceExpenses]             = Gen.oneOf(ProfessionalServiceExpenses.values)
  val financialExpensesGen: Gen[FinancialExpenses]                                 = Gen.oneOf(FinancialExpenses.values)
  val depreciationGen: Gen[Depreciation]                                           = Gen.oneOf(Depreciation.values)
  val otherExpensesGen: Gen[OtherExpenses]                                         = Gen.oneOf(OtherExpenses.values)
  val disallowableInterestGen: Gen[DisallowableInterest]                           = Gen.oneOf(DisallowableInterest.values)
  val disallowableOtherFinancialChargesGen: Gen[DisallowableOtherFinancialCharges] = Gen.oneOf(DisallowableOtherFinancialCharges.values)
  val disallowableIrrecoverableDebtsGen: Gen[DisallowableIrrecoverableDebts]       = Gen.oneOf(DisallowableIrrecoverableDebts.values)
  val disallowableStaffCostsGen: Gen[DisallowableStaffCosts]                       = Gen.oneOf(DisallowableStaffCosts.values)
  val disallowableSubcontractorCostsGen: Gen[DisallowableSubcontractorCosts]       = Gen.oneOf(DisallowableSubcontractorCosts.values)
  val disallowableProfessionalFeesGen: Gen[DisallowableProfessionalFees]           = Gen.oneOf(DisallowableProfessionalFees.values)

  val expensesTailoringNoExpensesAnswersGen: Gen[ExpensesTailoringNoExpensesAnswers] = for {
    expensesCategories <- expensesCategoriesGen
  } yield ExpensesTailoringNoExpensesAnswers(
    expensesCategories
  )

  val expensesTailoringIndividualCategoriesAnswersGen: Gen[ExpensesTailoringIndividualCategoriesAnswers] = for {
    expensesCategories                <- expensesCategoriesGen
    officeSupplies                    <- officeSuppliesGen
    taxiMinicabOrRoadHaulage          <- taxiMinicabOrRoadHaulageGen
    goodsToSellOrUse                  <- goodsToSellOrUseGen
    repairsAndMaintenance             <- repairsAndMaintenanceGen
    workFromHome                      <- workFromHomeGen
    workFromBusinessPremises          <- workFromBusinessPremisesGen
    travelForWork                     <- travelForWorkGen
    advertisingOrMarketing            <- advertisingOrMarketingGen
    entertainmentCosts                <- Gen.option(entertainmentCostsGen)
    professionalServiceExpenses       <- Gen.listOfN(3, professionalServiceExpensesGen)
    financialExpenses                 <- Gen.listOfN(3, financialExpensesGen)
    depreciation                      <- depreciationGen
    otherExpenses                     <- otherExpensesGen
    disallowableInterest              <- Gen.option(disallowableInterestGen)
    disallowableOtherFinancialCharges <- Gen.option(disallowableOtherFinancialChargesGen)
    disallowableIrrecoverableDebts    <- Gen.option(disallowableIrrecoverableDebtsGen)
    disallowableStaffCosts            <- Gen.option(disallowableStaffCostsGen)
    disallowableSubcontractorCosts    <- Gen.option(disallowableSubcontractorCostsGen)
    disallowableProfessionalFees      <- Gen.option(disallowableProfessionalFeesGen)
  } yield ExpensesTailoringIndividualCategoriesAnswers(
    expensesCategories,
    officeSupplies,
    taxiMinicabOrRoadHaulage,
    goodsToSellOrUse,
    repairsAndMaintenance,
    workFromHome,
    workFromBusinessPremises,
    travelForWork,
    advertisingOrMarketing,
    entertainmentCosts,
    professionalServiceExpenses,
    financialExpenses,
    depreciation,
    otherExpenses,
    disallowableInterest,
    disallowableOtherFinancialCharges,
    disallowableIrrecoverableDebts,
    disallowableStaffCosts,
    disallowableSubcontractorCosts,
    disallowableProfessionalFees
  )
}
