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
import models.common.{Mtditid, Nino}
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.connector.{ApiResponse, HipApiName, IntegrationContext, businessDetailsReads}
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.{Logging, ZonedDateTimeMachine}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait BusinessDetailsConnector {
  def getBusinessDetails(mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema]
}

@Singleton
class BusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig, timeMachine: ZonedDateTimeMachine)
    extends BusinessDetailsConnector
    with Logging {

  private def getBusinessDetailsUrl(mtdReference: Mtditid, nino: Nino): String =
    s"${appConfig.hipBaseUrl}/etmp/RESTAdapter/itsa/taxpayer/business-details?mtdReference=$mtdReference&nino=$nino"

  private val additionalHeaders: Seq[(String, String)] = Seq(
    "X-Message-Type"        -> "TaxpayerDisplay",
    "X-Originating-System"  -> "MDTP",
    "X-Receipt-Date"        -> timeMachine.now.toString,
    "X-Regime-Type"         -> "ITSA",
    "X-Transmitting-System" -> "HIP"
  )

  def getBusinessDetails(mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema] = {
    val url: String                                                                  = getBusinessDetailsUrl(mtditid, nino)
    val context: IntegrationContext.IntegrationHeaderCarrier                         = appConfig.mkMetadata(HipApiName.Api1171, url)
    implicit val reads: HttpReads[ApiResponse[BusinessDetailsSuccessResponseSchema]] = businessDetailsReads[BusinessDetailsSuccessResponseSchema]

    EitherT(connectors.getWithHeaders[ApiResponse[BusinessDetailsSuccessResponseSchema]](http, context, additionalHeaders))

  }

}
