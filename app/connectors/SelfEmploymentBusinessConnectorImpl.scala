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

import config.AppConfig
import connectors.httpParsers.api_1802.CreateAmendSEAnnualSubmissionHttpParser.{Api1802Response, createSEAnnualSubmissionHttpReads}
import connectors.httpParsers.api_1894.CreateSEPeriodSummaryHttpParser.{Api1894Response, createSEPeriodSummaryHttpReads}
import connectors.httpParsers.api_1895.AmendSEPeriodSummaryHttpParser.{Api1895Response, amendSEPeriodSummaryHttpReads}
import connectors.httpParsers.api_1965.ListSEPeriodSummariesHttpParser.{Api1965Response, listSEPeriodSummariesHttpReads}
import models.common.JourneyAnswersContext.JourneyContextWithNino
import models.common.TaxYear.{asTys, endDate, startDate}
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SelfEmploymentBusinessConnector {
  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response]
  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response]
  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response]
  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response]
}

@Singleton
class SelfEmploymentBusinessConnectorImpl @Inject() (val http: HttpClient, appConfig: AppConfig) extends SelfEmploymentBusinessConnector {

  // Pull out common parts of the URLs as too much copy/paste
  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] = {
    val url = buildUrl(s"/income-tax/${asTys(data.taxYear)}/${data.nino}/self-employments/${data.businessId}/periodic-summaries")

    http.POST[CreateSEPeriodSummaryRequestBody, Api1894Response](url, data.body)
  }

  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] = {
    val url = buildUrl(s"/income-tax/${asTys(data.taxYear)}/${data.nino}/self-employments/${data.businessId}/periodic-summaries?from=${startDate(
        data.taxYear)}&to=${endDate(data.taxYear)}")

    http.PUT[AmendSEPeriodSummaryRequestBody, Api1895Response](url, data.body)
  }

  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] = {
    val url = buildUrl(s"/income-tax/${asTys(ctx.taxYear)}/${ctx.nino}/self-employments/${ctx.businessId}/periodic-summaries")

    http.GET[Api1965Response](url)
  }

  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] = {
    val url = buildUrl(s"/income-tax/${asTys(data.taxYear)}/${data.nino}/self-employments/${data.businessId}/annual-summaries")

    http.PUT[CreateAmendSEAnnualSubmissionRequestBody, Api1802Response](url, data.body)
  }

  private def buildUrl(path: String): String =
    appConfig.ifsBaseUrl + path

}
