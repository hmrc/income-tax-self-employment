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
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsArray, Json, Writes}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.PagerDutyHelper.PagerDutyKeys.FAILED_TO_GET_JOURNEY_STATE_DATA
import utils.PagerDutyHelper.WithRecovery

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyStateController @Inject()(sessionRepository: SessionRepository,
                                       auth: AuthorisedAction,
                                       cc: ControllerComponents
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getJourneyState(businessId: String, journey: String, taxYear: Int): Action[AnyContent] = auth.async { request =>
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to find journey state data."
    sessionRepository.get(businessId, journey, taxYear)
      .recoverWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
      .map {
        case Right(Some(model)) => Ok(Json.toJson(model.journeyStateData.completed))
        case Right(None) => NoContent
        case Left(serverError) => InternalServerError(Json.toJson(serverError.msg))
      }
  }

  def getJourneyState(businessId: String, taxYear: Int): Action[AnyContent] = auth.async { request =>
    
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][get] Failed to find journey state data."
    sessionRepository.get(businessId, taxYear)
      .recoverWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
      .map {
        case Right(Nil) => NoContent
        case Right(Seq(model)) => Ok(Json.toJson(Seq(model.journeyStateData.journey, model.journeyStateData.completed)))
        case Left(serverError) => InternalServerError(Json.toJson(serverError.msg))
      }
  }

  def putJourneyState(businessId: String, journey: String, taxYear: Int, completed: Boolean): Action[AnyContent] = auth.async { _ =>
    lazy val pagerMsg = "[Self-Employment BE SessionRepository][set] Failed to save journey state data."
    
    val journeyStateData = JourneyStateData(businessId, journey, taxYear, completed)
    sessionRepository.get(businessId, journey, taxYear).flatMap({ optJourneyState =>
      val (journeystate, isCreated) =
        optJourneyState.fold((JourneyState(journeyStateData = journeyStateData), true))(js => (JourneyState(js.id, journeyStateData), false))
      sessionRepository.set(journeystate).map((_, isCreated))
    })
      .recoverWithPagerDutyLog(FAILED_TO_GET_JOURNEY_STATE_DATA, pagerMsg)
      .map {
        case Right((_, true)) => Created
        case Right((_, false)) => NoContent
        case Left(serverError) => InternalServerError(Json.toJson(serverError.msg))
      }
  }
}

object JourneyStateController {
  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A, B)] =
    (tuple: Tuple2[A, B]) => JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
}
