
package controllers

import base.IntegrationBaseSpec
import helpers.WiremockSpec
import models.connector.ReliefClaimType
import models.connector.api_1505.CreateLossClaimRequestBody
import models.connector.api_1803.{AnnualAllowancesType, SuccessResponseSchema}
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import org.scalatest.matchers.should.Matchers
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.{JsValue, Json}

class JourneyAnswersControllerISpec extends WiremockSpec with Matchers {

  "POST /:taxYear/:businessId/profit-or-loss/:nino/answers" should {
    "Save answers and return NO CONTENT" in new IntegrationBaseSpec {
      val answers: JsValue = Json.toJson(ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = true,
        goodsAndServicesAmount = Some(200),
        claimLossRelief = Some(true),
        whatDoYouWantToDoWithLoss = None,
        carryLossForward = Some(true),
        previousUnusedLosses = true,
        unusedLossAmount = Some(BigDecimal("200")),
        whichYearIsLossReported = Some(WhichYearIsLossReported.Year2018to2019)
      ))

      val api1803Response: JsValue = Json.toJson(SuccessResponseSchema(
        annualAdjustments = None,
        annualAllowances = Some(AnnualAllowancesType.emptyAnnualAllowancesType.copy(
          zeroEmissionsCarAllowance = Some(5000.00),
          zeroEmissionGoodsVehicleAllowance = Some(5000.00)
        )),
        annualNonFinancials = None
      ))

      val api1505Response: JsValue = Json.toJson(CreateLossClaimRequestBody(
        incomeSourceId = businessId.value,
        reliefClaimed = ReliefClaimType.CF.toString,
        taxYear = taxYear.endYear.toString
      ))

      stubPostWithResponseBody(
        url = s"/$taxYear/$businessId/profit-or-loss/$nino/answers",
        expectedResponse = api1803Response,
        expectedStatus = NO_CONTENT
      )

      buildClient(s"/$taxYear/$businessId/profit-or-loss/$nino/answers")
        .post(answers)
        .futureValue
        .status shouldBe NO_CONTENT

    }
  }


}
