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

import cats.implicits._
import controllers.actions.AuthorisedAction
import models.common.{BusinessId, JourneyContextWithNino, Nino, TaxYear}
import models.database.expenses.travel.TravelExpensesDb
import play.api.mvc._
import services.journeyAnswers.expenses.PeriodSummaryService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext


@Singleton
class TravelExpensesController @Inject() (auth: AuthorisedAction,
                                          periodSummaryService: PeriodSummaryService,
                                          cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def getTravelExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(periodSummaryService.getTravelExpensesAnswers(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def updateTravelExpenses(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[TravelExpensesDb](taxYear, businessId, nino) { (ctx, value) =>
      periodSummaryService.saveTravelExpenses(ctx, value).map(_ => NoContent)
    }
  }

}