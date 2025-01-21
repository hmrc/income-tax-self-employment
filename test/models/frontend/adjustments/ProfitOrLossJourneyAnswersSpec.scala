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

package models.frontend.adjustments

import models.common.JourneyContextWithNino
import models.connector.ReliefClaimType.{CF, CSGI}
import models.connector.api_1505.CreateLossClaimRequestBody
import models.connector.{ClaimId, ReliefClaimType, api_1508}
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.{CarryItForward, DeductFromOtherTypes}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import utils.BaseSpec.{businessId, currTaxYear, mtditid, nino}

import java.time.{LocalDate, LocalDateTime}

class ProfitOrLossJourneyAnswersSpec extends AnyWordSpecLike with Matchers {

  val journeyCtxWithNino: JourneyContextWithNino = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

  "ProfitOrLossJourneyAnswers" when {

    "calling toLossClaimSubmission" should {

      "return Some(CreateLossClaimRequestBody) when conditions are met" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Some(BigDecimal(1000)),
          claimLossRelief = Some(true),
          whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
          carryLossForward = Some(true),
          previousUnusedLosses = true,
          unusedLossAmount = Some(BigDecimal(500)),
          whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
        )

        val result = answers.toLossClaimSubmission(journeyCtxWithNino)

        result shouldBe Some(
          CreateLossClaimRequestBody(
            incomeSourceId = "SJPR05893938418",
            reliefClaimed = ReliefClaimType.CF.toString,
            taxYear = LocalDate.now().getYear.toString
          ))
      }

      "return None when carryLossForward is false" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Some(BigDecimal(1000)),
          claimLossRelief = Some(true),
          whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
          carryLossForward = Some(false),
          previousUnusedLosses = true,
          unusedLossAmount = Some(BigDecimal(500)),
          whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
        )

        val result = answers.toLossClaimSubmission(journeyCtxWithNino)

        result shouldBe None
      }

      "return None when whatDoYouWantToDoWithLoss is None" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Some(BigDecimal(1000)),
          claimLossRelief = Some(true),
          whatDoYouWantToDoWithLoss = None,
          carryLossForward = Some(true),
          previousUnusedLosses = true,
          unusedLossAmount = Some(BigDecimal(500)),
          whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
        )

        val result = answers.toLossClaimSubmission(journeyCtxWithNino)

        result shouldBe None
      }

      "return None when carryLossForward is None" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Some(BigDecimal(1000)),
          claimLossRelief = Some(true),
          whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
          carryLossForward = None,
          previousUnusedLosses = true,
          unusedLossAmount = Some(BigDecimal(500)),
          whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
        )

        val result = answers.toLossClaimSubmission(journeyCtxWithNino)

        result shouldBe None
      }
    }

    "calling ProfitOrLossJourneyAnswers apply method" should {

      "return ProfitOrLossJourneyAnswers with updated data from GetLossClaimSuccessResponse" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Option(BigDecimal(1000)),
          claimLossRelief = Option(true),
          whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.DeductFromOtherTypes, WhatDoYouWantToDoWithLoss.CarryItForward)),
          carryLossForward = None,
          previousUnusedLosses = true,
          unusedLossAmount = Option(BigDecimal(500)),
          whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
        )

        val apiResponse: api_1508.GetLossClaimSuccessResponse =
          api_1508.GetLossClaimSuccessResponse(
            incomeSourceId = "012345678912345",
            reliefClaimed = CF,
            claimId = ClaimId("AAZZ1234567890A"),
            sequence = Option(2),
            submissionDate = LocalDateTime.now())

        val result = ProfitOrLossJourneyAnswers.apply(apiResponse = apiResponse, journeyAnswers = answers)

        result shouldBe answers.copy(whatDoYouWantToDoWithLoss = Option(List(CarryItForward)), carryLossForward = Option(true))

      }

      "return ProfitOrLossJourneyAnswers with updated data from GetLossClaimSuccessResponse when reliefClaimed is CSGI" in {
        val answers = ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Option(BigDecimal(1000)),
          claimLossRelief = Option(true),
          whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.DeductFromOtherTypes, WhatDoYouWantToDoWithLoss.CarryItForward)),
          carryLossForward = None,
          previousUnusedLosses = true,
          unusedLossAmount = Option(BigDecimal(500)),
          whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
        )

        val apiResponse: api_1508.GetLossClaimSuccessResponse =
          api_1508.GetLossClaimSuccessResponse(
            incomeSourceId = "012345678912345",
            reliefClaimed = CSGI,
            claimId = ClaimId("AAZZ1234567890A"),
            sequence = Option(2),
            submissionDate = LocalDateTime.now())

        val result = ProfitOrLossJourneyAnswers.apply(apiResponse = apiResponse, journeyAnswers = answers)

        result shouldBe answers.copy(whatDoYouWantToDoWithLoss = Option(List(DeductFromOtherTypes)), carryLossForward = Option(false))

      }

    }
  }
}
