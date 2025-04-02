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

package controllers.answers

import controllers.actions.AuthorisedAction
import controllers.actions.AuthorisedAction.User
import jakarta.inject.Inject
import models.common.{BusinessId, CollectionOptions, JourneyContextWithNino, JourneyName, Nino, TaxYear}
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.answers.{AnswerService, InvalidSection, JourneyAnswerValidationService, ValidSection}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

class CollectionAnswerController @Inject() (answerService: AnswerService,
                                            sectionValidationService: JourneyAnswerValidationService,
                                            auth: AuthorisedAction,
                                            cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getCollectionAnswer(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName, index: Int): Action[AnyContent] =
    auth.async { implicit request =>
      ifIndexWithinRange(journey, index) {
        answerService.getCollectionAnswer[JsValue](mkContext(nino, businessId, taxYear), journey, index).flatMap {
          case Some(sectionData) =>
            sectionValidationService.validateIndex(journey, sectionData).map {
              case Right(ValidSection(validatedJson)) =>
                Ok(validatedJson)
              case Left(response @ InvalidSection(_)) =>
                logger.error(s"Answers for journey '${journey.entryName}'" +
                  s" for businessId '$businessId' failed to validate the following keys ${response.asString}")
                InternalServerError(s"Answers for journey '${journey.entryName}'" +
                  s" for businessId '$businessId' failed to validate the following keys ${response.asString}")
            }
          case None =>
            Future.successful(NotFound)
        }
      }
    }

  def replaceCollectionAnswer(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName, index: Int): Action[AnyContent] =
    auth.async { implicit request =>
      ifIndexWithinRange(journey, index) {
        request.body.asJson match {
          case Some(json) if journey.isCollectionJourney =>
            sectionValidationService.validateIndex(journey, json).flatMap {
              case Right(ValidSection(validatedJson)) =>
                answerService.upsertCollectionAnswer(mkContext(nino, businessId, taxYear), journey, validatedJson, index).map {
                  case Some(_) =>
                    Ok(validatedJson)
                  case _ =>
                    logger.error(s"Unable to upsert index $index for journey '${journey.entryName}' for businessId '$businessId'")
                    InternalServerError(s"Unable to upsert index $index for journey '${journey.entryName}' for businessId '$businessId'")
                }
              case Left(response @ InvalidSection(_)) =>
                logger.error(
                  s"Invalid or missing keys: ${response.asString} when upserting index $index for journey '${journey.entryName}' for businessId $businessId")
                Future.successful(BadRequest(response.asString))
            }
          case _ =>
            Future.successful(
              BadRequest(s"Empty request body when updating index $index for journey '${journey.entryName}' for businessId $businessId"))
        }
      }
    }

  def deleteCollectionAnswer(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName, index: Int): Action[AnyContent] =
    auth.async { implicit request =>
      ifIndexWithinRange(journey, index) {
        answerService
          .deleteCollectionAnswer(mkContext(nino, businessId, taxYear), journey, index)
          .map(_ => NoContent)
      }
    }

  private def ifIndexWithinRange(journey: JourneyName, index: Int)(function: => Future[Result]): Future[Result] =
    journey.collectionOptions match {
      case Some(CollectionOptions(minItems, Some(maxItems))) =>
        if (index > maxItems || index < minItems) {
          Future.successful(BadRequest(s"Index $index out of bounds for journey ${journey.entryName}"))
        } else {
          function
        }
      case Some(CollectionOptions(minItems, _)) =>
        if (index < minItems) {
          Future.successful(BadRequest(s"Index out of bounds for journey '${journey.entryName}'"))
        } else {
          function
        }
      case _ => Future.successful(BadRequest(s"Journey '${journey.entryName}' is not a collection"))
    }

  private def mkContext(nino: Nino, businessId: BusinessId, taxYear: TaxYear)(implicit request: User[_]): JourneyContextWithNino =
    JourneyContextWithNino(taxYear, businessId, request.getMtditid, nino)

}
