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

import models.database.capitalAllowances.ZeroEmissionCarsDb
import models.frontend.capitalAllowances.zeroEmissionCars._
import models.frontend.capitalAllowances.{CapitalAllowances, CapitalAllowancesTailoringAnswers}
import org.scalacheck.Gen

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
    zeroEmissionCarsAllowance               <- Gen.option(Gen.oneOf(ZeroEmissionCarsAllowance.values))
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

  val zeroEmissionCarsDbAnswersGen: Gen[ZeroEmissionCarsDb] = for {
    zeroEmissionCars                        <- booleanGen
    zeroEmissionCarsAllowance               <- Gen.option(Gen.oneOf(ZeroEmissionCarsAllowance.values))
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
}
