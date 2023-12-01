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
import connectors.httpParsers.CreateSEPeriodSummaryHttpParser.{Api1894Response, createSEPeriodSummaryHttpReads}
import models.common.TaxYear.asTys
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait SelfEmploymentBusinessConnector {
  def createSEPeriodSummary(requestData: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response]
}

@Singleton
class SelfEmploymentBusinessConnectorImpl @Inject() (val http: HttpClient, appConfig: AppConfig) extends SelfEmploymentBusinessConnector {

  def createSEPeriodSummary(
      requestData: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] =
    http.POST[CreateSEPeriodSummaryRequestBody, Api1894Response](
      url = buildUrl(
        s"/income-tax/${asTys(requestData.taxYear)}/${requestData.nino.value}/self-employments/${requestData.businessId.value}/periodic-summaries"),
      body = requestData.body
    )(wts = CreateSEPeriodSummaryRequestBody.formats, rds = createSEPeriodSummaryHttpReads, hc, ec)

  private def buildUrl(path: String): String =
    appConfig.ifsBaseUrl + path

}
