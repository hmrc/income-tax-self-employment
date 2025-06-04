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

package connectors.HIP

import cats.data.EitherT
import config.AppConfig
import jakarta.inject.{Inject, Singleton}
import models.common.{JourneyContextWithNino, Nino}
import models.connector.api_1505.{ClaimId, CreateLossClaimRequestBody}
import models.connector.{ApiResponse, HipApiName, ReliefClaimType, createCommonErrorParser}
import models.domain.ApiResultT
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}

import java.net.URL
import scala.concurrent.ExecutionContext

@Singleton
class HipReliefClaimsConnector @Inject()(httpClientV2: HttpClientV2,
                                         appConfig: AppConfig
                                        )
                                        (implicit ec: ExecutionContext) extends Logging {

  val getUrl: Nino => URL = (nino) => url"${appConfig.hipBaseUrl}/itsd/income-sources/claims-for-relief/$nino"

  def createReliefClaim(ctx: JourneyContextWithNino, answer: ReliefClaimType)(implicit hc: HeaderCarrier): ApiResultT[ClaimId] = {
    val enrichedHeaderCarrier: HeaderCarrier = appConfig.mkMetadata(HipApiName.Api1505, getUrl(ctx.nino).toString).enrichedHeaderCarrier

    implicit val reads: HttpReads[ApiResponse[ClaimId]] = new HttpReads[ApiResponse[ClaimId]] {
      override def read(method: String, url: String, response: HttpResponse): ApiResponse[ClaimId] = {

        response.status match {
          case OK =>
            response.json
            .validate[ClaimId]
            .fold(
              errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
              claimId => Right(claimId)
            )
          case NOT_FOUND =>
            logger.warn(s"$response")
            Right(ClaimId(""))
          case _ =>
            logger.error(s"HIP GET Create Claim for Relief returned unexpected status '${response.status}'")
            Left(createCommonErrorParser(method, url, response).handleDownstreamError(response))
        }
      }
    }

    val body = CreateLossClaimRequestBody(
      incomeSourceId = ctx.businessId.value,
      reliefClaimed = answer.toString,
      taxYear = ctx.taxYear.endYear.toString
    )

    EitherT {
      httpClientV2
        .post(getUrl(ctx.nino))(enrichedHeaderCarrier)
        .withBody(Json.toJson(body))
        .execute[ApiResponse[ClaimId]]
    }
  }

}
