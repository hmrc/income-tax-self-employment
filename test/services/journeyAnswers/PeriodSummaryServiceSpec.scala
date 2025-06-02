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


import cats.data.EitherT
import cats.data._
import cats.implicits._
import cats.instances.future._
import cats.implicits.catsSyntaxEitherId
import gens.ExpensesJourneyAnswersGen._
import gens.genOne
import mocks.connectors.MockIFSConnector
import mocks.services.MockPeriodSummaryService
import models.connector.api_1786.{DeductionsTypeTestData, FinancialsType, IncomeTypeTestData}
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestBodyTestData.dataSample
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBodyTestData, DeductionsTestData}
import models.domain.ApiResultT
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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.JourneyAnswersRepository
import services.journeyAnswers.expenses.PeriodSummaryService
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import utils.BaseSpec
import utils.BaseSpec.journeyCtxWithNino

import scala.concurrent.Future


class PeriodSummaryServiceSpec extends AnyWordSpec with Matchers with BaseSpec {

  val connector: StubIFSConnector = new StubIFSConnector()
  val repo: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  val service = new PeriodSummaryService(connector, repo)
  
  "save answers" should {

    "saveOfficeSuppliesAnswers" in {
      val answers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]    = service.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveGoodsToSell" in {
      val answers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = service.saveGoodsToSell(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveRepairsAndMaintenance" in {
      val answers: RepairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                = service.saveRepairsAndMaintenance(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveWorkplaceRunningCosts" in {
      val answers: WorkplaceRunningCostsJourneyAnswers = genOne(workplaceRunningCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]           = service.saveWorkplaceRunningCosts(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveAdvertisingOrMarketing" in {
      val answers: AdvertisingOrMarketingJourneyAnswers = genOne(advertisingOrMarketingJourneyAnswersGen)
      val result: Either[ServiceError, Unit]            = service.saveAdvertisingOrMarketing(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveEntertainmentCosts" in {
      val answers: EntertainmentJourneyAnswers = genOne(entertainmentJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = service.saveEntertainmentCosts(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveStaffCosts" in {
      val answers: StaffCostsJourneyAnswers  = genOne(staffCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = service.saveStaffCosts(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveConstructionIndustrySubcontractors" in {
      val answers: ConstructionJourneyAnswers = genOne(constructionJourneyAnswersGen)
      val result: Either[ServiceError, Unit]  = service.saveConstructionIndustrySubcontractors(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveProfessionalFees" in {
      val answers: ProfessionalFeesJourneyAnswers = genOne(professionalFeesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = service.saveProfessionalFees(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveFinancialCharges" in {
      val answers: FinancialChargesJourneyAnswers = genOne(financialChargesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]      = service.saveFinancialCharges(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveBadDebts" in {
      val answers: IrrecoverableDebtsJourneyAnswers = genOne(irrecoverableDebtsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]        = service.saveBadDebts(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveDepreciationCosts" in {
      val answers: DepreciationCostsJourneyAnswers = genOne(depreciationCostsJourneyAnswersGen)
      val result: Either[ServiceError, Unit]       = service.saveDepreciationCosts(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveOtherExpenses" in {
      val answers: OtherExpensesJourneyAnswers = genOne(otherExpensesJourneyAnswersGen)
      val result: Either[ServiceError, Unit]   = service.saveOtherExpenses(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "saveInterests" in {
      val answers: InterestJourneyAnswers    = genOne(interestJourneyAnswersGen)
      val result: Either[ServiceError, Unit] = service.saveInterests(journeyCtxWithNino, answers).value.futureValue
      
      result shouldBe ().asRight
    }

    "goodsToSell successfully" in {
      val someExpensesAnswers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
      val result: Either[ServiceError, Unit]                  = service.saveGoodsToSell(journeyCtxWithNino, someExpensesAnswers).value.futureValue

      result shouldBe ().asRight
    }

  }
}
