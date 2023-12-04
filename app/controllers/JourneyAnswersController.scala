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
import models.common.{BusinessId, Nino, TaxYear}
import models.frontend.expenses.ExpensesTailoringAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.income.IncomeJourneyAnswers
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.journeyAnswers.{ExpensesAnswersService, IncomeAnswersService}
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

  def saveIncomeAnswers(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[IncomeJourneyAnswers](user) { value =>
      incomeService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveExpensesTailoringAnswers(taxYear: TaxYear, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, value).map(_ => NoContent)
    }
  }

  def saveGoodsToSellOrUse(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[GoodsToSellOrUseJourneyAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, nino, value).map(_ => NoContent)
    }
  }

  def saveOfficeSupplies(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBody[OfficeSuppliesJourneyAnswers](user) { value =>
      expensesService.saveAnswers(businessId, taxYear, user.getMtditid, nino, value).map(_ => NoContent)
    }
  }
}
