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

import controllers.actions.AuthorisedAction
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.TaskListService
import services.journeyAnswers.JourneyStatusService
import jakarta.inject.{Inject, Singleton}
import models.common.{Mtditid, Nino, TaxYear}
import models.commonTaskList.TaskListModel
import models.frontend.TaskList
import play.api.libs.json.Json
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import scala.concurrent.ExecutionContext

@Singleton
class TaskListController @Inject() (journeyStatusService: JourneyStatusService,
                                    taskListService: TaskListService,
                                    auth: AuthorisedAction,
                                    cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getTaskList(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    for {
      legacyTaskList <- journeyStatusService.getLegacyTaskList(taxYear, Mtditid(user.mtditid), nino).value.map {
        case Right(taskList) => taskList
        case Left(_)         => TaskList(Nil, None)
      }
      commonTaskList <- taskListService.buildTaskList(legacyTaskList, taxYear, user.getMtditid)
    } yield commonTaskList match {
      case TaskListModel(Nil) =>
        logger.error(s"Failed to build task list for MTDITID '${user.mtditid}'. Possible issue with Business Details API.")
        InternalServerError
      case taskListModel =>
        Ok(Json.toJson(taskListModel))
    }
  }

}
