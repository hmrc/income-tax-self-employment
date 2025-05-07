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
import models.common.{BusinessId, Mtditid, Nino}
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.connector.{ApiResponse, HipApiName, createCommonErrorParser}
import models.domain.ApiResultT
import models.error.DownstreamError
import play.api.http.Status._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import utils.{IdGenerator, Logging, TimeMachine}

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait BusinessDetailsConnector {
  def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema]
}

@Singleton
class BusinessDetailsConnectorImpl @Inject() (httpClientV2: HttpClientV2, appConfig: AppConfig, timeMachine: TimeMachine, idGenerator: IdGenerator)
    extends BusinessDetailsConnector
    with Logging {

  private def getBusinessDetailsUrl(incomeSourceId: Option[BusinessId], mtdReference: Mtditid, nino: Nino): URI = new URI(
    s"${appConfig.hipBaseUrl}/etmp/RESTAdapter/itsa/taxpayer/business-details?incomeSourceId=$incomeSourceId&mtdReference=$mtdReference&nino=$nino")

  private val additionalHeaders: Seq[(String, String)] = Seq(
    "correlationid"         -> idGenerator.generateCorrelationId(),
    "X-Message-Type"        -> "TaxpayerDisplay",
    "X-Originating-System"  -> "MDTP",
    "X-Receipt-Date"        -> timeMachine.now.toString,
    "X-Regime-Type"         -> "ITSA",
    "X-Transmitting-System" -> "HIP"
  )

  def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema] = {

    val url: URI                             = getBusinessDetailsUrl(businessId, mtditid, nino)
    val enrichedHeaderCarrier: HeaderCarrier = appConfig.mkMetadata(HipApiName.Api1171, url.toString).enrichedHeaderCarrier

    implicit object BusinessDetailsHttpReads extends HttpReads[ApiResponse[BusinessDetailsSuccessResponseSchema]] {
      override def read(method: String, url: String, response: HttpResponse): ApiResponse[BusinessDetailsSuccessResponseSchema] = {

        val validatedResponse: Either[DownstreamError, BusinessDetailsSuccessResponseSchema] =
          if (response.body.isEmpty) {
            Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
          } else {
            response.json
              .validate[BusinessDetailsSuccessResponseSchema]
              .fold[Either[DownstreamError, BusinessDetailsSuccessResponseSchema]](
                errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
                parsedModel => Right(parsedModel)
              )
          }

        response.status match {
          case OK => validatedResponse
          case BAD_REQUEST | UNAUTHORIZED | FORBIDDEN | NOT_FOUND | UNSUPPORTED_MEDIA_TYPE | UNPROCESSABLE_ENTITY | INTERNAL_SERVER_ERROR |
              SERVICE_UNAVAILABLE =>
            Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
          case _ => Left(createCommonErrorParser(method, url, response).pagerDutyError(response))
        }
      }
    }

    EitherT {
      httpClientV2
        .get(url.toURL)(enrichedHeaderCarrier)
        .setHeader(additionalHeaders: _*)
        .execute
    }
  }

}
