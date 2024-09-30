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

import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import org.scalacheck.Gen

object ProfitOrLossAnswersGen {
  private val whichYearIsLossReportedGen: Gen[WhichYearIsLossReported] = Gen.oneOf(WhichYearIsLossReported.values)

  val profitOrLossAnswersGen: Gen[ProfitOrLossJourneyAnswers] = for {
    goodsAndServicesForYourOwnUse <- booleanGen
    goodsAndServicesAmount        <- Gen.option(bigDecimalGen)
    previousUnusedLosses          <- booleanGen
    unusedLossAmount              <- Gen.option(bigDecimalGen)
    whichYearIsLossReported       <- Gen.option(whichYearIsLossReportedGen)
  } yield ProfitOrLossJourneyAnswers(
    goodsAndServicesForYourOwnUse,
    goodsAndServicesAmount,
    previousUnusedLosses,
    unusedLossAmount,
    whichYearIsLossReported
  )
}
