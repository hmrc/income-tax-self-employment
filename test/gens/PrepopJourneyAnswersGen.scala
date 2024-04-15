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

import models.frontend.prepop.{AdjustmentsPrepopAnswers, IncomePrepopAnswers}
import org.scalacheck.Gen

object PrepopJourneyAnswersGen {

  val incomePrepopAnswersGen: Gen[IncomePrepopAnswers] = for {
    turnoverIncome <- Gen.option(bigDecimalGen)
    otherIncome    <- Gen.option(bigDecimalGen)
  } yield IncomePrepopAnswers(turnoverIncome, otherIncome)

  val adjustmentsPrepopAnswersGen: Gen[AdjustmentsPrepopAnswers] = for {
    includedNonTaxableProfits <- Gen.option(bigDecimalGen)
    basisAdjustment           <- Gen.option(bigDecimalGen)
    overlapReliefUsed         <- Gen.option(bigDecimalGen)
    accountingAdjustment      <- Gen.option(bigDecimalGen)
    averagingAdjustment       <- Gen.option(bigDecimalGen)
    lossBroughtForward        <- Gen.option(bigDecimalGen)
    outstandingBusinessIncome <- Gen.option(bigDecimalGen)
    balancingChargeBpra       <- Gen.option(bigDecimalGen)
    balancingChargeOther      <- Gen.option(bigDecimalGen)
    goodsAndServicesOwnUse    <- Gen.option(bigDecimalGen)
  } yield AdjustmentsPrepopAnswers(
    includedNonTaxableProfits,
    basisAdjustment,
    overlapReliefUsed,
    accountingAdjustment,
    averagingAdjustment,
    lossBroughtForward,
    outstandingBusinessIncome,
    balancingChargeBpra,
    balancingChargeOther,
    goodsAndServicesOwnUse
  )

}
