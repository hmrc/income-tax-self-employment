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

import controllers.actions.AuthorisedAction
import models.common._
import models.database.expenses.ExpensesCategoriesDb
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import models.frontend.expenses.tailoring._
import models.frontend.income.IncomeJourneyAnswers
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.journeyAnswers._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyAnswersController @Inject() (auth: AuthorisedAction,
                                          cc: ControllerComponents,
                                          incomeService: IncomeAnswersService,
                                          expensesService: ExpensesAnswersService)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(incomeService.getAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[IncomeJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      incomeService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getExpensesTailoring(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(expensesService.getExpensesTailoringAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveExpensesTailoringNoExpenses(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    val result = expensesService.persistAnswers(businessId, taxYear, user.getMtditid, NoExpensesAnswers)
    handleApiUnitResultT(result)
  }

  def saveExpensesTailoringIndividualCategories(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringIndividualCategoriesAnswers](user) { value =>
      expensesService.persistAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringTotalAmount(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[AsOneTotalAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      for {
        _ <- expensesService.saveAnswers(ctx, value)
        _ <- expensesService.persistAnswers(businessId, taxYear, user.getMtditid, ExpensesCategoriesDb(ExpensesTailoring.TotalAmount))
      } yield NoContent
    }
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[GoodsToSellOrUseJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[GoodsToSellOrUseJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def getOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[OfficeSuppliesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[OfficeSuppliesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[RepairsAndMaintenanceCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[RepairsAndMaintenanceCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[StaffCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[StaffCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(
      expensesService.getAnswers[AdvertisingOrMarketingJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino))
    )
  }

  def saveAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[AdvertisingOrMarketingJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[EntertainmentJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[EntertainmentJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ConstructionJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[ConstructionJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[ProfessionalFeesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveProfessionalFees(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[ProfessionalFeesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[InterestJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveInterest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[InterestJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[DepreciationCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveDepreciationCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[DepreciationCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[OtherExpensesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOtherExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[OtherExpensesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(expensesService.getAnswers[FinancialChargesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveFinancialCharges(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[FinancialChargesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

}
