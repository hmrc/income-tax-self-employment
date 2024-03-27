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

import cats.implicits.toTraverseOps
import controllers.actions.AuthorisedAction
import models.common.JourneyName._
import models.common._
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb}
import models.frontend.abroad.SelfEmploymentAbroadAnswers
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.{
  AnnualInvestmentAllowanceAnswers,
  AnnualInvestmentAllowanceDb,
  AnnualInvestmentAllowanceJourneyAnswers
}
import models.frontend.capitalAllowances.balancingAllowance.{BalancingAllowanceAnswers, BalancingAllowanceJourneyAnswers}
import models.frontend.capitalAllowances.electricVehicleChargePoints.{ElectricVehicleChargePointsAnswers, ElectricVehicleChargePointsJourneyAnswers}
import models.frontend.capitalAllowances.zeroEmissionCars.{ZeroEmissionCarsAnswers, ZeroEmissionCarsJourneyAnswers}
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
import models.frontend.expenses.tailoring.ExpensesTailoring.TotalAmount
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import models.frontend.income.IncomeJourneyAnswers
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.journeyAnswers._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging
import cats.implicits._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._
import models.database.capitalAllowances.{SpecialTaxSitesDb, StructuresBuildingsDb, WritingDownAllowanceDb}
import models.frontend.FrontendAnswers
import models.frontend.capitalAllowances.CapitalAllowances.StructuresAndBuildings
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers
import models.frontend.capitalAllowances.structuresBuildings.StructuresBuildingsAnswers
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class JourneyAnswersController @Inject() (auth: AuthorisedAction,
                                          cc: ControllerComponents,
                                          abroadAnswersService: AbroadAnswersService,
                                          incomeService: IncomeAnswersService,
                                          expensesService: ExpensesAnswersService,
                                          capitalAllowancesService: CapitalAllowancesAnswersService)(implicit ec: ExecutionContext)
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

  def saveExpensesTailoringNoExpenses(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    val result = expensesService.persistAnswers(businessId, taxYear, user.getMtditid, ExpensesTailoring, NoExpensesAnswers)
    handleApiUnitResultT(result)
  }

  def saveExpensesTailoringIndividualCategories(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringIndividualCategoriesAnswers](user) { value =>
      expensesService.persistAnswers(businessId, taxYear, user.getMtditid, ExpensesTailoring, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringTotalAmount(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AsOneTotalAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- expensesService.saveAnswers(ctx, value)
        _ <- expensesService.persistAnswers(businessId, taxYear, user.getMtditid, ExpensesTailoring, ExpensesCategoriesDb(TotalAmount))
      } yield NoContent
    }
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[GoodsToSellOrUseAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- expensesService.saveAnswers(ctx, GoodsToSellOrUseJourneyAnswers(value.goodsToSellOrUseAmount, value.disallowableGoodsToSellOrUseAmount))
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
        _ <- expensesService.saveAnswers(ctx, value.toApiSubmissionModel)
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
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[RepairsAndMaintenanceCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[RepairsAndMaintenanceCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[StaffCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[StaffCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[AdvertisingOrMarketingJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino))
    )
  }

  def saveAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AdvertisingOrMarketingJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[EntertainmentJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[EntertainmentJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ConstructionJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ConstructionJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ProfessionalFeesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ProfessionalFeesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[InterestJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[InterestJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[DepreciationCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[DepreciationCostsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[OtherExpensesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[OtherExpensesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[FinancialChargesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[FinancialChargesJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getIrrecoverableDebts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[IrrecoverableDebtsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveIrrecoverableDebts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[IrrecoverableDebtsJourneyAnswers](taxYear, businessId, nino) { (ctx, value) =>
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
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
    getBodyWithCtx[ZeroEmissionCarsAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- value.zecClaimAmount.traverse(claimAmount =>
          capitalAllowancesService.saveAnswers(ctx, ZeroEmissionCarsJourneyAnswers(zeroEmissionsCarAllowance = claimAmount)))
        _ <- capitalAllowancesService.persistAnswers(businessId, taxYear, user.getMtditid, ZeroEmissionCars, value.toDbModel)
      } yield NoContent
    }
  }
  def getZeroEmissionCars(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getZeroEmissionCars(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveZeroEmissionGoodsVehicle(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ZeroEmissionGoodsVehicleAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- value.zegvClaimAmount.traverse(claimAmount =>
          capitalAllowancesService.saveAnswers(ctx, ZeroEmissionCarsJourneyAnswers(zeroEmissionsCarAllowance = claimAmount)))
        _ <- capitalAllowancesService.persistAnswers(businessId, taxYear, user.getMtditid, ZeroEmissionGoodsVehicle, value.toDbModel)
      } yield NoContent
    }
  }
  def getZeroEmissionGoodsVehicle(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getZeroEmissionGoodsVehicle(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveElectricVehicleChargePoints(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ElectricVehicleChargePointsAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- value.evcpClaimAmount.traverse(claimAmount =>
          capitalAllowancesService.saveAnswers(ctx, ElectricVehicleChargePointsJourneyAnswers(electricChargePointAllowance = claimAmount)))
        _ <- capitalAllowancesService.persistAnswers(businessId, taxYear, user.getMtditid, ElectricVehicleChargePoints, value.toDbModel)
      } yield NoContent
    }
  }
  def getElectricVehicleChargePoints(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(
      capitalAllowancesService.getElectricVehicleChargePoints(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveBalancingAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[BalancingAllowanceAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- value.balancingAllowanceAmount.traverse(allowanceAmount =>
          capitalAllowancesService.saveAnswers(ctx, BalancingAllowanceJourneyAnswers(allowanceOnSales = allowanceAmount)))
        _ <- capitalAllowancesService.persistAnswers(businessId, taxYear, user.getMtditid, BalancingAllowance, value.toDbModel)
      } yield NoContent
    }
  }

  def getBalancingAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getBalancingAllowance(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveAnnualInvestmentAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AnnualInvestmentAllowanceAnswers](taxYear, businessId, nino) { (ctx, value) =>
      for {
        _ <- value.annualInvestmentAllowanceAmount.traverse(allowanceAmount =>
          capitalAllowancesService.saveAnswers(ctx, AnnualInvestmentAllowanceJourneyAnswers(annualInvestmentAllowance = allowanceAmount)))
        _ <- capitalAllowancesService.persistAnswers(
          businessId,
          taxYear,
          user.getMtditid,
          AnnualInvestmentAllowance,
          AnnualInvestmentAllowanceDb(value.annualInvestmentAllowance))
      } yield NoContent
    }
  }

  def getAnnualInvestmentAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getAnnualInvestmentAllowance(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  private def saveCapitalAllowancesAnswers[DbAnswers: Writes, A <: FrontendAnswers[DbAnswers]: Reads](
      journeyName: JourneyName,
      taxYear: TaxYear,
      businessId: BusinessId,
      nino: Nino)(implicit hc: HeaderCarrier, user: AuthorisedAction.User[AnyContent]): Future[Result] =
    getBodyWithCtx[A](taxYear, businessId, nino) { (ctx, answers) =>
      for {
        maybeCurrent <- capitalAllowancesService.getAnnualSummaries(JourneyContextWithNino(ctx.taxYear, ctx.businessId, ctx.mtditid, ctx.nino))
        maybeAnnualAllowance = maybeCurrent.flatMap(_.annualAllowances.map(_.toApi1802AnnualAllowance))
        _ <- capitalAllowancesService.saveAnnualAllowances(ctx, answers.toDownStream(maybeAnnualAllowance))
        _ <- answers.toDbModel.traverse(dbAnswers =>
          capitalAllowancesService.persistAnswers(ctx.businessId, ctx.taxYear, ctx.mtditid, journeyName, dbAnswers))
      } yield NoContent
    }

  def saveWritingDownAllowance(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    saveCapitalAllowancesAnswers[WritingDownAllowanceDb, WritingDownAllowanceAnswers](
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
    saveCapitalAllowancesAnswers[SpecialTaxSitesDb, SpecialTaxSitesAnswers](
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
    saveCapitalAllowancesAnswers[StructuresBuildingsDb, StructuresBuildingsAnswers](
      StructuresBuildings,
      taxYear,
      businessId,
      nino
    )
  }

  def getStructuresBuildings(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(capitalAllowancesService.getStructuresBuildings(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

}
