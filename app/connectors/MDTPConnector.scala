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
import models.common._
import models.connector._
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait MDTPConnector {
  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[citizen_details.SuccessResponseSchema]
}

@Singleton
class MDTPConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends MDTPConnector with Logging {
  private def citizenDetailsUrl(idType: IdType, idNumber: Nino) = s"${appConfig.ifsBaseUrl}/citizen-details/$idType/$idNumber"

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[citizen_details.SuccessResponseSchema] = {
    val url                                                                           = citizenDetailsUrl(IdType.Nino, nino)
    val context                                                                       = appConfig.mkIFSMetadata(IFSApiName.CitizenDetails, url)
    implicit val reads: HttpReads[ApiResponse[citizen_details.SuccessResponseSchema]] = commonReads[citizen_details.SuccessResponseSchema]

    EitherT(get[CitizenDetailsResponse](http, context))
  }
}
