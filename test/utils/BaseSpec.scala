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

package utils

import data.TimeData
import models.common.JourneyName.TradeDetails
import models.common._
import models.database.JourneyAnswers
import org.mockito.ArgumentMatchersSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, DefaultActionBuilder}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait BaseSpec extends AnyWordSpec with MockitoSugar with ArgumentMatchersSugar with ScalaFutures {

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  protected implicit val hc: HeaderCarrier    = HeaderCarrier()

  protected val stubControllerComponents: ControllerComponents = Helpers.stubControllerComponents()

  protected val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(stubControllerComponents.parsers.default)

  protected val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")
}

object BaseSpec extends TimeData {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  // static data
  val taxYear: TaxYear       = TaxYear(2024)
  val taxYearStart: String   = TaxYear.startDate(taxYear)
  val taxYearEnd: String     = TaxYear.endDate(taxYear)
  val businessId: BusinessId = BusinessId("SJPR05893938418")
  val nino: Nino             = Nino("nino")
  val mtditid: Mtditid       = Mtditid("1234567890")

  // dynamic & generated data
  val currTaxYear: TaxYear     = TaxYear(currentTaxYear.getYear)
  val currTaxYearStart: String = TaxYear.startDate(currTaxYear)
  val currTaxYearEnd: String   = TaxYear.endDate(currTaxYear)

  // more complex data
  val journeyCtxWithNino: JourneyContextWithNino    = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)
  val tradeDetailsCtx: JourneyContext               = journeyCtxWithNino.toJourneyContext(TradeDetails)
  val incomeCtx: JourneyContext                     = journeyCtxWithNino.toJourneyContext(JourneyName.Income)
  val expensesTailoringCtx: JourneyContext          = journeyCtxWithNino.toJourneyContext(JourneyName.ExpensesTailoring)
  val officeSuppliesCtx: JourneyContext             = journeyCtxWithNino.toJourneyContext(JourneyName.OfficeSupplies)
  val goodsToSellOrUseCtx: JourneyContext           = journeyCtxWithNino.toJourneyContext(JourneyName.GoodsToSellOrUse)
  val workplaceRunningCostsCtx: JourneyContext      = journeyCtxWithNino.toJourneyContext(JourneyName.WorkplaceRunningCosts)
  val repairsAndMaintenanceCostsCtx: JourneyContext = journeyCtxWithNino.toJourneyContext(JourneyName.RepairsAndMaintenanceCosts)
  val staffCostsCtx: JourneyContext                 = journeyCtxWithNino.toJourneyContext(JourneyName.StaffCosts)
  val constructionCostsCtx: JourneyContext          = journeyCtxWithNino.toJourneyContext(JourneyName.Construction)
  val professionalFeesCtx: JourneyContext           = journeyCtxWithNino.toJourneyContext(JourneyName.ProfessionalFees)
  val irrecoverableDebtsExpensesCtx: JourneyContext = journeyCtxWithNino.toJourneyContext(JourneyName.IrrecoverableDebts)
  val capitalAllowancesTailoringCtx: JourneyContext = journeyCtxWithNino.toJourneyContext(JourneyName.CapitalAllowancesTailoring)
  val zeroEmissionCarsCtx: JourneyContext           = journeyCtxWithNino.toJourneyContext(JourneyName.ZeroEmissionCars)
  val otherExpensesCtx: JourneyContext              = journeyCtxWithNino.toJourneyContext(JourneyName.OtherExpenses)
  val financialChargesCtx: JourneyContext           = journeyCtxWithNino.toJourneyContext(JourneyName.FinancialCharges)
  val advertisingOrMarketingCtx: JourneyContext     = journeyCtxWithNino.toJourneyContext(JourneyName.AdvertisingOrMarketing)
  val interestCtx: JourneyContext                   = journeyCtxWithNino.toJourneyContext(JourneyName.Interest)

  def mkJourneyAnswers(journey: JourneyName, status: JourneyStatus, data: JsObject): JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    journey,
    status,
    data,
    testInstant,
    testInstant,
    testInstant
  )

}
