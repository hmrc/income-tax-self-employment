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
import jakarta.inject.{Inject, Singleton}
import models.common._
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.answers.{AnswerService, InvalidSection, JourneyAnswerValidationService, ValidSection}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnswerController @Inject() (answerService: AnswerService,
                                  sectionValidationService: JourneyAnswerValidationService,
                                  auth: AuthorisedAction,
                                  cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  /** GET /answers/users/:nino/businesses/:business/section/:section
    * ===Purpose===
    * Retrieve a named "section" of task list answers for the given business
    * ===Detail===
    *
    * @param nino
    *   Taxpayer's National Insurance Number
    * @param businessId
    *   the ID of the "income source", aka business
    * @param journey
    *   the section to retrieve
    *
    * @return
    *   OK - Json representation of the named section<br> NOT_FOUND - The named section doesn't exist in the named section
    */
  def getJourneyAnswers(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName): Action[AnyContent] = auth.async {
    implicit request =>
      answerService.getJourneyAnswers[JsValue](mkContext(nino, businessId, taxYear), journey).flatMap {
        case Some(sectionData) =>
          sectionValidationService.validate(journey, sectionData).map {
            case Right(ValidSection(value)) =>
              Ok(value)
            case Left(response @ InvalidSection(_)) =>
              logger.error(s"Answers for journey '${journey.entryName}'" +
                s" for businessId '$businessId' failed to validate the following keys ${response.asString}")
              InternalServerError(s"Answers for journey '${journey.entryName}'" +
                s" for businessId '$businessId' failed to validate the following keys ${response.asString}")
          }
        case _ =>
          Future.successful(NotFound)
      }

  }

  /** PUT /answers/users/:nino/businesses/:business/section/:section
    * ===Purpose===
    * Replaces the named section with the given JSON
    * ===Detail===
    *
    * @param nino
    *   Taxpayer's National Insurance Number
    * @param businessId
    *   the ID of the "income source", aka business
    * @param journey
    *   the section to replace
    *
    * @return
    *   OK - Json representation of the updated section<br> BAD_REQUEST - The request json was not valid for the given section
    */
  def replaceJourneyAnswers(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName): Action[AnyContent] = auth.async {
    implicit request =>
      request.body.asJson match {
        case Some(json) =>
          sectionValidationService.validate(journey, json).flatMap {
            case Right(ValidSection(validatedJson)) =>
              answerService.upsertJourneyAnswers(mkContext(nino, businessId, taxYear), journey, validatedJson).map {
                case Some(updatedSectionJson) =>
                  Ok(updatedSectionJson)
                case _ =>
                  logger.error(s"Unable to upsert answers for journey '${journey.entryName}' for businessId '$businessId'")
                  InternalServerError(s"Unable to upsert answers for journey '${journey.entryName}' for buisnessId '$businessId'")
              }
            case Left(response @ InvalidSection(_)) =>
              logger.error(s"Missing keys: ${response.asString} when upserting answers for journey '${journey.entryName}' for businessId $businessId")
              Future.successful(BadRequest(response.asString))
          }
        case _ =>
          Future.successful(BadRequest(s"Empty request body when updating answers for journey '${journey.entryName}'"))
      }
  }

  /** DELETE /answers/users/:nino/businesses/:business/section/:section
    * ===Purpose===
    * Delete the named section
    * ===Detail===
    *
    * @param nino
    *   Taxpayer's National Insurance Number
    * @param businessId
    *   the ID of the "income source", aka business
    * @param journey
    *   the section to delete
    *
    * @return
    *   NO_CONTENT
    */
  def deleteJourneyAnswers(nino: Nino, businessId: BusinessId, taxYear: TaxYear, journey: JourneyName): Action[AnyContent] = auth.async {
    implicit request =>
      answerService.deleteJourneyAnswers(mkContext(nino, businessId, taxYear), journey).map { _ =>
        NoContent
      }
  }

  private def mkContext(nino: Nino, businessId: BusinessId, taxYear: TaxYear)(implicit request: User[_]): JourneyContextWithNino =
    JourneyContextWithNino(taxYear, businessId, request.getMtditid, nino)

}
