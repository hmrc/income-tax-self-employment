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

import com.typesafe.config.ConfigFactory
import config.AppConfig
import connectors.GetBusinessDetailsConnector.Api1171Response
import models.common._
import models.connector.IntegrationContext.IFSHeaderCarrier
import models.connector._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Keep the methods sorted by API number
  */
trait GetBusinessDetailsConnector {
  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response]
}

object GetBusinessDetailsConnector {
  type Api1171Response = ApiResponse[api_1171.SuccessResponseSchema]
}

@Singleton
class GetBusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends GetBusinessDetailsConnector with Logging {
  private val headerCarrierConfig = HeaderCarrier.Config.fromConfig(ConfigFactory.load())
  private val mkIFSMetadata       = IFSHeaderCarrier(headerCarrierConfig, appConfig, _, _)

  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) = s"${appConfig.ifsBaseUrl}/registration/business-details/$idType/$idNumber"

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] = {
    val context = mkIFSMetadata(IFSApiName.Api1171, api1171BusinessDetailsUrl(idType, idNumber))
    get[Api1171Response](http, context)
  }

}
