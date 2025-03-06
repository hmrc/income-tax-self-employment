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
import models.connector.api_1500.{CreateBroughtForwardLossRequestData, LossType}
import models.connector.api_1501.{UpdateBroughtForwardLossRequestData, UpdateBroughtForwardLossYear}
import models.connector.api_1802.request.AnnualAdjustments
import models.database.adjustments.ProfitOrLossDb
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import utils.BaseSpec._

class ProfitOrLossJourneyAnswersSpec extends AnyWordSpecLike with Matchers with TimeData {

  val journeyCtxWithNino: JourneyContextWithNino = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

  "ProfitOrLossJourneyAnswers" should {

    "read and write successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Some(BigDecimal(100.00)),
        claimLossRelief = Some(true),
        whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Some(true),
        previousUnusedLosses = true,
        unusedLossAmount = Some(BigDecimal(200.00)),
        whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
      )

      val json: JsValue                           = Json.toJson(answers)
      val readAnswers: ProfitOrLossJourneyAnswers = json.as[ProfitOrLossJourneyAnswers]

      readAnswers shouldEqual answers
    }

    "convert to database model successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Some(BigDecimal(100.00)),
        claimLossRelief = Some(true),
        whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Some(true),
        previousUnusedLosses = true,
        unusedLossAmount = Some(BigDecimal(200.00)),
        whichYearIsLossReported = Some(WhichYearIsLossReported.Year2018to2019)
      )

      val dbModel: Option[ProfitOrLossDb] = answers.toDbModel
      dbModel shouldEqual Some(ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, claimLossRelief = Some(true), previousUnusedLosses = true))
    }

    "convert to downstream annual adjustments successfully" in {
      val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Some(BigDecimal(100.00)),
        claimLossRelief = Some(true),
        whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = Some(true),
        previousUnusedLosses = true,
        unusedLossAmount = Some(BigDecimal(200.00)),
        whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
      )

      val annualAdjustments: AnnualAdjustments = answers.toDownStreamAnnualAdjustments(None)
      annualAdjustments.goodsAndServicesOwnUse shouldEqual Some(BigDecimal(100.00))
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
