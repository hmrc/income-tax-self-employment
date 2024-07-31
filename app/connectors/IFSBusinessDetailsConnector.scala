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
import models.connector.api_1171.SuccessResponseSchema
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait IFSBusinessDetailsConnector {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema]
}

object IFSBusinessDetailsConnector {
  type Api1171Response = ApiResponse[api_1171.SuccessResponseSchema]
}

@Singleton
class IFSBusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends IFSBusinessDetailsConnector with Logging {
  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) = s"${appConfig.ifsBaseUrl}/registration/business-details/$idType/$idNumber"

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[SuccessResponseSchema] = {
    val url                                                                    = api1171BusinessDetailsUrl(IdType.Nino, nino.value)
    val context                                                                = appConfig.mkIFSMetadata(IFSApiName.Api1171, url)
    implicit val reads: HttpReads[ApiResponse[api_1171.SuccessResponseSchema]] = commonReads[api_1171.SuccessResponseSchema]

    EitherT(get[Api1171Response](http, context))
  }
}
