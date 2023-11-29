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
import models.common.{BusinessId, JourneyName, Mtditid, TaxYear}
import models.frontend.ExpensesTailoringJourneyAnswers
import play.api.Logger
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsObject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.JourneyService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyAnswersController @Inject()(auth: AuthorisedAction, cc: ControllerComponents, service: JourneyService)(implicit ec: ExecutionContext)
    extends BackendController(cc) {
  private implicit val logger: Logger = Logger(this.getClass)

  def getAnswers(taxYear: TaxYear, businessId: BusinessId, journey: JourneyName): Action[AnyContent] = auth.async { implicit user =>
    handleResultT(
      service
        .getAnswers[JsObject](businessId, taxYear, Mtditid(user.mtditid), journey)
        .map(Ok(_))
    )
  }

  def saveAnswers(taxYear: TaxYear, businessId: BusinessId, journey: JourneyName): Action[AnyContent] = auth.async { implicit user =>
    getBody[ExpensesTailoringJourneyAnswers](user) { value =>
      service.setAnswers(businessId, taxYear, Mtditid(user.mtditid), journey, value).map(_ => NoContent)
    }
  }

}
