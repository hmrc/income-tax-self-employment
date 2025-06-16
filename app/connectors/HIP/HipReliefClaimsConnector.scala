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
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.connector.api_1505.{ClaimId, CreateLossClaimRequestBodyHip}
import models.connector.{ApiResponse, HipApiName, ReliefClaimType, createCommonErrorParser}
import models.domain.ApiResultT
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import utils.IdGenerator

import java.net.{URI, URL}
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class HipReliefClaimsConnector @Inject ()(httpClientV2: HttpClientV2,
                                         idGenerator: IdGenerator,
                                         appConfig: AppConfig)(implicit ec: ExecutionContext)
  extends Logging {

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
          case _ =>
            Left(createCommonErrorParser(method, url, response).handleDownstreamError(response))
        }
      }
    }

    val body = CreateLossClaimRequestBodyHip(
      incomeSourceId = ctx.businessId.value,
      reliefClaimed = answer.toString,
      taxYearClaimedFor = ctx.taxYear.endYear
    )

    EitherT {
      httpClientV2
        .post(getUrl(ctx.nino))(enrichedHeaderCarrier)
        .withBody(Json.toJson(body))
        .execute[ApiResponse[ClaimId]]
    }
  }

  private def deleteReliefClaimsUrl(taxableEntityId: Nino, claimId: String, taxYear: TaxYear): URI = {

    val baseUrl = s"${appConfig.hipBaseUrl}/itsd/income-sources/claims-for-relief/$taxableEntityId/$claimId"

    val queryParams = Seq(
      s"taxYear=${TaxYear.asTys(taxYear)}"
    ).filter(_.nonEmpty).mkString("&")

    new URI(s"$baseUrl?$queryParams")
  }

  private def deleteAdditionalHeaders(): Seq[(String, String)] = Seq(
    "correlationid" -> idGenerator.generateCorrelationId()
  )

  def deleteReliefClaim(ctx: JourneyContextWithNino, claimId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {

    val url: URI                             = deleteReliefClaimsUrl(ctx.nino, claimId, ctx.taxYear)
    val enrichedHeaderCarrier: HeaderCarrier = appConfig.mkMetadata(HipApiName.Api1509, url.toString).enrichedHeaderCarrier

    implicit object DeleteReliefClaimsHttpReads extends HttpReads[ApiResponse[Unit]] {
      override def read(method: String, url: String, response: HttpResponse): ApiResponse[Unit] =
        response.status match {
          case NO_CONTENT => Right(())
          case BAD_REQUEST | UNAUTHORIZED | NOT_FOUND| NOT_IMPLEMENTED | UNPROCESSABLE_ENTITY | INTERNAL_SERVER_ERROR | BAD_GATEWAY | SERVICE_UNAVAILABLE =>
            logger.error(s"HIP Relief Claims API returned unexpected status '${response.status}'")
            Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
          case _ => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
        }
    }

    EitherT {
      httpClientV2
        .delete(url.toURL)(enrichedHeaderCarrier)
        .setHeader(deleteAdditionalHeaders(): _*)
        .execute
    }
  }

}
