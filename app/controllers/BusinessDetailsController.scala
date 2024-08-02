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
import models.common.{BusinessId, Nino, TaxYear}
import models.error.DownstreamError
import models.error.DownstreamError.{MultipleDownstreamErrors, SingleDownstreamError}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.BusinessService
import services.BusinessService.GetBusinessResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessDetailsController @Inject() (businessService: BusinessService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getBusinesses(nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    businessService.getBusinesses(nino) map businessDataResponse
  }

  def getBusiness(nino: Nino, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    businessService.getBusiness(nino, businessId) map businessDataResponse
  }

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId): Action[AnyContent] = auth.async { implicit user =>
    businessService.getBusinessIncomeSourcesSummary(taxYear, nino, businessId) map {
      case Right(model)     => Ok(Json.toJson(model))
      case Left(errorModel) => handleErrorModel(errorModel)
    }
  }

  def getUserDateOfBirth(nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    businessService.getUserDateOfBirth(nino) map {
      case Right(model)     => Ok(Json.toJson(model))
      case Left(errorModel) => handleErrorModel(errorModel)
    }
  }

  private def businessDataResponse(dataResponse: GetBusinessResponse): Result =
    dataResponse match {
      case Right(model)     => Ok(Json.toJson(model))
      case Left(errorModel) => handleErrorModel(errorModel)
    }

  private def handleErrorModel(errorModel: DownstreamError): Result =
    errorModel match {
      case sde: SingleDownstreamError => Status(errorModel.status)(Json.toJson(sde.toDomain))
      case _                          => Status(errorModel.status)(Json.toJson(errorModel.asInstanceOf[MultipleDownstreamErrors].toDomain))
    }

}
