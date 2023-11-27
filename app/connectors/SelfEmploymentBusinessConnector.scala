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
import models.common.RequestData
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmploymentBusinessConnector @Inject() (val http: HttpClient, appConfig: AppConfig) {

  def createSEPeriodSummary[T](data: RequestData, answers: T)(implicit
      writes: Writes[T],
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Api1894Response] =
    http.POST[T, Api1894Response](
      url = buildUrl(s"/income-tax/${data.taxYear.value}/${data.nino.value}/self-employments/${data.businessId.value}/periodic-summaries"),
      body = answers)(wts = writes, rds = createSEPeriodSummaryHttpReads, hc, ec)

  private def buildUrl(path: String): String =
    appConfig.ifsBaseUrl + path

}
