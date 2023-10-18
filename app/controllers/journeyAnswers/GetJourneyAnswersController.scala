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

import play.api.libs.json.Json
import play.api.mvc._
import services.journeyAnswers.GetJourneyAnswersResult.{NoJourneyAnswersFound, JourneyAnswersFound}
import services.journeyAnswers.GetJourneyAnswersService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class GetJourneyAnswersController @Inject()(cc: ControllerComponents, service: GetJourneyAnswersService)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def handleRequest(id: String): Action[AnyContent] = Action.async { _ =>
    service.getJourneyAnswers(id).map {
      case JourneyAnswersFound(answers) => Ok(Json.toJson(answers))
      case NoJourneyAnswersFound        => NotFound(Json.obj("code" -> "NOT_FOUND", "reason" -> s"No journey answers found for id: $id"))
    }
  }

}
