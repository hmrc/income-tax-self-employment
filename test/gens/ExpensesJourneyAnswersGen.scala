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

import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.workplaceRunningCosts._
import org.scalacheck.Gen

object ExpensesJourneyAnswersGen {

  val goodsToSellOrUseJourneyAnswersGen: Gen[GoodsToSellOrUseJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield GoodsToSellOrUseJourneyAnswers(amount, disallowableAmount)

  val goodsToSellOrUseAnswersGen: Gen[GoodsToSellOrUseAnswers] = for {
    taxiMinicabOrRoadHaulage <- booleanGen
    amount                   <- bigDecimalGen
    disallowableAmount       <- Gen.option(bigDecimalGen)
  } yield GoodsToSellOrUseAnswers(taxiMinicabOrRoadHaulage, amount, disallowableAmount)

  val workplaceRunningCostsJourneyAnswersGen: Gen[WorkplaceRunningCostsJourneyAnswers] = for {
    wfhPremisesRunningCosts              <- bigDecimalGen
    wfbpPremisesRunningCostsDisallowable <- Gen.option(bigDecimalGen)
  } yield WorkplaceRunningCostsJourneyAnswers(wfhPremisesRunningCosts, wfbpPremisesRunningCostsDisallowable)

  val workplaceRunningCostsAnswersGen: Gen[WorkplaceRunningCostsAnswers] = for {
    moreThan25Hours                         <- Gen.option(booleanGen)
    wfhHours25To50                          <- Gen.option(intGen)
    wfhHours51To100                         <- Gen.option(intGen)
    wfhHours101Plus                         <- Gen.option(intGen)
    wfhFlatRateOrActualCosts                <- Gen.option(Gen.oneOf(WfhFlatRateOrActualCosts.values))
    wfhClaimingAmount                       <- Gen.option(bigDecimalGen)
    liveAtBusinessPremises                  <- Gen.option(booleanGen)
    businessPremisesAmount                  <- Gen.option(bigDecimalGen)
    businessPremisesDisallowableAmount      <- Gen.option(bigDecimalGen)
    livingAtBusinessPremisesOnePerson       <- Gen.option(intGen)
    livingAtBusinessPremisesTwoPeople       <- Gen.option(intGen)
    livingAtBusinessPremisesThreePlusPeople <- Gen.option(intGen)
    wfbpFlatRateOrActualCosts               <- Gen.option(Gen.oneOf(WfbpFlatRateOrActualCosts.values))
    wfbpClaimingAmount                      <- Gen.option(bigDecimalGen)
  } yield WorkplaceRunningCostsAnswers(
    moreThan25Hours,
    wfhHours25To50,
    wfhHours51To100,
    wfhHours101Plus,
    wfhFlatRateOrActualCosts,
    wfhClaimingAmount,
    liveAtBusinessPremises,
    businessPremisesAmount,
    businessPremisesDisallowableAmount,
    livingAtBusinessPremisesOnePerson,
    livingAtBusinessPremisesTwoPeople,
    livingAtBusinessPremisesThreePlusPeople,
    wfbpFlatRateOrActualCosts,
    wfbpClaimingAmount
  )

  val officeSuppliesJourneyAnswersGen: Gen[OfficeSuppliesJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield OfficeSuppliesJourneyAnswers(amount, disallowableAmount)

  val entertainmentJourneyAnswersGen: Gen[EntertainmentJourneyAnswers] = bigDecimalGen.map(EntertainmentJourneyAnswers(_))

  val repairsAndMaintenanceCostsJourneyAnswersGen: Gen[RepairsAndMaintenanceCostsJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield RepairsAndMaintenanceCostsJourneyAnswers(amount, disallowableAmount)

  val staffCostsJourneyAnswersGen: Gen[StaffCostsJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield StaffCostsJourneyAnswers(amount, disallowableAmount)

  val advertisingOrMarketingJourneyAnswersGen: Gen[AdvertisingOrMarketingJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield AdvertisingOrMarketingJourneyAnswers(amount, disallowableAmount)

  val entertainmentCostsJourneyAnswersGen: Gen[EntertainmentJourneyAnswers] = for {
    disallowableAmount <- bigDecimalGen
  } yield EntertainmentJourneyAnswers(disallowableAmount)

  val constructionJourneyAnswersGen: Gen[ConstructionJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield ConstructionJourneyAnswers(amount, disallowableAmount)

  val professionalFeesJourneyAnswersGen: Gen[ProfessionalFeesJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield ProfessionalFeesJourneyAnswers(amount, disallowableAmount)

  val depreciationCostsJourneyAnswersGen: Gen[DepreciationCostsJourneyAnswers] = bigDecimalGen.map(DepreciationCostsJourneyAnswers(_))

  val interestJourneyAnswersGen: Gen[InterestJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield InterestJourneyAnswers(amount, disallowableAmount)

  val depreciationJourneyAnswersGen: Gen[DepreciationCostsJourneyAnswers] = for {
    disallowableAmount <- bigDecimalGen
  } yield DepreciationCostsJourneyAnswers(disallowableAmount)

  val otherExpensesJourneyAnswersGen: Gen[OtherExpensesJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield OtherExpensesJourneyAnswers(amount, disallowableAmount)

  val financialChargesJourneyAnswersGen: Gen[FinancialChargesJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield FinancialChargesJourneyAnswers(amount, disallowableAmount)

  val irrecoverableDebtsJourneyAnswersGen: Gen[IrrecoverableDebtsJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield IrrecoverableDebtsJourneyAnswers(amount, disallowableAmount)

}
