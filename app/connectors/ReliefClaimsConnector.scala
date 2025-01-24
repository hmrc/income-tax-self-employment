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

import config.AppConfig
import jakarta.inject.Inject
import models.connector.api_1867.ReliefClaim
import models.connector.{ApiResponse, IFSApiName, api_1507, api_1867, commonGetListReads}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

class ReliefClaimsConnector @Inject() (httpClient: HttpClient, appConfig: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  type Api1867Response = ApiResponse[List[ReliefClaim]]
  type Api1507Response = ApiResponse[List[ReliefClaim]]

  def getReliefClaims1867(taxYear: String, mtditid: String)(implicit hc: HeaderCarrier): Future[Api1867Response] = {
    implicit val reads: HttpReads[ApiResponse[List[api_1867.ReliefClaim]]] = commonGetListReads[api_1867.ReliefClaim]

    get[Api1867Response](httpClient, appConfig.mkMetadata(IFSApiName.Api1867, appConfig.api1867Url(taxYear, mtditid)))
  }

  def getReliefClaims1507(taxYear: String, mtditid: String)(implicit hc: HeaderCarrier): Future[Api1507Response] = {
    implicit val reads: HttpReads[ApiResponse[List[api_1507.ReliefClaim]]] = commonGetListReads[api_1507.ReliefClaim]

    get[Api1507Response](httpClient, appConfig.mkMetadata(IFSApiName.Api1507, appConfig.api1507Url(taxYear, mtditid)))
  }

}
