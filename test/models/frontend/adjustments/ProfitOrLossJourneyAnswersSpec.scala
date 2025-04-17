/*
 * Copyright 2025 HM Revenue & Customs
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

import data.TimeData
import models.common.JourneyContextWithNino
import models.connector.ReliefClaimType.{CF, CSGI}
import models.connector.api_1500.{CreateBroughtForwardLossRequestData, LossType}
import models.connector.api_1501.{UpdateBroughtForwardLossRequestData, UpdateBroughtForwardLossYear}
import models.connector.api_1802.request.AnnualAdjustments
import models.connector.api_1870.LossData
import models.connector.common.ReliefClaim
import models.database.adjustments.ProfitOrLossDb
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.{CarryItForward, DeductFromOtherTypes}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import utils.BaseSpec._

import java.time.LocalDateTime

class ProfitOrLossJourneyAnswersSpec extends AnyWordSpecLike with Matchers with TimeData {

  val journeyCtxWithNino: JourneyContextWithNino = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

  "ProfitOrLossJourneyAnswers" should {

    "read and write successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Option(BigDecimal(100.00)),
        claimLossRelief = Option(true),
        whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Option(true),
        previousUnusedLosses = true,
        unusedLossAmount = Option(BigDecimal(200.00)),
        whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
      )

      val json: JsValue                           = Json.toJson(answers)
      val readAnswers: ProfitOrLossJourneyAnswers = json.as[ProfitOrLossJourneyAnswers]

      readAnswers shouldEqual answers
    }

    "convert to database model successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Option(BigDecimal(100.00)),
        claimLossRelief = Option(true),
        whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Option(true),
        previousUnusedLosses = true,
        unusedLossAmount = Option(BigDecimal(200.00)),
        whichYearIsLossReported = Option(WhichYearIsLossReported.Year2018to2019)
      )

      val dbModel: Option[ProfitOrLossDb] = answers.toDbModel
      dbModel shouldEqual Option(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Option(true), previousUnusedLosses = true))
    }

    "convert to downstream annual adjustments successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Option(BigDecimal(100.00)),
        claimLossRelief = Option(true),
        whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Option(true),
        previousUnusedLosses = true,
        unusedLossAmount = Option(BigDecimal(200.00)),
        whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
      )

      val annualAdjustments: AnnualAdjustments = answers.toDownStreamAnnualAdjustments(None)
      annualAdjustments.goodsAndServicesOwnUse shouldEqual Option(BigDecimal(100.00))
    }

    "create ProfitOrLossJourneyAnswers from the input data as optLossData is None and reliefClaims is empty and goodsAndServicesOwnUse is None" in {

      ProfitOrLossJourneyAnswers.apply(None, reliefClaims = Nil, optLossData = None) shouldEqual ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = false,
        goodsAndServicesAmount = None,
        claimLossRelief = None,
        whatDoYouWantToDoWithLoss = None,
        carryLossForward = None,
        previousUnusedLosses = false,
        unusedLossAmount = None,
        whichYearIsLossReported = None
      )
    }

    "create ProfitOrLossJourneyAnswers from the input data as optLossData is None and reliefClaims is empty" in {

      ProfitOrLossJourneyAnswers.apply(Option(200), reliefClaims = Nil, optLossData = None) shouldEqual ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        Option(200),
        None,
        None,
        None,
        previousUnusedLosses = false,
        None,
        None)
    }

    "create ProfitOrLossJourneyAnswers from the valid inputData" in {
      val claims: List[ReliefClaim] = List(
        ReliefClaim("XAIS12345678900", None, CF, "2025", "12345", None, LocalDateTime.parse("2024-10-01T12:13:48.763"))
      )

      val lossData = Option(LossData("5678", "SJPR05893938418", LossType.SelfEmployment, 400, "2018-19", testDateTime))

      ProfitOrLossJourneyAnswers.apply(Option(200), reliefClaims = claims, optLossData = lossData) shouldEqual ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        Option(200),
        Option(true),
        Option(List(CarryItForward)),
        Option(true),
        previousUnusedLosses = true,
        Option(400),
        Option(WhichYearIsLossReported.Year2018to2019)
      )
    }

    "create ProfitOrLossJourneyAnswers from the input data as optLossData is None" in {
      val claims: List[ReliefClaim] = List(
        ReliefClaim("XAIS12345678900", None, CF, "2025", "12345", None, LocalDateTime.parse("2024-10-01T12:13:48.763")),
        ReliefClaim("XAIS12345678901", None, CSGI, "2024", "1234567890", None, LocalDateTime.parse("2024-10-01T12:13:48.763"))
      )

      ProfitOrLossJourneyAnswers.apply(None, reliefClaims = claims, optLossData = None) shouldEqual ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = false,
        None,
        Option(true),
        Option(List(CarryItForward, DeductFromOtherTypes)),
        Option(true),
        previousUnusedLosses = false,
        None,
        None
      )

    }
  }

  "BroughtForwardLossYearData" should {

    "CreateBroughtForwardLoss data successfully" in {
      val unusedLossAmount: BigDecimal                     = BigDecimal(500.00)
      val whichYearIsLossReported: WhichYearIsLossReported = WhichYearIsLossReported.Year2022to2023

      val result: CreateBroughtForwardLossRequestData =
        ProfitOrLossJourneyAnswers.toCreateBroughtForwardLossData(journeyCtxWithNino, unusedLossAmount, whichYearIsLossReported)

      result.body.lossAmount shouldEqual unusedLossAmount
      result.body.taxYearBroughtForwardFrom shouldEqual whichYearIsLossReported.apiTaxYear
      result.body.typeOfLoss shouldEqual LossType.SelfEmployment
    }

    "UpdateBroughtForwardLoss data successfully" in {
      val lossId: String               = "lossId123"
      val unusedLossAmount: BigDecimal = BigDecimal(400.00)

      val result: UpdateBroughtForwardLossRequestData =
        ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossData(journeyCtxWithNino, lossId, unusedLossAmount)

      result.body.lossAmount shouldEqual unusedLossAmount
    }

    "UpdateBroughtForwardLossYear data successfully" in {
      val lossId: String     = "lossId123"
      val amount: BigDecimal = BigDecimal(300.00)
      val whichYear: String  = "2024"

      val result: UpdateBroughtForwardLossYear =
        ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossYearData(journeyCtxWithNino, lossId, amount, whichYear)

      result.lossId shouldEqual lossId
      result.body.lossAmount shouldEqual amount
      result.body.taxYearBroughtForwardFrom shouldEqual whichYear
      result.body.typeOfLoss shouldEqual LossType.SelfEmployment
    }
  }

}
