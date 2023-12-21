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
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
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

  def saveIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[IncomeJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      incomeService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getIncomeAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(incomeService.getAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveExpensesTailoringCategoryTypeAnswer(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringCategoryTypeAnswer](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringIndividualCategoriesAnswers(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringIndividualCategoriesAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringTotalAmountAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async {
    implicit user =>
      val submitTotalAmountToApi = getBody[ExpensesTailoringTotalAmountAnswers](user) { value =>
        val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
        expensesService.saveAnswers(ctx, value).map(_ => NoContent)
      }
      val storeTailoringTypeInDatabase = getBody[ExpensesTailoringCategoryTypeAnswer](user) { value =>
        expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
      }
      for {
        _      <- submitTotalAmountToApi
        result <- storeTailoringTypeInDatabase
      } yield result
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[GoodsToSellOrUseJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getGoodsToSellOrUseAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResult(expensesService.getAnswers[GoodsToSellOrUseJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[OfficeSuppliesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getOfficeSuppliesAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResult(expensesService.getAnswers[OfficeSuppliesJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[RepairsAndMaintenanceCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getRepairsAndMaintenanceCostsAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResult(
      expensesService.getAnswers[RepairsAndMaintenanceCostsJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[StaffCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def saveAdvertisingOrMarketing(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[AdvertisingOrMarketingJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def saveEntertainmentCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[EntertainmentJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getEntertainmentCostsAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResult(expensesService.getAnswers[EntertainmentJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def saveConstructionCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[ConstructionJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def getConstructionCostsAnswers(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResult(expensesService.getAnswers[ConstructionJourneyAnswers](JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }
}
