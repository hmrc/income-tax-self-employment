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
import models.common.{BusinessId, JourneyContextWithNino, Nino, TaxYear}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.BusinessService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessDetailsController @Inject() (businessService: BusinessService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getBusinesses(nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    businessService.getBusinesses(user.getMtditid, nino).value.map {
      case Right(Nil) => NotFound
      case Right(listOfBusinesses) => Ok(Json.toJson(listOfBusinesses))
      case Left(error) =>
        logger.error(s"Error encountered when retrieving businesses: ${error.errorMessage}")
        InternalServerError(error.errorMessage)
    }
  }

  def getBusiness(nino: Nino, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.getBusiness(businessId, user.getMtditid, nino))
  }

  def getUserDateOfBirth(nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.getUserDateOfBirth(nino))
  }

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.getAllBusinessIncomeSourcesSummaries(taxYear, user.getMtditid, nino))
  }

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.getBusinessIncomeSourcesSummary(taxYear, nino, businessId))
  }

  def getNetBusinessProfitOrLossValues(taxYear: TaxYear, nino: Nino, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.getNetBusinessProfitOrLossValues(JourneyContextWithNino(taxYear, businessId, user.getMtditid, nino)))
  }

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(businessService.hasOtherIncomeSources(taxYear, nino))
  }
}
