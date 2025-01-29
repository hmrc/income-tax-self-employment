/*
 * Copyright 2024 HM Revenue & Customs
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
import models.common.{BusinessId, JourneyContextWithNino, TaxYear}
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.connector.common.ReliefClaim
import models.connector.{ApiResponse, IFSApiName, IntegrationContext, ReliefClaimType, commonGetListReads, commonGetReads, lossClaimReads}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.toReliefClaimType
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

class ReliefClaimsConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  private val fulcrumTaxYear = 2025

  def createReliefClaims(context: IntegrationContext, body: CreateLossClaimRequestBody)(implicit
      reads: HttpReads[ApiResponse[CreateLossClaimSuccessResponse]],
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[ApiResponse[CreateLossClaimSuccessResponse]] =
    post[CreateLossClaimRequestBody, ApiResponse[CreateLossClaimSuccessResponse]](httpClient, context, body)

  def getAllReliefClaims(taxYear: TaxYear, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] = {
    implicit val reads: HttpReads[ApiResponse[List[ReliefClaim]]] = commonGetListReads[ReliefClaim]

    val context =
      if (taxYear.endYear >= fulcrumTaxYear) appConfig.mkMetadata(IFSApiName.Api1867, appConfig.api1867Url(taxYear, businessId))
      else appConfig.mkMetadata(IFSApiName.Api1507, appConfig.api1507Url(businessId))

    EitherT(get[ApiResponse[List[ReliefClaim]]](httpClient, context))
  }

//  def getReliefClaim(businessId: BusinessId, claimId: String)
//                    (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[ReliefClaim]] = {
//    implicit val reads: HttpReads[ApiResponse[Option[ReliefClaim]]] = commonGetReads[ReliefClaim]
//    val context = appConfig.mkMetadata(IFSApiName.Api1508, appConfig.api1508Url(businessId, claimId))
//
//    EitherT(get[ApiResponse[Option[ReliefClaim]]](httpClient, context))
//  }

  def updateReliefClaims(ctx: JourneyContextWithNino, oldAnswers: List[ReliefClaim], newAnswers: Seq[WhatDoYouWantToDoWithLoss])(implicit
      hc: HeaderCarrier): ApiResultT[List[WhatDoYouWantToDoWithLoss]] = {

    val createCtx: IntegrationContext           = appConfig.mkMetadata(IFSApiName.Api1505, appConfig.api1505Url(ctx.businessId))
    val deleteCtx: String => IntegrationContext = claimId => appConfig.mkMetadata(IFSApiName.Api1506, appConfig.api1506Url(ctx.businessId, claimId))

    val newAnswersAsReliefClaimType = newAnswers.map(toReliefClaimType)
    val answersToKeep               = oldAnswers.filter(claim => newAnswersAsReliefClaimType.contains(claim.reliefClaimed))
    val answersToCreate             = newAnswersAsReliefClaimType.filter(claim => oldAnswers.exists(_.reliefClaimed == claim))
    val answersToDelete             = oldAnswers.diff(answersToKeep)

    val deleteResponses = Future.sequence {
      answersToDelete.map { answer =>
        delete(httpClient, deleteCtx(answer.claimId))
      }
    }

    for {
      createSuccess <- createLossClaims(ctx, answersToCreate.map(WhatDoYouWantToDoWithLoss.fromReliefClaimType))
      deleteSuccess <- deleteReliefClaims(ctx, answersToDelete.map(_.claimId))
    } yield EitherT(overallResponses)
  }

  def deleteReliefClaims(ctx: JourneyContextWithNino, claimIds: Seq[String]): ApiResultT[Unit] =
    EitherT.right[ServiceError](Future.successful(())) // TODO: Implement as part of SASS-10370

}
