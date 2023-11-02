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

package controllers.journeyAnswers

import models.database.JourneyAnswers
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import services.journeyAnswers.SetJourneyAnswersService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SetJourneyAnswersController @Inject()(cc: ControllerComponents, service: SetJourneyAnswersService)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def handleRequest(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[JourneyAnswers] match {
      case JsSuccess(value, _) => service.setJourneyAnswers(value).map(_ => NoContent)
      case JsError(_) =>
        Future.successful(
          BadRequest(Json.obj("code" -> "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "reason" -> "An empty or non-matching body was submitted")))
    }
  }

}
