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

package controllers.journeyAnswers.expenses.goodsToSellOrUse

import cats.data.EitherT
import controllers.actions.AuthorisedAction
import models.common.TaxYear.{endDate, startDate}
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType}
import models.error.DownstreamError
import models.error.DownstreamError.{MultipleDownstreamErrors, SingleDownstreamError}
import models.frontend.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.journeyAnswers.expenses.goodsToSellOrUse.SelfEmploymentBusinessService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsToSellOrUseController @Inject() (service: SelfEmploymentBusinessService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def handleRequest(taxYear: TaxYear, businessId: BusinessId, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    user.request.body.asJson match {
      case Some(json) =>
        val answers    = json.as[GoodsToSellOrUseJourneyAnswers]
        val financials = Json.toJson(answers).as[FinancialsType]

        val body = CreateSEPeriodSummaryRequestBody(startDate(taxYear), endDate(taxYear), Some(financials))
        val data = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, body)

        (for {
          _ <- EitherT(service.createSEPeriodSummary(data))
        } yield NoContent).leftMap(resultFromError).merge

      case None =>
        logger.warn("[GoodsToSellOrUseController] [createSEPeriodSummary] Expected a JSON payload.")
        Future.successful(BadRequest)
    }
  }

  private def resultFromError(error: DownstreamError): Result = {
    val domainError = error match {
      case sde: SingleDownstreamError    => Json.toJson(sde.toDomain)
      case mde: MultipleDownstreamErrors => Json.toJson(mde.toDomain)
    }
    Status(error.status)(Json.toJson(domainError))
  }

}
