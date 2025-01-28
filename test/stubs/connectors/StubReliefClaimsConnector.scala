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

package stubs.connectors

import config.AppConfig
import connectors.ReliefClaimsConnector
import scala.concurrent.ExecutionContext.Implicits.global
import models.connector.{ApiResponse, api_1867}

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject

final case class StubReliefClaimsConnector @Inject() (httpClient: HttpClient,
                                                      appConfig: AppConfig,
                                                      getReliefClaimsRes: StubReliefClaimsConnector.Api1867Response = Right(List.empty))
    extends ReliefClaimsConnector(httpClient, appConfig) {

  override def getReliefClaimsPost2024(taxYear: String, mtditid: String)(implicit hc: HeaderCarrier): Future[StubReliefClaimsConnector.Api1867Response] =
    Future.successful(getReliefClaimsRes)
}

object StubReliefClaimsConnector {
  type Api1867Response = ApiResponse[List[api_1867.ReliefClaim]]
}
