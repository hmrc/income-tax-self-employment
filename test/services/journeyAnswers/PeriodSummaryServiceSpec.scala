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

package services.journeyAnswers

import cats.implicits.catsSyntaxEitherId
import gens.ExpensesJourneyAnswersGen._
import gens.genOne
import mocks.connectors.MockIFSConnector
import mocks.repositories.MockJourneyAnswersRepository
import models.common.JourneyName
import models.common.JourneyStatus.Completed
import models.connector.api_1786._
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import models.database.JourneyAnswers
import models.database.expenses.travel.TravelExpensesDb
import models.error.ServiceError
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsJourneyAnswers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.JsObject
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import repositories.JourneyAnswersRepository
import services.journeyAnswers.expenses.PeriodSummaryService
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global


class PeriodSummaryServiceSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with DefaultAwaitTimeout {

  val repo: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  val service = new PeriodSummaryService(
    MockIFSConnector.mockInstance,
    MockJourneyAnswersRepository.mockInstance
  )

  val deductTestData: Option[DeductionsType] = Some(DeductionsTypeTestData.sample)
  val incomeTestData: Option[IncomesType] = Some(IncomeTypeTestData.sample)

  MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
    returnValue = Right(api1786DeductionsSuccessResponse.copy(
      financials = FinancialsType(deductTestData, incomeTestData)))
  )

  val amendBody: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(
    incomes = incomeTestData.map(_.toApi1895),
    deductions = deductTestData.map(_.toApi1895)
  )

  val dataSample: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(
    currTaxYear,
    nino,
    businessId,
    amendBody
  )

  MockIFSConnector.amendSEPeriodSummary(dataSample)(Right(()))

  "get answers" should {

    "getTravelExpensesAnswers" in {
      val now: Instant   = Instant.now()
      val journeyAnswers = Some(JourneyAnswers(mtditid, businessId, taxYear, JourneyName.TravelExpenses, Completed, JsObject.empty, now, now, now))

      MockJourneyAnswersRepository.get(travelExpensesCtx)(journeyAnswers)

      val result: Either[ServiceError, Option[TravelExpensesDb]] = await(service.getTravelExpensesAnswers(journeyCtxWithNino).value)

      result shouldBe Right(Some(TravelExpensesDb(None, None, None, None, None)))
    }
  }

  "save answers" should {

    "saveTravelExpensesAnswers" in {
      val answers: TravelExpensesDb = TravelExpensesDb(
        totalTravelExpenses = Some(200.00),
        disallowableTravelExpenses = Some(100.00)
      )

      val result: Either[ServiceError, Unit] = await(service.saveTravelExpenses(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveOfficeSuppliesAnswers " in {
      val answers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)

      val result: Either[ServiceError, Unit] = await(service.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveGoodsToSell" in {
      val answers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = await(service.saveGoodsToSell(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveRepairsAndMaintenance" in {
      val answers: RepairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                = await(service.saveRepairsAndMaintenance(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveWorkplaceRunningCosts" in {
      val answers: WorkplaceRunningCostsJourneyAnswers = genOne(workplaceRunningCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]           = await(service.saveWorkplaceRunningCosts(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveAdvertisingOrMarketing" in {
      val answers: AdvertisingOrMarketingJourneyAnswers = genOne(advertisingOrMarketingJourneyAnswersGen)
      val result: Either[ServiceError, Unit]            = await(service.saveAdvertisingOrMarketing(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveEntertainmentCosts" in {
      val answers: EntertainmentJourneyAnswers = genOne(entertainmentJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = await(service.saveEntertainmentCosts(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "saveStaffCosts" in {
      val answers: StaffCostsJourneyAnswers  = genOne(staffCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = await(service.saveStaffCosts(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveConstructionIndustrySubcontractors" in {
      val answers: ConstructionJourneyAnswers = genOne(constructionJourneyAnswersGen)
      val result: Either[ServiceError, Unit]  = await(service.saveConstructionIndustrySubcontractors(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveProfessionalFees" in {
      val answers: ProfessionalFeesJourneyAnswers = genOne(professionalFeesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = await(service.saveProfessionalFees(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveFinancialCharges" in {
      val answers: FinancialChargesJourneyAnswers = genOne(financialChargesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = await(service.saveFinancialCharges(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveBadDebts" in {
      val answers: IrrecoverableDebtsJourneyAnswers = genOne(irrecoverableDebtsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]        = await(service.saveBadDebts(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveDepreciationCosts" in {
      val answers: DepreciationCostsJourneyAnswers = genOne(depreciationCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]       = await(service.saveDepreciationCosts(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveOtherExpenses" in {
      val answers: OtherExpensesJourneyAnswers = genOne(otherExpensesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = await(service.saveOtherExpenses(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "saveInterests" in {
      val answers: InterestJourneyAnswers    = genOne(interestJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = await(service.saveInterests(journeyCtxWithNino, answers).value)

      result shouldBe ().asRight
    }

    "goodsToSell successfully" in {
      val someExpensesAnswers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                  = await(service.saveGoodsToSell(journeyCtxWithNino, someExpensesAnswers).value)

      result shouldBe ().asRight
    }

  }
}
