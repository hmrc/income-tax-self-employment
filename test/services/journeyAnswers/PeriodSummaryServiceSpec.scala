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
import models.common.{BusinessId, TaxYear}
import models.connector.api_1786.{DeductionsType, DeductionsTypeTestData, FinancialsType, IncomeTypeTestData, IncomesType}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Deductions, Incomes, SelfEmploymentDeductionsDetailAllowablePosNegType, SelfEmploymentDeductionsDetailPosNegType, SelfEmploymentDeductionsDetailType}
import models.error.ServiceError
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import repositories.JourneyAnswersRepository
import services.journeyAnswers.expenses.PeriodSummaryService
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global


class PeriodSummaryServiceSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with DefaultAwaitTimeout {

  //val connector: StubIFSConnector = new StubIFSConnector()
  val repo: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  val service = new PeriodSummaryService(
    MockIFSConnector.mockInstance,
    MockJourneyAnswersRepository.mockInstance
  )

  "save answers" should {

//    "saveTravelExpensesAnswers" in {
//      val answers: TravelExpensesDb = TravelExpensesDb(
//        totalTravelExpenses = Some(200.00),
//        disallowableTravelExpenses = Some(100.00)
//      )
//
//      val result: Either[ServiceError, Unit]    = await(service.saveTravelExpenses(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }

//    "saveOfficeSuppliesAnswers 22 " in {
//      val answers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)
//
//      MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
//        returnValue = Right(api1786DeductionsSuccessResponse.copy(
//          financials = FinancialsType(Option(DeductionsTypeTestData.sample), Option(IncomeTypeTestData.sample))))
//      )
//
//
//      val sample2: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(
//        Some(Incomes(
//          Some(100.00),
//          None,
//          None)
//        ),
//        Some(Deductions(
//          costOfGoods = None,
//          constructionIndustryScheme = None,
//          staffCosts = None,
//          premisesRunningCosts = None,
//          maintenanceCosts = None,
//          adminCosts = None,
//          businessEntertainmentCosts = None,
//          advertisingCosts = None,
//          interest = None,
//          financialCharges = None,
//          badDebt = None,
//          professionalFees = None,
//          depreciation = None,
//          other = None,
//          simplifiedExpenses = None,
//          travelCosts = Some(
//            SelfEmploymentDeductionsDetailType(
//              200,
//              Some(100)
//            )
//          ))
//        )
//      )
//
//      val dataSample: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(
//        taxYear,
//        nino,
//        businessId,
//        sample2
//      )
//
//      MockIFSConnector.amendSEPeriodSummary(dataSample)(Right(()))
//
//      val result: Either[ServiceError, Unit] = await(service.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value)
//
//      result shouldBe Right(())
//    }

    "saveOfficeSuppliesAnswers " in {
      val answers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)

      val deductTestData: Option[DeductionsType] = Some(DeductionsTypeTestData.sample)
      val incomeTestData: Option[IncomesType] = Some(IncomeTypeTestData.sample)

      MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
        returnValue = Right(api1786DeductionsSuccessResponse.copy(
          financials = FinancialsType(deductTestData, incomeTestData)))
      )

      val amendBody = AmendSEPeriodSummaryRequestBody(
        incomes = incomeTestData.map(_.toApi1895),
        deductions = deductTestData.map(_.toApi1895)
      )

      val dataSample: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(
        currTaxYear,
        nino,
        businessId,
        amendBody
      )

      val dataSample2 = AmendSEPeriodSummaryRequestData(
        taxYear = taxYear,
        nino = nino,
        businessId = businessId,
        body = AmendSEPeriodSummaryRequestBody(
          incomes = Some(Incomes(Some(1.0), Some(2.0), Some(3.0))),
          deductions = Some(Deductions(
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(9.0), Some(10.0))),
            Some(SelfEmploymentDeductionsDetailType(7.0, Some(8.0))),
            Some(SelfEmploymentDeductionsDetailType(25.0, Some(26.0))),
            Some(SelfEmploymentDeductionsDetailType(27.0, Some(28.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(23.0), Some(24.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(17.0), Some(18.0))),
            Some(SelfEmploymentDeductionsDetailType(0.00, Some(4104.13))),
            Some(SelfEmploymentDeductionsDetailType(29.0, Some(30.0))),
            Some(SelfEmploymentDeductionsDetailType(3.0, Some(4.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(15.0), Some(16.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(13.0), Some(14.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(5.0), Some(6.0))),
            Some(SelfEmploymentDeductionsDetailAllowablePosNegType(Some(21.0), Some(22.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(11.0), Some(12.0))),
            Some(SelfEmploymentDeductionsDetailType(19.0, Some(20.0))),
            Some(31)
          ))
        )
      )

      MockIFSConnector.amendSEPeriodSummary(dataSample.copy(

        taxYear = taxYear,
        nino = nino,
        businessId = businessId,
        body = AmendSEPeriodSummaryRequestBody(
          incomes = Some(Incomes(Some(1.0), Some(2.0), Some(3.0))),
          deductions = Some(Deductions(
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(9.0), Some(10.0))),
            Some(SelfEmploymentDeductionsDetailType(7.0, Some(8.0))),
            Some(SelfEmploymentDeductionsDetailType(25.0, Some(26.0))),
            Some(SelfEmploymentDeductionsDetailType(27.0, Some(28.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(23.0), Some(24.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(17.0), Some(18.0))),
            Some(SelfEmploymentDeductionsDetailType(0.00, Some(4104.13))),
            Some(SelfEmploymentDeductionsDetailType(29.0, Some(30.0))),
            Some(SelfEmploymentDeductionsDetailType(3.0, Some(4.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(15.0), Some(16.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(13.0), Some(14.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(5.0), Some(6.0))),
            Some(SelfEmploymentDeductionsDetailAllowablePosNegType(Some(21.0), Some(22.0))),
            Some(SelfEmploymentDeductionsDetailPosNegType(Some(11.0), Some(12.0))),
            Some(SelfEmploymentDeductionsDetailType(19.0, Some(20.0))),
            Some(31)
          )))
      ))(Right(()))

      val result: Either[ServiceError, Unit] = await(service.saveOfficeSuppliesAnswers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

//    "saveGoodsToSell" in {
//      val answers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]      = await(service.saveGoodsToSell(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveRepairsAndMaintenance" in {
//      val answers: RepairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]                = await(service.saveRepairsAndMaintenance(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveWorkplaceRunningCosts" in {
//      val answers: WorkplaceRunningCostsJourneyAnswers = genOne(workplaceRunningCostsJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]           = await(service.saveWorkplaceRunningCosts(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveAdvertisingOrMarketing" in {
//      val answers: AdvertisingOrMarketingJourneyAnswers = genOne(advertisingOrMarketingJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]            = await(service.saveAdvertisingOrMarketing(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveEntertainmentCosts" in {
//      val answers: EntertainmentJourneyAnswers = genOne(entertainmentJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]   = await(service.saveEntertainmentCosts(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveStaffCosts" in {
//      val answers: StaffCostsJourneyAnswers  = genOne(staffCostsJourneyAnswersGen)
//      val result: Either[ServiceError, Unit] = await(service.saveStaffCosts(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveConstructionIndustrySubcontractors" in {
//      val answers: ConstructionJourneyAnswers = genOne(constructionJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]  = await(service.saveConstructionIndustrySubcontractors(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveProfessionalFees" in {
//      val answers: ProfessionalFeesJourneyAnswers = genOne(professionalFeesJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]      = await(service.saveProfessionalFees(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveFinancialCharges" in {
//      val answers: FinancialChargesJourneyAnswers = genOne(financialChargesJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]      = await(service.saveFinancialCharges(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveBadDebts" in {
//      val answers: IrrecoverableDebtsJourneyAnswers = genOne(irrecoverableDebtsJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]        = await(service.saveBadDebts(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveDepreciationCosts" in {
//      val answers: DepreciationCostsJourneyAnswers = genOne(depreciationCostsJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]       = await(service.saveDepreciationCosts(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveOtherExpenses" in {
//      val answers: OtherExpensesJourneyAnswers = genOne(otherExpensesJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]   = await(service.saveOtherExpenses(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "saveInterests" in {
//      val answers: InterestJourneyAnswers    = genOne(interestJourneyAnswersGen)
//      val result: Either[ServiceError, Unit] = await(service.saveInterests(journeyCtxWithNino, answers).value)
//
//      result shouldBe ().asRight
//    }
//
//    "goodsToSell successfully" in {
//      val someExpensesAnswers: GoodsToSellOrUseJourneyAnswers = genOne(goodsToSellOrUseJourneyAnswersGen)
//      val result: Either[ServiceError, Unit]                  = await(service.saveGoodsToSell(journeyCtxWithNino, someExpensesAnswers).value)
//
//      result shouldBe ().asRight
//    }

  }
}
