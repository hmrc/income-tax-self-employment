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
import models.common.{IdType, JourneyContextWithNino}
import models.connector.IntegrationContext.IFSHeaderCarrier
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import models.connector.{ApiResponse, IFSApiName, api_1171, api_1802, api_1894, api_1895, api_1965}
import uk.gov.hmrc.http.HeaderCarrier.Config
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Keep the methods sorted by API number // TODO Rename to IFSConnector
  */
trait SelfEmploymentConnector {
  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response]
  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response]
  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response]
  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response]
  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response]
}

object SelfEmploymentConnector {
  type Api1171Response = ApiResponse[api_1171.SuccessResponseSchema]
  type Api1802Response = ApiResponse[api_1802.response.CreateAmendSEAnnualSubmissionResponse]
  type Api1894Response = ApiResponse[api_1894.response.CreateSEPeriodSummaryResponse]
  type Api1895Response = ApiResponse[api_1895.response.AmendSEPeriodSummaryResponse]
  type Api1965Response = ApiResponse[api_1965.ListSEPeriodSummariesResponse]
}

@Singleton
class SelfEmploymentConnectorImpl @Inject() (val http: HttpClient, val appConfig: AppConfig) extends SelfEmploymentConnector {
  private val headerCarrierConfig: Config = HeaderCarrier.Config.fromConfig(ConfigFactory.load())
  private val ifsHeader                   = IFSHeaderCarrier(headerCarrierConfig, appConfig, _, _)

  private def buildUrl(path: String): String = appConfig.ifsBaseUrl + path

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] = {
    val url     = buildUrl(s"/registration/business-details/$idType/$idNumber")
    val context = ifsHeader(IFSApiName.Api1171, url)
    get[Api1171Response](http, context)
  }

  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] = {
    val url     = buildUrl(s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId}/annual-summaries")
    val context = ifsHeader(IFSApiName.Api1802, url)
    put[CreateAmendSEAnnualSubmissionRequestBody, Api1802Response](http, context, data.body)
  }

  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] = {
    val url     = buildUrl(s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId}/periodic-summaries")
    val context = ifsHeader(IFSApiName.Api1894, url)
    post[CreateSEPeriodSummaryRequestBody, Api1894Response](http, context, data.body)
  }

  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] = {
    val url = buildUrl(
      s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId}/periodic-summaries?from=${startDate(
          data.taxYear)}&to=${endDate(data.taxYear)}")
    val context = ifsHeader(IFSApiName.Api1895, url)
    put[AmendSEPeriodSummaryRequestBody, Api1895Response](http, context, data.body)
  }

  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] = {
    val url     = buildUrl(s"/income-tax/${asTys(ctx.taxYear)}/${ctx.nino.value}/self-employments/${ctx.businessId}/periodic-summaries")
    val context = IFSHeaderCarrier(headerCarrierConfig, appConfig, IFSApiName.Api1965, url)
    get[Api1965Response](http, context)
  }
}
