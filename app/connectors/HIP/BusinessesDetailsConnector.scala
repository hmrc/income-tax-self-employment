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
import models.common.{BusinessId, JourneyContextWithNino, Mtditid, Nino}
import models.connector.{ApiResponse, HipApiName, IntegrationContext, api_1171, commonReads}
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import java.time.{ZoneOffset, ZonedDateTime}
import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait BusinessesDetailsConnector {
  def getBusinessDetails(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema]
}

@Singleton
class BusinessesDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends BusinessesDetailsConnector with Logging {

  private def getBusinessDetailsUrl(incomeSourceId: BusinessId, mtdReference: Mtditid, nino: Nino): String =
    s"${appConfig.hipBaseUrl}/etmp/RESTAdapter/itsa/taxpayer/business-details/$incomeSourceId/$mtdReference/$nino"

  private val additionalHeaders: Seq[(String, String)] = Seq(
    "X-Message-Type"        -> "TaxpayerDisplay",
    "X-Originating-System"  -> "MDTP",
    "X-Receipt-Date"        -> ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString,
    "X-Regime-Type"         -> "ITSA",
    "X-Transmitting-System" -> "HIP"
  )

  def getBusinessDetails(
      ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema] = {
    val url: String                                                            = getBusinessDetailsUrl(ctx.businessId, ctx.mtditid, ctx.nino)
    val context: IntegrationContext.IntegrationHeaderCarrier                   = appConfig.mkMetadata(HipApiName.Api1171, url)
    implicit val reads: HttpReads[ApiResponse[api_1171.SuccessResponseSchema]] = commonReads[api_1171.SuccessResponseSchema]

    EitherT(connectors.getWithHeaders[ApiResponse[api_1171.SuccessResponseSchema]](http, context, additionalHeaders))

  }

}
