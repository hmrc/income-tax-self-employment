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

package controllers

import cats.data.EitherT
import cats.implicits._
import controllers.actions.AuthorisedAction
import models.common.JourneyName._
import models.common._
import models.database.capitalAllowances._
import models.database.expenses.TaxiMinicabOrRoadHaulageDb
import models.domain.ApiResultT
import models.error.ServiceError.InvalidNICsAnswer
import models.frontend.abroad.SelfEmploymentAbroadAnswers
import models.frontend.adjustments.ProfitOrLossJourneyAnswers
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
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
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import models.frontend.income.IncomeJourneyAnswers
import models.frontend.nics.NICsAnswers
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.journeyAnswers._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyAnswersController @Inject() (auth: AuthorisedAction,
                                          cc: ControllerComponents,
                                          prepopAnswersService: PrepopAnswersService,
                                          abroadAnswersService: AbroadAnswersService,
                                          incomeService: IncomeAnswersService,
                                          expensesService: ExpensesAnswersService,
                                          capitalAllowancesService: CapitalAllowancesAnswersService,
                                          profitOrLossAnswersService: ProfitOrLossAnswersService,
                                          nicsAnswersService: NICsAnswersService)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  // Abroad
  def getSelfEmploymentAbroad(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(abroadAnswersService.getAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveSelfEmploymentAbroad(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[SelfEmploymentAbroadAnswers](taxYear, businessId, nino) { (ctx, value) =>
      abroadAnswersService.persistAnswers(ctx, value).map(_ => NoContent)
    }
  }

  // Prepop
  def getIncomePrepopAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(prepopAnswersService.getIncomeAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def getAdjustmentsPrepopAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(prepopAnswersService.getAdjustmentsAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  // Income
  def getIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(incomeService.getAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[IncomeJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      incomeService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  // Expenses
  def getExpensesTailoring(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(expensesService.getExpensesTailoringAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveExpensesTailoringNoExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.saveTailoringAnswers(ctx, NoExpensesAnswers)
    handleApiUnitResultT(result)
  }

  def saveExpensesTailoringIndividualCategories(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async {
    implicit user =>
      getBodyWithCtx[ExpensesTailoringIndividualCategoriesAnswers](taxYear, businessId, nino) { (ctx, value) =>
        expensesService.saveTailoringAnswers(ctx, value).map(_ => NoContent)
      }
  }

  def saveExpensesTailoringTotalAmount(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AsOneTotalAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveTailoringAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def clearExpensesSimplifiedOrNoExpensesAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async {
    implicit user =>
      val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      val result = expensesService.deleteSimplifiedExpensesAnswers(ctx)
      handleApiUnitResultT(result)
  }

  def clearExpensesAndCapitalAllowancesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearExpensesAndCapitalAllowancesData(ctx)
    handleApiUnitResultT(result)
  }

  def clearOfficeSuppliesExpensesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearOfficeSuppliesExpensesData(ctx)
    handleApiUnitResultT(result)
  }

  def clearGoodsToSellOrUseExpensesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearGoodsToSellOrUseExpensesData(ctx)
    handleApiUnitResultT(result)
  }

  def clearRepairsAndMaintenanceExpensesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearRepairsAndMaintenanceExpensesData(ctx)
    handleApiUnitResultT(result)
  }

  def clearWorkplaceRunningCostsExpensesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearWorkplaceRunningCostsExpensesData(ctx)
    handleApiUnitResultT(result)
  }

  def clearConstructionExpensesData(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    val ctx    = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
    val result = expensesService.clearConstructionExpensesData(ctx)
    handleApiUnitResultT(result)
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[GoodsToSellOrUseAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- expensesService.saveGoodsToSell(
          ctx,
          GoodsToSellOrUseJourneyAnswers(value.goodsToSellOrUseAmount, value.disallowableGoodsToSellOrUseAmount))
        _ <- expensesService.persistAnswers(
          businessId,
          taxYear,
          user.getMtditid,
          GoodsToSellOrUse,
          TaxiMinicabOrRoadHaulageDb(value.taxiMinicabOrRoadHaulage))
      } yield NoContent
    }
  }

  def getGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(expensesService.getGoodsToSellOrUseAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveWorkplaceRunningCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[WorkplaceRunningCostsAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- expensesService.saveWorkplaceRunningCosts(ctx, value.toApiSubmissionModel)
        _ <- expensesService.persistAnswers(businessId, taxYear, user.getMtditid, WorkplaceRunningCosts, value.toDbModel)
      } yield NoContent
    }
  }

  def getWorkplaceRunningCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(expensesService.getWorkplaceRunningCostsAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def getOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[OfficeSuppliesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[OfficeSuppliesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveOfficeSuppliesAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[RepairsAndMaintenanceCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[RepairsAndMaintenanceCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveRepairsAndMaintenance(ctx, value).map(_ => NoContent)
    }
  }

  def getStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[StaffCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[StaffCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveStaffCosts(ctx, value).map(_ => NoContent)
    }
  }

  def getAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[AdvertisingOrMarketingJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino))
    )
  }

  def saveAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AdvertisingOrMarketingJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAdvertisingOrMarketing(ctx, value).map(_ => NoContent)
    }
  }

  def getEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[EntertainmentJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[EntertainmentJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveEntertainmentCosts(ctx, value).map(_ => NoContent)
    }
  }

  def getConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ConstructionJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ConstructionJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveConstructionIndustrySubcontractors(ctx, value).map(_ => NoContent)
    }
  }

  def getProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ProfessionalFeesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ProfessionalFeesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveProfessionalFees(ctx, value).map(_ => NoContent)
    }
  }

  def getInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[InterestJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[InterestJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveInterests(ctx, value).map(_ => NoContent)
    }
  }

  def getDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[DepreciationCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[DepreciationCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveDepreciationCosts(ctx, value).map(_ => NoContent)
    }
  }

  def getOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[OtherExpensesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[OtherExpensesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveOtherExpenses(ctx, value).map(_ => NoContent)
    }
  }

  def getFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[FinancialChargesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[FinancialChargesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveFinancialCharges(ctx, value).map(_ => NoContent)
    }
  }

  def getIrrecoverableDebts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[IrrecoverableDebtsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveIrrecoverableDebts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[IrrecoverableDebtsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveBadDebts(ctx, value).map(_ => NoContent)
    }
  }

  // Capital Allowances
  def getCapitalAllowancesTailoring(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(
      capitalAllowancesService.getCapitalAllowancesTailoring(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveCapitalAllowancesTailoring(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[CapitalAllowancesTailoringAnswers](user) { value =>
      capitalAllowancesService.persistAnswers(businessId, taxYear, user.getMtditid, CapitalAllowancesTailoring, value).map(_ => NoContent)
    }
  }

  def saveZeroEmissionCars(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[ZeroEmissionCarsDb, ZeroEmissionCarsAnswers](
      ZeroEmissionCars,
      taxYear,
      businessId,
      nino
    )
  }

  def getZeroEmissionCars(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getZeroEmissionCars(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveZeroEmissionGoodsVehicle(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[ZeroEmissionGoodsVehicleDb, ZeroEmissionGoodsVehicleAnswers](
      ZeroEmissionGoodsVehicle,
      taxYear,
      businessId,
      nino
    )
  }

  def getZeroEmissionGoodsVehicle(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getZeroEmissionGoodsVehicle(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveBalancingAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[BalancingAllowanceDb, BalancingAllowanceAnswers](
      BalancingAllowance,
      taxYear,
      businessId,
      nino
    )
  }

  def getBalancingAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getBalancingAllowance(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveBalancingCharge(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[BalancingChargeDb, BalancingChargeAnswers](
      BalancingCharge,
      taxYear,
      businessId,
      nino
    )
  }

  def getBalancingCharge(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getBalancingCharge(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveAnnualInvestmentAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[AnnualInvestmentAllowanceDb, AnnualInvestmentAllowanceAnswers](
      AnnualInvestmentAllowance,
      taxYear,
      businessId,
      nino
    )
  }

  def getAnnualInvestmentAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getAnnualInvestmentAllowance(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveWritingDownAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[WritingDownAllowanceDb, WritingDownAllowanceAnswers](
      WritingDownAllowance,
      taxYear,
      businessId,
      nino
    )
  }

  def getWritingDownAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getWritingDownAllowance(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveSpecialTaxSites(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[SpecialTaxSitesDb, SpecialTaxSitesAnswers](
      SpecialTaxSites,
      taxYear,
      businessId,
      nino
    )
  }

  def getSpecialTaxSites(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getSpecialTaxSites(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveStructuresBuildings(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    capitalAllowancesService.saveAnswers[NewStructuresBuildingsDb, NewStructuresBuildingsAnswers](
      StructuresBuildings,
      taxYear,
      businessId,
      nino
    )
  }

  def getStructuresBuildings(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getStructuresBuildings(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveProfitOrLoss(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ProfitOrLossJourneyAnswers](taxYear, businessId, nino) { (ctx, answers) =>
      profitOrLossAnswersService.saveProfitOrLoss(ctx, answers).map(_ => NoContent)
    }
  }

  def getProfitOrLoss(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(profitOrLossAnswersService.getProfitOrLoss(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveNationalInsuranceContributions(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[NICsAnswers](taxYear, businessId, nino) { (ctx, answers) =>
      val result: ApiResultT[Unit] = (answers.class2Answers, answers.class4Answers) match {
        case (Some(class2Answers), None) => nicsAnswersService.saveClass2Answers(ctx, class2Answers)
        case (None, Some(class4Answers)) if class4Answers.userHasSingleBusinessExemption =>
          nicsAnswersService.saveClass4SingleBusiness(ctx, class4Answers)
        case (None, Some(class4Answers)) => nicsAnswersService.saveClass4MultipleBusinessOrNoExemptionJourneys(ctx, class4Answers)
        case _                           => EitherT.leftT[Future, Unit](InvalidNICsAnswer(answers))
      }
      result.map(_ => NoContent)
    }
  }

  def getNationalInsuranceContributions(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(nicsAnswersService.getAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }
}
