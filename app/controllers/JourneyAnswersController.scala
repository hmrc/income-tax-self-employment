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
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.{ExpensesTailoringIndividualCategoriesAnswers, ExpensesTailoringNoExpensesAnswers}
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

  def saveExpensesTailoringNoExpensesAnswers(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringNoExpensesAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringIndividualCategoriesAnswers(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringIndividualCategoriesAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[GoodsToSellOrUseJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def saveOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[OfficeSuppliesJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def saveRepairsAndMaintenanceCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[RepairsAndMaintenanceCostsJourneyAnswers](user) { value =>
      val ctx = JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)
      expensesService.saveAnswers(ctx, value).map(_ => NoContent)
    }
  }

  def saveStaffCosts(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[StaffCostsJourneyAnswers](user) { value =>
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
}
