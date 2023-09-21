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
import models.mdtp.JourneyState
import models.mdtp.JourneyState.JourneyStateData
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.SessionRepository
import utils.PagerDutyHelper.WithRecovery
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.PagerDutyHelper.PagerDutyKeys.FAILED_TO_GET_JOURNEY_STATE_DATA

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyStateController @Inject()(sessionRepository: SessionRepository,
                                       auth: AuthorisedAction,
                                       cc: ControllerComponents
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getJourneyState(nino: String, taxYear: Int, journey: String): Action[AnyContent] = auth.async { _ =>
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to find journey state data."
    sessionRepository.get(nino, taxYear, journey)
      .recoverWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
      .map {
        case Right(Some(model)) => Ok(Json.toJson(model.journeyStateData.completed))
        case Right(None) => NoContent
        case Left(serverError) => InternalServerError(Json.toJson(serverError.msg))
      }
  }

  def putJourneyState(nino: String, taxYear: Int, journey: String, completed: Boolean): Action[AnyContent] = auth.async { _ =>
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to save journey state data."
    sessionRepository.set(JourneyState(journeyStateData = JourneyStateData(nino, taxYear, journey, completed)))
      .recoverWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
      .map {
        case Right(_) => NoContent
        case Left(serverError) => InternalServerError(Json.toJson(serverError.msg))
      }
  }
}
