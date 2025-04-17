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
import config.AppConfig
import jakarta.inject.Inject
import models.common._
import models.connector.api_1505.{CreateLossClaimRequestBody, ClaimId}
import models.connector.common.ReliefClaim
import models.connector._
import models.domain.ApiResultT
import uk.gov.hmrc.http._
import utils.Logging

import scala.concurrent.ExecutionContext

class ReliefClaimsConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  private val fulcrumTaxYear = 2025

  def getAllReliefClaims(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] = {
    val taxYear                                                   = ctx.taxYear
    val nino                                                      = ctx.nino
    implicit val reads: HttpReads[ApiResponse[List[ReliefClaim]]] = commonGetListReads[ReliefClaim]

    val context =
      if (taxYear.endYear >= fulcrumTaxYear) appConfig.mkMetadata(IFSApiName.Api1867, appConfig.api1867Url(taxYear, nino))
      else appConfig.mkMetadata(IFSApiName.Api1507, appConfig.api1507Url(nino))

    EitherT(get[ApiResponse[List[ReliefClaim]]](httpClient, context))
  }

  def createReliefClaim(ctx: JourneyContextWithNino, answer: ReliefClaimType)(implicit hc: HeaderCarrier): ApiResultT[ClaimId] = {
    implicit val reads: HttpReads[ApiResponse[ClaimId]] = lossClaimReads[ClaimId]
    val context                                         = appConfig.mkMetadata(IFSApiName.Api1505, appConfig.api1505Url(ctx.nino, ctx.taxYear))

    val body = CreateLossClaimRequestBody(
      incomeSourceId = ctx.businessId.value,
      reliefClaimed = answer.toString,
      taxYear = ctx.taxYear.endYear.toString
    )
    EitherT(post[CreateLossClaimRequestBody, ApiResponse[ClaimId]](httpClient, context, body))
  }

  def deleteReliefClaim(ctx: JourneyContextWithNino, claimId: String)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonDeleteReads
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1509, appConfig.api1509Url(ctx.nino, claimId))

    EitherT(delete(httpClient, context))
  }

}
