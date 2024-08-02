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
import connectors.BusinessDetailsConnector.{Api1171Response, Api1871Response, CitizenDetailsResponse}
import models.common.TaxYear.asTys
import models.common._
import models.connector.IntegrationContext.IFSHeaderCarrier
import models.connector._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait BusinessDetailsConnector {
  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response]
  def getCitizenDetails(idNumber: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CitizenDetailsResponse]
  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Api1871Response]
}

object BusinessDetailsConnector {
  type CitizenDetailsResponse = ApiResponse[citizen_details.SuccessResponseSchema]
  type Api1171Response        = ApiResponse[api_1171.SuccessResponseSchema]
  type Api1871Response        = ApiResponse[api_1871.BusinessIncomeSourcesSummaryResponse]
}

@Singleton
class BusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends BusinessDetailsConnector with Logging {
  private val headerCarrierConfig = HeaderCarrier.Config.fromConfig(ConfigFactory.load())
  private val mkIFSMetadata       = IFSHeaderCarrier(headerCarrierConfig, appConfig, _, _)

  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) = s"${appConfig.ifsBaseUrl}/registration/business-details/$idType/$idNumber"
  private def citizenDetailsUrl(idType: IdType, idNumber: Nino)           = s"${appConfig.citizenDetailsUrl}/citizen-details/$idType/$idNumber"
  // TODO BISSUrl is a placeholder, update with correct url
  private def businessIncomeSourcesSummaryUrl(taxYear: TaxYear, nino: Nino, businessId: BusinessId) =
    s"${appConfig.ifsBaseUrl}/${asTys(taxYear)}/$nino/$businessId"

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] = {
    val context = mkIFSMetadata(IFSApiName.Api1171, api1171BusinessDetailsUrl(idType, idNumber))
    get[Api1171Response](http, context)
  }

  def getCitizenDetails(idNumber: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CitizenDetailsResponse] = {
    val context = mkIFSMetadata(IFSApiName.CitizenDetails, citizenDetailsUrl(IdType.Nino, idNumber))
    get[CitizenDetailsResponse](http, context)
  }

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Api1871Response] = {
    val context = mkIFSMetadata(IFSApiName.Api1871, businessIncomeSourcesSummaryUrl(taxYear, nino, businessId))
    get[Api1871Response](http, context)
  }

}
