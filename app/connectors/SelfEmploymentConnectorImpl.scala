/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.SelfEmploymentConnector._
import models.common.TaxYear.{asTys, endDate, startDate}
import models.common._
import models.connector.IntegrationContext.IFSHeaderCarrier
import models.connector._
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Keep the methods sorted by API number
  */
trait SelfEmploymentConnector {
  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response]
  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response]
  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response]
  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response]
  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response]
  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response]
  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response]
}

object SelfEmploymentConnector {
  type Api1171Response = ApiResponse[api_1171.SuccessResponseSchema]
  type Api1786Response = ApiResponse[api_1786.SuccessResponseSchema]
  type Api1802Response = ApiResponse[api_1802.response.CreateAmendSEAnnualSubmissionResponse]
  type Api1803Response = ApiResponse[api_1803.SuccessResponseSchema]
  type Api1894Response = ApiResponse[api_1894.response.CreateSEPeriodSummaryResponse]
  type Api1895Response = ApiResponse[api_1895.response.AmendSEPeriodSummaryResponse]
  type Api1965Response = ApiResponse[api_1965.ListSEPeriodSummariesResponse]
}

@Singleton
class SelfEmploymentConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends SelfEmploymentConnector with Logging {
  private val headerCarrierConfig = HeaderCarrier.Config.fromConfig(ConfigFactory.load())
  private val mkIFSMetadata       = IFSHeaderCarrier(headerCarrierConfig, appConfig, _, _)

  // TODO Move to GetBusinessDetailsConnector
  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) = s"${appConfig.ifsBaseUrl}/registration/business-details/$idType/$idNumber"

  private def baseUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${appConfig.ifsBaseUrl}/income-tax/${asTys(taxYear)}/$nino/self-employments/$incomeSourceId"

  private def annualSummariesUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/annual-summaries"
  private def periodicSummaries(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summaries"
  private def periodicSummariesFromTo(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summaries?from=${startDate(taxYear)}&to=${endDate(taxYear)}"
  private def periodicSummaryDetailUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summary-detail?from=${startDate(taxYear)}&to=${endDate(taxYear)}"

  // TODO Move to GetBusinessDetailsConnector
  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] = {
    val context = mkIFSMetadata(IFSApiName.Api1171, api1171BusinessDetailsUrl(idType, idNumber))
    get[Api1171Response](http, context)
  }

  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] = {
    val url     = annualSummariesUrl(data.nino, data.businessId, data.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1802, url)
    put[CreateAmendSEAnnualSubmissionRequestBody, Api1802Response](http, context, data.body)
  }

  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] = {
    val url     = periodicSummaries(data.nino, data.businessId, data.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1894, url)
    post[CreateSEPeriodSummaryRequestBody, Api1894Response](http, context, data.body)
  }

  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] = {
    val url     = periodicSummariesFromTo(data.nino, data.businessId, data.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1895, url)
    put[AmendSEPeriodSummaryRequestBody, Api1895Response](http, context, data.body)
  }

  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] = {
    val url     = periodicSummaries(ctx.nino, ctx.businessId, ctx.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1965, url)
    get[Api1965Response](http, context)
  }

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response] = {
    val url     = periodicSummaryDetailUrl(ctx.nino, ctx.businessId, ctx.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1786, url)
    get[Api1786Response](http, context)
  }

  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response] = {
    val url     = annualSummariesUrl(ctx.nino, ctx.businessId, ctx.taxYear)
    val context = mkIFSMetadata(IFSApiName.Api1803, url)
    get[Api1803Response](http, context)
  }
}
