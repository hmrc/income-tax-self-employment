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

package connectors

import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import jakarta.inject.Inject
import models.common._
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.connector.common.ReliefClaim
import models.connector._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.toReliefClaimType
import uk.gov.hmrc.http._
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

class ReliefClaimsConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  private val fulcrumTaxYear = 2025

  def createReliefClaims(ctx: JourneyContextWithNino, body: CreateLossClaimRequestBody)(
      implicit hc: HeaderCarrier,
      ec: ExecutionContext): Future[ApiResponse[CreateLossClaimSuccessResponse]] = {
    implicit val reads: HttpReads[ApiResponse[CreateLossClaimSuccessResponse]] = lossClaimReads[CreateLossClaimSuccessResponse]

    val context = appConfig.mkMetadata(IFSApiName.Api1505, appConfig.api1505Url(ctx.businessId))

    post[CreateLossClaimRequestBody, ApiResponse[CreateLossClaimSuccessResponse]](httpClient, context, body)
  }

  def getAllReliefClaims(taxYear: TaxYear, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] = {
    implicit val reads: HttpReads[ApiResponse[List[ReliefClaim]]] = commonGetListReads[ReliefClaim]

    val context =
      if (taxYear.endYear >= fulcrumTaxYear) appConfig.mkMetadata(IFSApiName.Api1867, appConfig.api1867Url(taxYear, businessId))
      else appConfig.mkMetadata(IFSApiName.Api1507, appConfig.api1507Url(businessId))

    EitherT(get[ApiResponse[List[ReliefClaim]]](httpClient, context))
  }

  def updateReliefClaims(ctx: JourneyContextWithNino, oldAnswers: List[ReliefClaim], newAnswers: Seq[WhatDoYouWantToDoWithLoss])(implicit
      hc: HeaderCarrier): ApiResultT[List[WhatDoYouWantToDoWithLoss]] = {

    val createCtx: IntegrationContext           = appConfig.mkMetadata(IFSApiName.Api1505, appConfig.api1505Url(ctx.businessId))
    val deleteCtx: String => IntegrationContext = claimId => appConfig.mkMetadata(IFSApiName.Api1506, appConfig.api1506Url(ctx.businessId, claimId))

    val newAnswersAsReliefClaimType = newAnswers.map(toReliefClaimType)
    val answersToKeep               = oldAnswers.filter(claim => newAnswersAsReliefClaimType.contains(claim.reliefClaimed))
    val answersToCreate             = newAnswersAsReliefClaimType.filter(claim => oldAnswers.exists(_.reliefClaimed == claim))
    val answersToDelete             = oldAnswers.diff(answersToKeep)

    val deleteResponses: Seq[Future[HttpResponse]] =
      answersToDelete.map { answer =>
        delete(httpClient, deleteCtx(answer.claimId))
      }

    val createResponses: Seq[Future[HttpResponse]] =
      answersToCreate.map { answer =>
        post(httpClient, createCtx, answer)
      }

    val combinedAnswers: Future[Seq[HttpResponse]] = for {
      deleteSuccess <- deleteResponses.sequence
      createSuccess <- createResponses.sequence
    } yield deleteSuccess ++ createSuccess

    val mappedResponses: Future[List[WhatDoYouWantToDoWithLoss]] = combinedAnswers.map { responses =>
      responses.toList.collect {
        case response if response.status == 200 =>
          response.json.as[ReliefClaimType] match {
            case ReliefClaimType.CF   => WhatDoYouWantToDoWithLoss.CarryItForward
            case ReliefClaimType.CSGI => WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
          }
      }
    }

    EitherT.right(mappedResponses)
  }

  def deleteReliefClaims(ctx: JourneyContextWithNino, claimIds: Seq[String]): ApiResultT[Unit] =
    EitherT.right[ServiceError](Future.successful(())) // TODO: Implement as part of SASS-10370

}
