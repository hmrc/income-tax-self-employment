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

package data

import models.common._
import models.database.JourneyAnswers
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers.ExpensesTailoringIndividualCategoriesAnswers
import models.frontend.expenses.tailoring.individualCategories.{
  AdvertisingOrMarketing => AdvertisingOrMarketingAnswer,
  GoodsToSellOrUse => GoodsToSellOrUseAnswer,
  OfficeSupplies => OfficeSuppliesAnswer,
  OtherExpenses => OtherExpensesAnswer,
  RepairsAndMaintenance => RepairsAndMaintenanceAnswer,
  TravelForWork => TravelForWorkAnswer,
  WorkFromBusinessPremises => WorkFromBusinessPremisesAnswer
}
import play.api.libs.json.{JsObject, JsValue}

import java.time.temporal.ChronoUnit

trait CommonTestData extends TimeData {

  val testBusinessId: BusinessId = BusinessId("XH1234567890")
  val testMtdId: Mtditid         = Mtditid("12345")
  val testNino: Nino             = Nino("AB123456C")

  val testCurrentTaxYear: TaxYear = TaxYear(2025)
  val testPrevTaxYear: TaxYear    = TaxYear(2024)
  val testTaxYear                 = testCurrentTaxYear
  val testTradingName: String     = "Test Trading Name"

  val testContextCurrentYear: JourneyContextWithNino = JourneyContextWithNino(testCurrentTaxYear, testBusinessId, testMtdId, testNino)
  val testContextPrevYear: JourneyContextWithNino    = JourneyContextWithNino(testPrevTaxYear, testBusinessId, testMtdId, testNino)

  val testServiceError: ServiceError = new ServiceError {
    val errorMessage: String = "Returned a service error"
  }

  def testJourneyAnswers(journeyName: JourneyName, json: JsValue): JourneyAnswers =
    JourneyAnswers(
      mtditid = testMtdId,
      businessId = testBusinessId,
      taxYear = testTaxYear,
      journey = journeyName,
      status = JourneyStatus.InProgress,
      data = json.as[JsObject],
      expireAt = testInstant.plus(1, ChronoUnit.MINUTES),
      createdAt = testInstant,
      updatedAt = testInstant
    )

  val testExpensesTailoring: ExpensesTailoringIndividualCategoriesAnswers = ExpensesTailoringIndividualCategoriesAnswers(
    officeSupplies = OfficeSuppliesAnswer.No,
    goodsToSellOrUse = GoodsToSellOrUseAnswer.No,
    repairsAndMaintenance = RepairsAndMaintenanceAnswer.No,
    workFromHome = false,
    workFromBusinessPremises = WorkFromBusinessPremisesAnswer.No,
    travelForWork = TravelForWorkAnswer.No,
    advertisingOrMarketing = AdvertisingOrMarketingAnswer.No,
    entertainmentCosts = None,
    professionalServiceExpenses = Nil,
    financialExpenses = Nil,
    depreciation = false,
    otherExpenses = OtherExpensesAnswer.No,
    disallowableInterest = None,
    disallowableOtherFinancialCharges = None,
    disallowableIrrecoverableDebts = None,
    disallowableStaffCosts = None,
    disallowableSubcontractorCosts = None,
    disallowableProfessionalFees = None
  )

  val testCapitalAllowancesTailoring: CapitalAllowancesTailoringAnswers = CapitalAllowancesTailoringAnswers(
    claimCapitalAllowances = false,
    selectCapitalAllowances = Nil
  )

}
