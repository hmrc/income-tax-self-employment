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
import models.domain.JourneyNameAndStatus
import models.frontend.JourneyStatusData
import play.api.libs.json.Format.GenericFormat
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.JourneyStateRepository
import services.BusinessService
import services.journeyAnswers.JourneyStatusService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyStateController @Inject() (journeyStateRepository: JourneyStateRepository,
                                        journeyStatusService: JourneyStatusService,
                                        businessService: BusinessService,
                                        auth: AuthorisedAction,
                                        cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getJourneyState(businessId: BusinessId, journey: JourneyName, taxYear: TaxYear): Action[AnyContent] = auth.async { request =>
    val result = journeyStatusService
      .get(JourneyContext(taxYear, businessId, request.getMtditid, journey))
      .map(JourneyNameAndStatus(journey, _))

    handleApiResultT(result)
  }

  // TODO Rename
  def getJourneyStateSeq(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit request =>
    val result = journeyStatusService.getTaskList(taxYear, request.getMtditid, nino)
    handleApiResultT(result)
//    businessService
//      .getBusinessJourneyStates(nino, taxYear)
//      .map {
//        case Left(serviceError) => InternalServerError(Json.toJson(serviceError))
//        case Right(Seq())       => NoContent
//        case Right(res)         => Ok(Json.toJson(res))
//      }
  }

  def setJourneyStatus(businessId: BusinessId, journey: JourneyName, taxYear: TaxYear): Action[AnyContent] = auth.async { implicit request =>
    getBody[JourneyStatusData](request) { statusData =>
      journeyStatusService.set(JourneyContext(taxYear, businessId, request.getMtditid, journey), statusData.status).map(_ => NoContent)
    }
  }
}
