/*
 * Copyright 2025 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import config.AppConfig
import connectors.HIP.BusinessDetailsConnector
import models.common.{BusinessId, Mtditid, Nino}
import models.connector.ApiResponse
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.domain.ApiResultT
import stubs.connectors.StubIFSConnector.api1171EmptyResponse
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

final case class StubBusinessDetailsConnector @Inject() (httpClient: HttpClient,
                                                         appConfig: AppConfig,
                                                         getBusinessDetailsRes: StubBusinessDetailsConnector.Api1171Response =
                                                         api1171EmptyResponse.asRight)
    extends BusinessDetailsConnector {

  def getBusinessDetails(businessId: BusinessId, mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema] =
    EitherT.fromEither[Future](getBusinessDetailsRes)
}

object StubBusinessDetailsConnector {
  type Api1171Response = ApiResponse[BusinessDetailsSuccessResponseSchema]
}
