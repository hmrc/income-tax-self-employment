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

import builders.NICsAnswersBuilder._
import cats.data.EitherT
import cats.implicits._
import controllers.ControllerBehaviours.{buildRequest, buildRequestNoContent}
import data.journeyAnswersData.GoodsToSellOrUserTestAnswers._
import data.journeyAnswersData.WorkplaceRunningCostsTestAnswers._
import gens.CapitalAllowancesAnswersGen._
import gens.ExpensesJourneyAnswersGen._
import gens.ExpensesTailoringAnswersGen._
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import gens.PrepopJourneyAnswersGen.{annualAdjustmentsTypeGen, incomePrepopAnswersGen}
import gens.ProfitOrLossAnswersGen.profitOrLossAnswersGen
import gens.SelfEmploymentAbroadAnswersGen.selfEmploymentAbroadAnswersGen
import gens.genOne
import mocks.services.{MockExpensesAnswersService, MockPeriodSummaryService}
import models.common.JourneyName
import models.connector.Api1786ExpensesResponseParser
import models.database.expenses.WorkplaceRunningCostsDb
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.balancingCharge.BalancingChargeAnswers
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers
import models.frontend.capitalAllowances.structuresBuildings.NewStructuresBuildingsAnswers
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehicleAnswers
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import models.frontend.expenses.workplaceRunningCosts._
import models.frontend.nics.{NICsAnswers, NICsClass2Answers}
import models.frontend.prepop.AdjustmentsPrepopAnswers.fromAnnualAdjustmentsType
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status._
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent}
import services.journeyAnswers.CapitalAllowancesAnswersService
import services.journeyAnswers.expenses.{ExpensesAnswersService, PeriodSummaryService}
import stubs.serviceErrorT
import stubs.services._
import utils.BaseSpec._


class JourneyAnswersControllerSpec extends ControllerBehaviours with ScalaCheckPropertyChecks with TableDrivenPropertyChecks with OptionValues {

  private val mockPeriodSummaryService: PeriodSummaryService = MockPeriodSummaryService.mockInstance
  private val mockExpensesAnswersService: ExpensesAnswersService = MockExpensesAnswersService.mockInstance

  trait GetExpensesTest[T] {
    val expensesService: ExpensesAnswersService = mock[ExpensesAnswersService]
    val journeyAnswers: T

    val controller = new JourneyAnswersController(
      auth = mockAuthorisedAction,
      cc = stubControllerComponents,
      abroadAnswersService = StubAbroadAnswersService(),
      incomeService = StubIncomeAnswersService(),
      periodSummaryService = mockPeriodSummaryService,
      expensesService = mockExpensesAnswersService,
      capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
      prepopAnswersService = StubPrepopAnswersService(),
      nicsAnswersService = StubNICsAnswersService(),
      profitOrLossAnswersService = StubProfitOrLossAnswersService()
    )
  }

  private def mkUnderTest(abroadAnswersService: StubAbroadAnswersService = StubAbroadAnswersService(),
                          incomeService: StubIncomeAnswersService = StubIncomeAnswersService(),
                          expensesService: ExpensesAnswersService = mockExpensesAnswersService,
                          periodSummaryService: PeriodSummaryService = mockPeriodSummaryService,
                          capitalAllowancesService: StubCapitalAllowancesAnswersAnswersService = StubCapitalAllowancesAnswersAnswersService(),
                          prepopAnswersService: StubPrepopAnswersService = StubPrepopAnswersService(),
                          nicsAnswersService: StubNICsAnswersService = StubNICsAnswersService(),
                          profitOrLossAnswersService: StubProfitOrLossAnswersService = StubProfitOrLossAnswersService()): JourneyAnswersController =
    new JourneyAnswersController(
      auth = mockAuthorisedAction,
      cc = stubControllerComponents,
      abroadAnswersService = abroadAnswersService,
      incomeService = incomeService,
      periodSummaryService = periodSummaryService,
      expensesService = expensesService,
      capitalAllowancesService = capitalAllowancesService,
      prepopAnswersService = prepopAnswersService,
      nicsAnswersService = nicsAnswersService,
      profitOrLossAnswersService = profitOrLossAnswersService
    )

  val underTest: JourneyAnswersController = mkUnderTest()

  private def checkNoContent(action: Action[AnyContent]): Unit =
    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => action
    )

  private def mkJourneyAnswersController(capitalAllowancesService: CapitalAllowancesAnswersService) =
    new JourneyAnswersController(
      auth = mockAuthorisedAction,
      cc = stubControllerComponents,
      abroadAnswersService = StubAbroadAnswersService(),
      incomeService = StubIncomeAnswersService(),
      periodSummaryService = mockPeriodSummaryService,
      expensesService = mockExpensesAnswersService,
      capitalAllowancesService = capitalAllowancesService,
      prepopAnswersService = StubPrepopAnswersService(),
      nicsAnswersService = StubNICsAnswersService(),
      profitOrLossAnswersService = StubProfitOrLossAnswersService()
    )

  // TODO It's better to use lower testNoContent + testGetReturnAnswers + testSaveAnswers directly
  private def checkGetAndSave[A: Writes](actionForGetNoContent: Action[AnyContent],
                                         actionForGet: Action[AnyContent],
                                         expectedBodyForGet: String,
                                         dataGen: Gen[A],
                                         actionForSave: Action[AnyContent]): Unit = {

    testNoContent(actionForGetNoContent)
    testGetReturnAnswers(actionForGet, expectedBodyForGet)

    s"Save answers and return a $NO_CONTENT when successful" in {
      forAll(dataGen) { data =>
        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => actionForSave
        )
      }
    }
  }

  def testNoContent(actionForGetNoContent: Action[AnyContent]): Unit =
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(actionForGetNoContent)
    }

  def testGetReturnAnswers(actionForGet: Action[AnyContent], expectedBodyForGet: String): Unit =
    s"Get return answers" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = expectedBodyForGet,
        methodBlock = () => actionForGet
      )
    }


  def testSaveAnswers[A: Writes](actionForSave: Action[AnyContent], data: A): Unit =
    s"Save answers and return a $NO_CONTENT when successful" in {
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => actionForSave
      )
    }

  "SelfEmploymentAbroadAnswers" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(underTest.getSelfEmploymentAbroad(currTaxYear, businessId, nino))
    }

    "Get return answers" in {
      val answers = genOne(selfEmploymentAbroadAnswersGen)
      val underTest = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(getAnswersRes = Some(answers).asRight),
        incomeService = StubIncomeAnswersService(),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getSelfEmploymentAbroad(currTaxYear, businessId, nino)
      )
    }
    s"Save return a $NO_CONTENT when successful" in forAll(selfEmploymentAbroadAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveSelfEmploymentAbroad(currTaxYear, businessId, nino)
      )
    }

  }

  "IncomeAnswers" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(underTest.getIncomeAnswers(currTaxYear, businessId, nino))
    }

    s"Get return answers" in {
      val answers = genOne(incomeJourneyAnswersGen)
      val underTest = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(),
        incomeService = StubIncomeAnswersService(getAnswersRes = Option(answers).asRight),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getIncomeAnswers(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(incomeJourneyAnswersGen) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveIncomeAnswers(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $INTERNAL_SERVER_ERROR when downstream fails" in {
      val data            = incomeJourneyAnswersGen.sample.value
      val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.serviceUnavailable)

      val underTest = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(),
        incomeService = StubIncomeAnswersService(incomeJourneyAnswersRes = EitherT.leftT(downstreamError)),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = INTERNAL_SERVER_ERROR,
        expectedBody = "",
        methodBlock = () => underTest.saveIncomeAnswers(currTaxYear, businessId, nino)
      )
    }
  }

  "PrepopAnswers" should {
    val incomePrepopAnswers      = genOne(incomePrepopAnswersGen)
    val adjustmentsPrepopAnswers = fromAnnualAdjustmentsType(genOne(annualAdjustmentsTypeGen))
    val prepopStubbedUnderTest = new JourneyAnswersController(
      auth = mockAuthorisedAction,
      cc = stubControllerComponents,
      abroadAnswersService = StubAbroadAnswersService(),
      incomeService = StubIncomeAnswersService(),
      periodSummaryService = mockPeriodSummaryService,
      expensesService = mockExpensesAnswersService,
      capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
      prepopAnswersService = StubPrepopAnswersService(
        getIncomeAnswersResult = incomePrepopAnswers.asRight,
        getAdjustmentsAnswersResult = adjustmentsPrepopAnswers.asRight
      ),
      nicsAnswersService = StubNICsAnswersService(),
      profitOrLossAnswersService = StubProfitOrLossAnswersService()
    )
    s"getIncomeAnswers from downstream" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(incomePrepopAnswers).toString(),
        methodBlock = () => prepopStubbedUnderTest.getIncomePrepopAnswers(currTaxYear, businessId, nino)
      )
    }
    s"getAdjustmentsPrepopAnswers from downstream" in {
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(adjustmentsPrepopAnswers).toString(),
        methodBlock = () => prepopStubbedUnderTest.getAdjustmentsPrepopAnswers(currTaxYear, businessId, nino)
      )
    }
  }

  "ExpensesTailoring" should {
    val cases = Table(
      ("journeyAnswers", "expectedStatus"),
      (None, NO_CONTENT),
      (NoExpensesAnswers.some, OK),
      (genOne[AsOneTotalAnswers](expensesTailoringTotalAmountAnswersGen).some, OK),
      (genOne[ExpensesTailoringIndividualCategoriesAnswers](expensesTailoringIndividualCategoriesAnswersGen).some, OK)
    )

    "Get return correct status if for get tailoring answers" in
      forAll(cases) { (journeyAnswers, expectedStatus) =>
        MockExpensesAnswersService.getExpensesTailoringAnswers(journeyCtxWithNino)(EitherT.rightT(journeyAnswers))

        val controller: JourneyAnswersController = new JourneyAnswersController(
          auth = mockAuthorisedAction,
          cc = stubControllerComponents,
          abroadAnswersService = StubAbroadAnswersService(),
          incomeService = StubIncomeAnswersService(),
          periodSummaryService = mockPeriodSummaryService,
          expensesService = mockExpensesAnswersService,
          capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(),
          prepopAnswersService = StubPrepopAnswersService(),
          nicsAnswersService = StubNICsAnswersService(),
          profitOrLossAnswersService = StubProfitOrLossAnswersService()
        )
        behave like testRoute(
          request = buildRequestNoContent,
          expectedStatus = expectedStatus,
          expectedBody = journeyAnswers.fold("")(j => Json.stringify(Json.toJson(j))),
          methodBlock = () => controller.getExpensesTailoring(currTaxYear, businessId, nino)
        )
      }
    }

    s"Save ExpensesTailoringNoExpenses return a $NO_CONTENT when successful" in {
      MockExpensesAnswersService.saveTailoringAnswers(journeyCtxWithNino, NoExpensesAnswers)(EitherT.rightT(()))

      checkNoContent(underTest.saveExpensesTailoringNoExpenses(currTaxYear, businessId, nino))
    }

    s"Save ExpensesTailoringIndividualCategories return a $NO_CONTENT when successful" in {
      forAll(expensesTailoringIndividualCategoriesAnswersGen) { data =>
        MockExpensesAnswersService.saveTailoringAnswers(journeyCtxWithNino, data)(EitherT.rightT(()))

        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => underTest.saveExpensesTailoringIndividualCategories(currTaxYear, businessId, nino)
        )
      }
    }

    s"Save ExpensesTailoringTotalAmount return a $NO_CONTENT when successful" in {
      forAll(expensesTailoringTotalAmountAnswersGen) { data =>
        MockExpensesAnswersService.saveTailoringAnswers(journeyCtxWithNino, data)(EitherT.rightT(()))

        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => underTest.saveExpensesTailoringTotalAmount(currTaxYear, businessId, nino)
        )
      }
    }

  "clearExpensesSimplifiedOrNoExpensesAnswers" in {
    MockExpensesAnswersService.deleteSimplifiedExpensesAnswers(journeyCtxWithNino)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearExpensesSimplifiedOrNoExpensesAnswers(currTaxYear, businessId, nino)
    )
  }

  s"clearExpensesAndCapitalAllowancesData" in {
    MockExpensesAnswersService.clearExpensesAndCapitalAllowancesData(journeyCtxWithNino)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearExpensesAndCapitalAllowancesData(currTaxYear, businessId, nino)
    )
  }

  s"clearOfficeSuppliesExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.OfficeSupplies)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearOfficeSuppliesExpensesData(currTaxYear, businessId, nino)
    )
  }

  s"clearGoodsToSellOrUseExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.GoodsToSellOrUse)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearGoodsToSellOrUseExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearRepairsAndMaintenanceExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.RepairsAndMaintenanceCosts)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearRepairsAndMaintenanceExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearStaffCostsExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.StaffCosts)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearStaffCostsExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearWorkplaceRunningCostsExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.WorkplaceRunningCosts)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearWorkplaceRunningCostsExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearConstructionExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.Construction)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearConstructionExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearProfessionalFeesExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.ProfessionalFees)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearProfessionalFeesExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearOtherExpensesExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.ProfessionalFees)(serviceErrorT)

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearProfessionalFeesExpensesData(currTaxYear, businessId, nino)
    )

    (BAD_REQUEST, INTERNAL_SERVER_ERROR) map { status =>
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = status,
        expectedBody = "",
        methodBlock = () => {
          underTest.clearOtherExpensesData(currTaxYear, businessId, nino)
        }
      )
    }
  }

  "clearFinancialChargeExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.FinancialCharges)(serviceErrorT)

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearFinancialChargeExpensesData(currTaxYear, businessId, nino)
    )

    (BAD_REQUEST, INTERNAL_SERVER_ERROR) map { status =>
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = status,
        expectedBody = "",
        methodBlock = () => {
          underTest.clearOtherExpensesData(currTaxYear, businessId, nino)
        }
      )
    }
  }

  "clearInterestOnBankAndOtherExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.Interest)(serviceErrorT)

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearInterestOnBankAndOtherExpensesData(currTaxYear, businessId, nino)
    )

    (BAD_REQUEST, INTERNAL_SERVER_ERROR) map { status =>
      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = status,
        expectedBody = "",
        methodBlock = () => {
          underTest.clearOtherExpensesData(currTaxYear, businessId, nino)
        }
      )
    }
  }

  "clearAdvertisingOrMarketingExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.AdvertisingOrMarketing)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () =>
        underTest.clearAdvertisingOrMarketingExpensesData(currTaxYear, businessId, nino)
    )
  }

  "clearIrrecoverableDebtsExpensesData" in {
    MockExpensesAnswersService.clearExpensesData(journeyCtxWithNino, JourneyName.IrrecoverableDebts)(EitherT.rightT(()))

    behave like testRoute(
      request = buildRequestNoContent,
      expectedStatus = NO_CONTENT,
      expectedBody = "",
      methodBlock = () => underTest.clearIrrecoverableDebtsExpensesData(currTaxYear, businessId, nino)
    )
  }

  "GoodsToSellOrUse" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      MockExpensesAnswersService.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(EitherT.rightT(None))

      checkNoContent(underTest.getGoodsToSellOrUse(currTaxYear, businessId, nino))
    }

    s"Get answers and return a $OK when successful" in new GetExpensesTest[GoodsToSellOrUseAnswers] {
      override val journeyAnswers: GoodsToSellOrUseAnswers = genOne(goodsToSellOrUseAnswersGen)
      MockExpensesAnswersService.getGoodsToSellOrUseAnswers(journeyCtxWithNino)(EitherT.rightT(Some(journeyAnswers)))

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getGoodsToSellOrUse(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in {
      MockPeriodSummaryService.saveGoodsToSell(journeyCtxWithNino, goodsToSellOrUseJourneyAnswers)(EitherT.rightT(()))
      MockExpensesAnswersService.persistAnswers(
        businessId, currTaxYear, mtditid, JourneyName.GoodsToSellOrUse, taxiMinicabOrRoadHaulageDb)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(goodsToSellOrUseAnswers),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveGoodsToSellOrUse(currTaxYear, businessId, nino)
      )
    }

  }

  "WorkplaceRunningCosts" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      MockExpensesAnswersService.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(EitherT.rightT(None))

      checkNoContent(underTest.getWorkplaceRunningCosts(currTaxYear, businessId, nino))
    }

    s"Get answers and return a $OK when successful" in new GetExpensesTest[WorkplaceRunningCostsAnswers] {
      override val journeyAnswers: WorkplaceRunningCostsAnswers = genOne(workplaceRunningCostsAnswersGen)
      MockExpensesAnswersService.getWorkplaceRunningCostsAnswers(journeyCtxWithNino)(EitherT.rightT(Some(journeyAnswers)))

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getWorkplaceRunningCosts(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful " in {
        MockPeriodSummaryService.saveWorkplaceRunningCosts(journeyCtxWithNino, workplaceRunningCostsJourneyAnswers)(EitherT.rightT(()))
        MockExpensesAnswersService.persistAnswers(
          businessId,
          currTaxYear,
          mtditid,
          JourneyName.WorkplaceRunningCosts,
          workplaceRunningCostsDb
        )(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(workplaceRunningCostsAnswers),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveWorkplaceRunningCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "OfficeSupplies" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[OfficeSuppliesJourneyAnswers] {
      override val journeyAnswers: OfficeSuppliesJourneyAnswers = genOne(officeSuppliesJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.officeSuppliesParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getOfficeSupplies(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(officeSuppliesJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveOfficeSuppliesAnswers(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveOfficeSupplies(currTaxYear, businessId, nino)
      )
    }
  }

  "RepairsAndMaintenanceCosts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[RepairsAndMaintenanceCostsJourneyAnswers] {
      override val journeyAnswers: RepairsAndMaintenanceCostsJourneyAnswers = genOne(repairsAndMaintenanceCostsJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.repairsAndMaintenanceCostsParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getRepairsAndMaintenanceCosts(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(repairsAndMaintenanceCostsJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveRepairsAndMaintenance(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveRepairsAndMaintenanceCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "StaffCosts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[StaffCostsJourneyAnswers] {
      override val journeyAnswers: StaffCostsJourneyAnswers = genOne(staffCostsJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.staffCostsParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getStaffCosts(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(staffCostsJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveStaffCosts(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveStaffCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "EntertainmentCosts" should {
    s"Save return a $NO_CONTENT when successful" in forAll(entertainmentCostsJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveEntertainmentCosts(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveEntertainmentCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "AdvertisingOrMarketing" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[AdvertisingOrMarketingJourneyAnswers] {
      override val journeyAnswers: AdvertisingOrMarketingJourneyAnswers = genOne(advertisingOrMarketingJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.advertisingOrMarketingParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getAdvertisingOrMarketing(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(advertisingOrMarketingJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveAdvertisingOrMarketing(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveAdvertisingOrMarketing(currTaxYear, businessId, nino)
      )
    }
  }

  "ConstructionCosts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[ConstructionJourneyAnswers] {
      override val journeyAnswers: ConstructionJourneyAnswers = genOne(constructionJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.constructionParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getConstructionCosts(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(constructionJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveConstructionIndustrySubcontractors(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveConstructionCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "ProfessionalFees" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[ProfessionalFeesJourneyAnswers] {
      override val journeyAnswers: ProfessionalFeesJourneyAnswers = genOne(professionalFeesJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.professionalFeesParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getProfessionalFees(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(professionalFeesJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveProfessionalFees(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveProfessionalFees(currTaxYear, businessId, nino)
      )
    }
  }

  "Interest" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[InterestJourneyAnswers] {
      override val journeyAnswers: InterestJourneyAnswers = genOne(interestJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.interestParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getInterest(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(interestJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveInterests(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveInterest(currTaxYear, businessId, nino)
      )
    }
  }

  "EntertainmentCosts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[EntertainmentJourneyAnswers] {
      override val journeyAnswers: EntertainmentJourneyAnswers = genOne(entertainmentCostsJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.entertainmentCostsParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getEntertainmentCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "DepreciationCosts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[DepreciationCostsJourneyAnswers] {
      override val journeyAnswers: DepreciationCostsJourneyAnswers = genOne(depreciationJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.depreciationCostsParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getDepreciationCosts(currTaxYear, businessId, nino)
      )
    }

    s"Save return a $NO_CONTENT when successful" in forAll(depreciationJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveDepreciationCosts(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveDepreciationCosts(currTaxYear, businessId, nino)
      )
    }
  }

  "OtherExpenses" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[OtherExpensesJourneyAnswers] {
      override val journeyAnswers: OtherExpensesJourneyAnswers = genOne(otherExpensesJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.otherExpensesParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getOtherExpenses(currTaxYear, businessId, nino)
      )
    }

    s"Save answers and return a $NO_CONTENT when successful" in forAll(otherExpensesJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveOtherExpenses(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveOtherExpenses(currTaxYear, businessId, nino)
      )
    }
  }

  "FinancialCharges" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[FinancialChargesJourneyAnswers] {
      override val journeyAnswers: FinancialChargesJourneyAnswers = genOne(financialChargesJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.financialChargesParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getFinancialCharges(currTaxYear, businessId, nino)
      )
    }
    s"Save answers and return a $NO_CONTENT when successful" in forAll(financialChargesJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveFinancialCharges(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveFinancialCharges(currTaxYear, businessId, nino)
      )
    }
  }

  "IrrecoverableDebts" should {
    s"Get answers and return a $OK when successful" in new GetExpensesTest[IrrecoverableDebtsJourneyAnswers] {
      override val journeyAnswers: IrrecoverableDebtsJourneyAnswers = genOne(irrecoverableDebtsJourneyAnswersGen)
      MockExpensesAnswersService.getAnswers(journeyCtxWithNino)(
        EitherT.rightT(journeyAnswers))(Api1786ExpensesResponseParser.irrecoverableDebtsParser)

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.stringify(Json.toJson(journeyAnswers)),
        methodBlock = () => controller.getIrrecoverableDebts(currTaxYear, businessId, nino)
      )
    }

    s"Save answers and return a $NO_CONTENT when successful" in forAll(irrecoverableDebtsJourneyAnswersGen) { data =>
      MockPeriodSummaryService.saveBadDebts(journeyCtxWithNino, data)(EitherT.rightT(()))

      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveIrrecoverableDebts(currTaxYear, businessId, nino)
      )
    }
  }

  "CapitalAllowancesTailoring" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(
        underTest.getCapitalAllowancesTailoring(currTaxYear, businessId, nino)
      )
    }

    s"Get return answers" in {
      val answers = genOne(capitalAllowancesTailoringAnswersGen)
      val underTest = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(),
        incomeService = StubIncomeAnswersService(),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(getCapitalAllowancesTailoring = Some(answers).asRight),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getCapitalAllowancesTailoring(currTaxYear, businessId, nino)
      )
    }

    s"Save answers and return a $NO_CONTENT when successful" in {
      forAll(capitalAllowancesTailoringAnswersGen) { data =>
        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => underTest.saveCapitalAllowancesTailoring(currTaxYear, businessId)
        )
      }
    }
  }

  "ZeroEmissionCars" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(
        underTest.getZeroEmissionCars(currTaxYear, businessId, nino)
      )
    }

    s"Get return answers" in {
      val answers: ZeroEmissionCarsAnswers = genOne(zeroEmissionCarsAnswersGen)
      val underTest: JourneyAnswersController = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(),
        incomeService = StubIncomeAnswersService(),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(getZeroEmissionCars = Some(answers).asRight),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getZeroEmissionCars(currTaxYear, businessId, nino)
      )
    }

    s"Save answers and return a $NO_CONTENT when successful" in {
      forAll(zeroEmissionCarsAnswersGen) { data =>
        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => underTest.saveZeroEmissionCars(currTaxYear, businessId, nino)
        )
      }
    }
  }

  "ZeroEmissionGoodsVehicle" should {
    s"Get return $NO_CONTENT if there is no answers" in {
      checkNoContent(
        underTest.getZeroEmissionGoodsVehicle(currTaxYear, businessId, nino)
      )
    }

    s"Get return answers" in {
      val answers: ZeroEmissionGoodsVehicleAnswers = genOne(zeroEmissionGoodsVehicleAnswersGen)
      val underTest: JourneyAnswersController = new JourneyAnswersController(
        auth = mockAuthorisedAction,
        cc = stubControllerComponents,
        abroadAnswersService = StubAbroadAnswersService(),
        incomeService = StubIncomeAnswersService(),
        periodSummaryService = mockPeriodSummaryService,
        expensesService = mockExpensesAnswersService,
        capitalAllowancesService = StubCapitalAllowancesAnswersAnswersService(getZeroEmissionGoodsVehicleCars = Some(answers).asRight),
        prepopAnswersService = StubPrepopAnswersService(),
        nicsAnswersService = StubNICsAnswersService(),
        profitOrLossAnswersService = StubProfitOrLossAnswersService()
      )

      behave like testRoute(
        request = buildRequestNoContent,
        expectedStatus = OK,
        expectedBody = Json.toJson(answers).toString(),
        methodBlock = () => underTest.getZeroEmissionGoodsVehicle(currTaxYear, businessId, nino)
      )
    }

    s"Save answers and return a $NO_CONTENT when successful" in {
      forAll(zeroEmissionGoodsVehicleAnswersGen) { data =>
        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = NO_CONTENT,
          expectedBody = "",
          methodBlock = () => underTest.saveZeroEmissionGoodsVehicle(currTaxYear, businessId, nino)
        )
      }
    }
  }

  "BalancingAllowance" should {
    val answers: BalancingAllowanceAnswers = genOne(balancingAllowanceAnswersGen)

    def underTestWithData: JourneyAnswersController =
      mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getBalancingAllowance = Some(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getBalancingAllowance(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getBalancingAllowance(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = balancingAllowanceAnswersGen,
      actionForSave = underTest.saveBalancingAllowance(currTaxYear, businessId, nino)
    )
  }

  "BalancingCharge" should {
    val answers: BalancingChargeAnswers = genOne(balancingChargeAnswersGen)

    def underTestWithData: JourneyAnswersController =
      mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getBalancingCharge = Some(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getBalancingCharge(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getBalancingCharge(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = balancingChargeAnswersGen,
      actionForSave = underTest.saveBalancingCharge(currTaxYear, businessId, nino)
    )
  }

  "AnnualInvestmentAllowance" should {
    val answers: AnnualInvestmentAllowanceAnswers = genOne(annualInvestmentAllowanceAnswersGen)

    def underTestWithData: JourneyAnswersController =
      mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getAnnualInvestmentAllowance = Some(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getAnnualInvestmentAllowance(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getAnnualInvestmentAllowance(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = annualInvestmentAllowanceAnswersGen,
      actionForSave = underTest.saveAnnualInvestmentAllowance(currTaxYear, businessId, nino)
    )
  }

  "WritingDownAllowance" should {
    val answers: WritingDownAllowanceAnswers = genOne(writingDownAllowanceGen)

    def underTestWithData: JourneyAnswersController =
    mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getWritingDownAllowance = Some(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getWritingDownAllowance(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getWritingDownAllowance(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = writingDownAllowanceGen,
      actionForSave = underTest.saveWritingDownAllowance(currTaxYear, businessId, nino)
    )
  }

  "SpecialTaxSites" should {
    val answers: SpecialTaxSitesAnswers = genOne(specialTaxSitesGen)

    def underTestWithData: JourneyAnswersController =
      mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getSpecialTaxSites = Option(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getSpecialTaxSites(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getSpecialTaxSites(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = specialTaxSitesGen,
      actionForSave = underTest.saveSpecialTaxSites(currTaxYear, businessId, nino)
    )
  }

  "StructuresBuildings" should {
    val answers: NewStructuresBuildingsAnswers = genOne(structuresBuildingsWithYeses)

    def underTestWithData: JourneyAnswersController =
      mkJourneyAnswersController(StubCapitalAllowancesAnswersAnswersService(getStructuresBuildings = Option(answers).asRight))

    checkGetAndSave(
      actionForGetNoContent = underTest.getStructuresBuildings(currTaxYear, businessId, nino),
      actionForGet = underTestWithData.getStructuresBuildings(currTaxYear, businessId, nino),
      expectedBodyForGet = Json.toJson(answers).toString(),
      dataGen = structuresBuildingsWithYeses,
      actionForSave = underTest.saveStructuresBuildings(currTaxYear, businessId, nino)
    )
  }

  "ProfitOrLoss" should {
    "save answers and return a NO_CONTENT when successful" in {
      val answers = genOne(profitOrLossAnswersGen)

      behave like testRoute(
        request = buildRequest(answers),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => underTest.saveProfitOrLoss(currTaxYear, businessId, nino)
      )

    }
  }

  "NationalInsuranceContributions" when {
    val answerCases = List(
      NICsAnswers(Some(NICsClass2Answers(true)), None),
      NICsAnswers(None, Some(class4NoAnswer)),
      NICsAnswers(None, Some(class4SingleBusinessAnswers)),
      NICsAnswers(None, Some(class4DiverAndTrusteeMultipleBusinessesAnswers)),
      NICsAnswers(None, Some(class4DiverMultipleBusinessesAnswers)),
      NICsAnswers(None, Some(class4TrusteeMultipleBusinessesAnswers))
    )

    answerCases.foreach { answers =>
      s"NICs answers are $answers" should {
        val controllerWithData = mkUnderTest(nicsAnswersService = StubNICsAnswersService(getAnswersRes = Right(Some(answers))))

        testNoContent(underTest.getNationalInsuranceContributions(currTaxYear, businessId, nino))
        testSaveAnswers(underTest.saveNationalInsuranceContributions(currTaxYear, businessId, nino), answers)
        testGetReturnAnswers(controllerWithData.getNationalInsuranceContributions(currTaxYear, businessId, nino), Json.toJson(answers).toString())
      }
    }
  }

}
