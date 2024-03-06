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

import models.frontend.expenses.tailoring.ExpensesTailoring
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.{AsOneTotalAnswers, ExpensesTailoringIndividualCategoriesAnswers}
import models.frontend.expenses.tailoring.individualCategories._
import org.scalacheck.Gen

object ExpensesTailoringAnswersGen {
  val expensesCategoriesGen: Gen[ExpensesTailoring]                    = Gen.oneOf(ExpensesTailoring.values)
  val officeSuppliesGen: Gen[OfficeSupplies]                           = Gen.oneOf(OfficeSupplies.values)
  val goodsToSellOrUseGen: Gen[GoodsToSellOrUse]                       = Gen.oneOf(GoodsToSellOrUse.values)
  val repairsAndMaintenanceGen: Gen[RepairsAndMaintenance]             = Gen.oneOf(RepairsAndMaintenance.values)
  val workFromBusinessPremisesGen: Gen[WorkFromBusinessPremises]       = Gen.oneOf(WorkFromBusinessPremises.values)
  val travelForWorkGen: Gen[TravelForWork]                             = Gen.oneOf(TravelForWork.values)
  val advertisingOrMarketingGen: Gen[AdvertisingOrMarketing]           = Gen.oneOf(AdvertisingOrMarketing.values)
  val professionalServiceExpensesGen: Gen[ProfessionalServiceExpenses] = Gen.oneOf(ProfessionalServiceExpenses.values)
  val financialExpensesGen: Gen[FinancialExpenses]                     = Gen.oneOf(FinancialExpenses.values)
  val otherExpensesGen: Gen[OtherExpenses]                             = Gen.oneOf(OtherExpenses.values)

  val expensesTailoringIndividualCategoriesAnswersGen: Gen[ExpensesTailoringIndividualCategoriesAnswers] = for {
    officeSupplies                    <- officeSuppliesGen
    goodsToSellOrUse                  <- goodsToSellOrUseGen
    repairsAndMaintenance             <- repairsAndMaintenanceGen
    workFromHome                      <- booleanGen
    workFromBusinessPremises          <- workFromBusinessPremisesGen
    travelForWork                     <- travelForWorkGen
    advertisingOrMarketing            <- advertisingOrMarketingGen
    entertainmentCosts                <- Gen.option(booleanGen)
    professionalServiceExpenses       <- Gen.listOfN(3, professionalServiceExpensesGen)
    financialExpenses                 <- Gen.listOfN(3, financialExpensesGen)
    depreciation                      <- booleanGen
    otherExpenses                     <- otherExpensesGen
    disallowableInterest              <- Gen.option(booleanGen)
    disallowableOtherFinancialCharges <- Gen.option(booleanGen)
    disallowableIrrecoverableDebts    <- Gen.option(booleanGen)
    disallowableStaffCosts            <- Gen.option(booleanGen)
    disallowableSubcontractorCosts    <- Gen.option(booleanGen)
    disallowableProfessionalFees      <- Gen.option(booleanGen)
  } yield ExpensesTailoringIndividualCategoriesAnswers(
    officeSupplies,
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

  val expensesTailoringTotalAmountAnswersGen: Gen[AsOneTotalAnswers] = for {
    totalAmount <- bigDecimalGen
  } yield AsOneTotalAnswers(
    totalAmount
  )
}
