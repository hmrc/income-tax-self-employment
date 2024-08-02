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
import config.AppConfig
import connectors.IFSBusinessDetailsConnector._
import models.common.TaxYear.asTys
import models.common._
import models.connector._
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait IFSBusinessDetailsConnector {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema]
  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[citizen_details.SuccessResponseSchema]
  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse]
}

object IFSBusinessDetailsConnector {
  type CitizenDetailsResponse = ApiResponse[citizen_details.SuccessResponseSchema]
  type Api1171Response        = ApiResponse[api_1171.SuccessResponseSchema]
  type Api1871Response        = ApiResponse[api_1871.BusinessIncomeSourcesSummaryResponse]
}

@Singleton
class IFSBusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends IFSBusinessDetailsConnector with Logging {

  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) = s"${appConfig.ifsBaseUrl}/registration/business-details/$idType/$idNumber"
  private def citizenDetailsUrl(idType: IdType, idNumber: Nino)           = s"${appConfig.citizenDetailsUrl}/citizen-details/$idType/$idNumber"
  // TODO BISSUrl is a placeholder, update with correct url
  private def businessIncomeSourcesSummaryUrl(taxYear: TaxYear, nino: Nino, businessId: BusinessId) =
    s"${appConfig.ifsBaseUrl}/${asTys(taxYear)}/$nino/$businessId"

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema] = {
    val url                                                                    = api1171BusinessDetailsUrl(IdType.Nino, nino.value)
    val context                                                                = appConfig.mkIFSMetadata(IFSApiName.Api1171, url)
    implicit val reads: HttpReads[ApiResponse[api_1171.SuccessResponseSchema]] = commonReads[api_1171.SuccessResponseSchema]

    EitherT(get[Api1171Response](http, context))
  }

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[citizen_details.SuccessResponseSchema] = {
    val url                                                                           = citizenDetailsUrl(IdType.Nino, nino)
    val context                                                                       = appConfig.mkIFSMetadata(IFSApiName.CitizenDetails, url)
    implicit val reads: HttpReads[ApiResponse[citizen_details.SuccessResponseSchema]] = commonReads[citizen_details.SuccessResponseSchema]

    EitherT(get[CitizenDetailsResponse](http, context))
  }

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse] = {
    val url     = businessIncomeSourcesSummaryUrl(taxYear, nino, businessId)
    val context = appConfig.mkIFSMetadata(IFSApiName.Api1871, url)
    implicit val reads: HttpReads[ApiResponse[api_1871.BusinessIncomeSourcesSummaryResponse]] =
      commonReads[api_1871.BusinessIncomeSourcesSummaryResponse]

    EitherT(get[Api1871Response](http, context))
  }
}
