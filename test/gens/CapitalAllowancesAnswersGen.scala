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

import models.database.capitalAllowances.{ElectricVehicleChargePointsDb, ZeroEmissionCarsDb}
import models.frontend.capitalAllowances.annualInvestmentAllowance.{AnnualInvestmentAllowanceAnswers, AnnualInvestmentAllowanceDb}
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.{
  ElectricVehicleChargePointsAnswers,
  EvcpHowMuchDoYouWantToClaim,
  EvcpOnlyForSelfEmployment,
  EvcpUseOutsideSE
}
import models.frontend.capitalAllowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation, SpecialTaxSitesAnswers}
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars._
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE, ZeroEmissionGoodsVehicleAnswers}
import models.frontend.capitalAllowances.{CapitalAllowances, CapitalAllowancesTailoringAnswers}
import org.scalacheck.Gen

import java.time.LocalDate

object CapitalAllowancesAnswersGen {
  val capitalAllowancesTailoringGen: Gen[CapitalAllowances] = Gen.oneOf(CapitalAllowances.accrualAllowances)

  val capitalAllowancesTailoringAnswersGen: Gen[CapitalAllowancesTailoringAnswers] = for {
    claimCapitalAllowances  <- booleanGen
    selectCapitalAllowances <- Gen.listOfN(9, capitalAllowancesTailoringGen)
  } yield CapitalAllowancesTailoringAnswers(
    claimCapitalAllowances,
    selectCapitalAllowances
  )

  val zeroEmissionCarsAnswersGen: Gen[ZeroEmissionCarsAnswers] = for {
    zeroEmissionCars                        <- booleanGen
    zeroEmissionCarsAllowance               <- Gen.option(Gen.oneOf(ZecAllowance.values))
    zeroEmissionCarsTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zeroEmissionCarsOnlyForSelfEmployment   <- Gen.option(Gen.oneOf(ZecOnlyForSelfEmployment.values))
    zeroEmissionCarsUsedOutsideSE           <- Gen.option(Gen.oneOf(ZecUseOutsideSE.values))
    zeroEmissionCarsUsedOutsideSEPercentage <- Gen.option(intGen)
    zecHowMuchDoYouWantToClaim              <- Gen.option(Gen.oneOf(ZecHowMuchDoYouWantToClaim.values))
    zeroEmissionCarsClaimAmount             <- Gen.option(bigDecimalGen)
  } yield ZeroEmissionCarsAnswers(
    zeroEmissionCars,
    zeroEmissionCarsAllowance,
    zeroEmissionCarsTotalCostOfCar,
    zeroEmissionCarsOnlyForSelfEmployment,
    zeroEmissionCarsUsedOutsideSE,
    zeroEmissionCarsUsedOutsideSEPercentage,
    zecHowMuchDoYouWantToClaim,
    zeroEmissionCarsClaimAmount
  )

  val zeroEmissionGoodsVehicleAnswersGen: Gen[ZeroEmissionGoodsVehicleAnswers] = for {
    zeroEmissionGoodsVehicle    <- booleanGen
    zegvAllowance               <- Gen.option(booleanGen)
    zegvTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zegvOnlyForSelfEmployment   <- Gen.option(booleanGen)
    zegvUsedOutsideSE           <- Gen.option(Gen.oneOf(ZegvUseOutsideSE.values))
    zegvUsedOutsideSEPercentage <- Gen.option(intGen)
    zegvHowMuchDoYouWantToClaim <- Gen.option(Gen.oneOf(ZegvHowMuchDoYouWantToClaim.values))
    zegvClaimAmount             <- Gen.option(bigDecimalGen)
  } yield ZeroEmissionGoodsVehicleAnswers(
    zeroEmissionGoodsVehicle,
    zegvAllowance,
    zegvTotalCostOfCar,
    zegvOnlyForSelfEmployment,
    zegvUsedOutsideSE,
    zegvUsedOutsideSEPercentage,
    zegvHowMuchDoYouWantToClaim,
    zegvClaimAmount
  )

  val zeroEmissionCarsDbAnswersGen: Gen[ZeroEmissionCarsDb] = for {
    zeroEmissionCars                        <- booleanGen
    zeroEmissionCarsAllowance               <- Gen.option(Gen.oneOf(ZecAllowance.values))
    zeroEmissionCarsTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zeroEmissionCarsOnlyForSelfEmployment   <- Gen.option(Gen.oneOf(ZecOnlyForSelfEmployment.values))
    zeroEmissionCarsUsedOutsideSE           <- Gen.option(Gen.oneOf(ZecUseOutsideSE.values))
    zeroEmissionCarsUsedOutsideSEPercentage <- Gen.option(intGen)
    zecHowMuchDoYouWantToClaim              <- Gen.option(Gen.oneOf(ZecHowMuchDoYouWantToClaim.values))
  } yield ZeroEmissionCarsDb(
    zeroEmissionCars,
    zeroEmissionCarsAllowance,
    zeroEmissionCarsTotalCostOfCar,
    zeroEmissionCarsOnlyForSelfEmployment,
    zeroEmissionCarsUsedOutsideSE,
    zeroEmissionCarsUsedOutsideSEPercentage,
    zecHowMuchDoYouWantToClaim
  )

  val zeroEmissionCarsJourneyAnswersGen: Gen[ZeroEmissionCarsJourneyAnswers] = for {
    zeroEmissionCarsClaimAmount <- bigDecimalGen
  } yield ZeroEmissionCarsJourneyAnswers(zeroEmissionCarsClaimAmount)

  val electricVehicleChargePointsAnswersGen: Gen[ElectricVehicleChargePointsAnswers] = for {
    evcpAllowance               <- booleanGen
    chargePointTaxRelief        <- Gen.option(booleanGen)
    amountSpentOnEvcp           <- Gen.option(bigDecimalGen)
    evcpOnlyForSelfEmployment   <- Gen.option(Gen.oneOf(EvcpOnlyForSelfEmployment.values))
    evcpUsedOutsideSE           <- Gen.option(Gen.oneOf(EvcpUseOutsideSE.values))
    evcpUsedOutsideSEPercentage <- Gen.option(intGen)
    evcpHowMuchDoYouWantToClaim <- Gen.option(Gen.oneOf(EvcpHowMuchDoYouWantToClaim.values))
    evcpClaimAmount             <- Gen.option(bigDecimalGen)
  } yield ElectricVehicleChargePointsAnswers(
    evcpAllowance,
    chargePointTaxRelief,
    amountSpentOnEvcp,
    evcpOnlyForSelfEmployment,
    evcpUsedOutsideSE,
    evcpUsedOutsideSEPercentage,
    evcpHowMuchDoYouWantToClaim,
    evcpClaimAmount
  )

  val electricVehicleChargePointsDbAnswersGen: Gen[ElectricVehicleChargePointsDb] = for {
    evcpAllowance               <- booleanGen
    chargePointTaxRelief        <- Gen.option(booleanGen)
    amountSpentOnEvcp           <- Gen.option(bigDecimalGen)
    evcpOnlyForSelfEmployment   <- Gen.option(Gen.oneOf(EvcpOnlyForSelfEmployment.values))
    evcpUsedOutsideSE           <- Gen.option(Gen.oneOf(EvcpUseOutsideSE.values))
    evcpUsedOutsideSEPercentage <- Gen.option(intGen)
    evcpHowMuchDoYouWantToClaim <- Gen.option(Gen.oneOf(EvcpHowMuchDoYouWantToClaim.values))
  } yield ElectricVehicleChargePointsDb(
    evcpAllowance,
    chargePointTaxRelief,
    amountSpentOnEvcp,
    evcpOnlyForSelfEmployment,
    evcpUsedOutsideSE,
    evcpUsedOutsideSEPercentage,
    evcpHowMuchDoYouWantToClaim
  )

  val balancingAllowanceAnswersGen: Gen[BalancingAllowanceAnswers] = for {
    balancingAllowance       <- booleanGen
    balancingAllowanceAmount <- Gen.option(bigDecimalGen)
  } yield BalancingAllowanceAnswers(
    balancingAllowance,
    balancingAllowanceAmount
  )

  val annualInvestmentAllowanceDbAnswersGen: Gen[AnnualInvestmentAllowanceDb] = for {
    annualInvestmentAllowance <- booleanGen
  } yield AnnualInvestmentAllowanceDb(annualInvestmentAllowance)

  val annualInvestmentAllowanceAnswersGen: Gen[AnnualInvestmentAllowanceAnswers] = for {
    annualInvestmentAllowance       <- booleanGen
    annualInvestmentAllowanceAmount <- Gen.option(bigDecimalGen)
  } yield AnnualInvestmentAllowanceAnswers(
    annualInvestmentAllowance,
    annualInvestmentAllowanceAmount
  )

  val writingDownAllowanceGen: Gen[WritingDownAllowanceAnswers] = for {
    wdaSpecialRateClaimAmount  <- Gen.option(bigDecimalGen)
    wdaMainRateClaimAmount     <- Gen.option(bigDecimalGen)
    wdaSingleAssetClaimAmounts <- Gen.option(bigDecimalGen)
  } yield WritingDownAllowanceAnswers(
    Some(true),
    wdaSpecialRateClaimAmount,
    Some(true),
    wdaMainRateClaimAmount,
    Some(true),
    wdaSingleAssetClaimAmounts
  )

  val specialTaxSitesGen: Gen[SpecialTaxSitesAnswers] = for {
    claimingAmount             <- Gen.option(bigDecimalGen)
    existingSiteClaimingAmount <- bigDecimalGen
  } yield {
    val newSpecialTaxSites = List(
      NewSpecialTaxSite(
        contractForBuildingConstruction = Some(true),
        contractStartDate = Some(LocalDate.now()),
        constructionStartDate = Some(LocalDate.now()),
        qualifyingUseStartDate = Some(LocalDate.now()),
        specialTaxSiteLocation = Some(SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA")),
        newSiteClaimingAmount = claimingAmount
      )
    )

    SpecialTaxSitesAnswers(
      specialTaxSites = true,
      newSpecialTaxSites = newSpecialTaxSites,
      doYouHaveAContinuingClaim = true,
      continueClaimingAllowanceForExistingSite = true,
      existingSiteClaimingAmount = existingSiteClaimingAmount
    )
  }

}
