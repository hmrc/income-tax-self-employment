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

import models.database.capitalAllowances._
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.balancingCharge.BalancingChargeAnswers
import models.frontend.capitalAllowances.specialTaxSites.{NewSpecialTaxSite, SpecialTaxSiteLocation, SpecialTaxSitesAnswers}
import models.frontend.capitalAllowances.structuresBuildings.{NewStructureBuilding, NewStructuresBuildingsAnswers, StructuresBuildingsLocation}
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars._
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.{ZegvHowMuchDoYouWantToClaim, ZegvUseOutsideSE, ZeroEmissionGoodsVehicleAnswers}
import models.frontend.capitalAllowances.{CapitalAllowances, CapitalAllowancesTailoringAnswers}
import org.scalacheck.Gen
import data.TimeData

import java.time.LocalDate

object CapitalAllowancesAnswersGen extends TimeData {
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
    zeroEmissionCarsAllowance               <- Gen.option(booleanGen)
    zeroEmissionCarsTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zeroEmissionCarsOnlyForSelfEmployment   <- Gen.option(booleanGen)
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

  val zeroEmissionCarsDbAnswersGen: Gen[ZeroEmissionCarsDb] = for {
    zeroEmissionCars                        <- booleanGen
    zeroEmissionCarsAllowance               <- Gen.option(booleanGen)
    zeroEmissionCarsTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zeroEmissionCarsOnlyForSelfEmployment   <- Gen.option(booleanGen)
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

  val zeroEmissionGoodsVehicleDbAnswersGen: Gen[ZeroEmissionGoodsVehicleDb] = for {
    zeroEmissionGoodsVehicle    <- booleanGen
    zegvAllowance               <- Gen.option(booleanGen)
    zegvTotalCostOfCar          <- Gen.option(bigDecimalGen)
    zegvOnlyForSelfEmployment   <- Gen.option(booleanGen)
    zegvUsedOutsideSE           <- Gen.option(Gen.oneOf(ZegvUseOutsideSE.values))
    zegvUsedOutsideSEPercentage <- Gen.option(intGen)
    zegvHowMuchDoYouWantToClaim <- Gen.option(Gen.oneOf(ZegvHowMuchDoYouWantToClaim.values))
  } yield ZeroEmissionGoodsVehicleDb(
    zeroEmissionGoodsVehicle,
    zegvAllowance,
    zegvTotalCostOfCar,
    zegvOnlyForSelfEmployment,
    zegvUsedOutsideSE,
    zegvUsedOutsideSEPercentage,
    zegvHowMuchDoYouWantToClaim
  )

  val balancingAllowanceAnswersGen: Gen[BalancingAllowanceAnswers] = for {
    balancingAllowance       <- booleanGen
    balancingAllowanceAmount <- Gen.option(bigDecimalGen)
  } yield BalancingAllowanceAnswers(
    balancingAllowance,
    balancingAllowanceAmount
  )

  val balancingChargeAnswersGen: Gen[BalancingChargeAnswers] = for {
    balancingCharge       <- booleanGen
    balancingChargeAmount <- Gen.option(bigDecimalGen)
  } yield BalancingChargeAnswers(
    balancingCharge,
    balancingChargeAmount
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
        contractStartDate = Some(testDate),
        constructionStartDate = Some(testDate),
        qualifyingUseStartDate = Some(testDate),
        qualifyingExpenditure = claimingAmount,
        specialTaxSiteLocation = Some(SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA")),
        newSiteClaimingAmount = claimingAmount
      )
    )

    SpecialTaxSitesAnswers(
      specialTaxSites = true,
      newSpecialTaxSites = Some(newSpecialTaxSites),
      doYouHaveAContinuingClaim = Some(true),
      continueClaimingAllowanceForExistingSite = Some(true),
      existingSiteClaimingAmount = Some(existingSiteClaimingAmount)
    )
  }

  val structuresBuildingsWithYeses: Gen[NewStructuresBuildingsAnswers] = for {
    newStructureBuildingClaimingAmount <- bigDecimalGen
  } yield NewStructuresBuildingsAnswers(
    structuresBuildingsAllowance = true,
    structuresBuildingsEligibleClaim = Some(true),
    structuresBuildingsPreviousClaimUse = Some(true),
    structuresBuildingsClaimed = Some(true),
    newStructuresBuildings = Some(List(
      NewStructureBuilding(
        qualifyingUse = Some(testDate),
        newStructureBuildingLocation = Some(StructuresBuildingsLocation(Some("name"), Some("number"), "AA11AA")),
        newStructureBuildingClaimingAmount = Some(newStructureBuildingClaimingAmount)
      )
    ))
  )

}
