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

package controllers

import base.IntegrationBaseSpec
import models.connector.api_1803.{AnnualAllowancesType, SuccessResponseSchema}
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}

class JourneyAnswersControllerISpec extends IntegrationBaseSpec {

  // TODO ticket - create new tests and required stubbing for integration controller
  "POST /:taxYear/:businessId/profit-or-loss/:nino/answers" should {
    "Save answers and return NO CONTENT" ignore new IntegrationBaseSpec {
      val answers: JsValue = Json.toJson(
        ProfitOrLossJourneyAnswers(
          goodsAndServicesForYourOwnUse = true,
          goodsAndServicesAmount = Some(200),
          claimLossRelief = Some(true),
          whatDoYouWantToDoWithLoss = None,
          carryLossForward = Some(true),
          previousUnusedLosses = true,
          unusedLossAmount = Some(BigDecimal("200")),
          whichYearIsLossReported = Some(WhichYearIsLossReported.Year2018to2019)
        ))

      val api1803Response: JsValue = Json.toJson(
        SuccessResponseSchema(
          annualAdjustments = None,
          annualAllowances = Some(
            AnnualAllowancesType.emptyAnnualAllowancesType.copy(
              zeroEmissionsCarAllowance = Some(5000.00),
              zeroEmissionGoodsVehicleAllowance = Some(5000.00)
            )),
          annualNonFinancials = None
        ))

      stubPostWithResponseBody(
        url = s"/income-tax-self-employment/$testTaxYear/$testBusinessId/profit-or-loss/$testNino/answers",
        expectedResponse = api1803Response.toString(),
        expectedStatus = NO_CONTENT
      )

      buildClient(s"/income-tax-self-employment/$testTaxYear/$testBusinessId/profit-or-loss/$testNino/answers")
        .withHttpHeaders(("MTDITID", testMtdItId.value))
        .post(answers)
        .futureValue
        .status mustBe NO_CONTENT
    }
  }

}
