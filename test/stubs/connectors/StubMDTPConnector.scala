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
import connectors.MDTP.MDTPConnector
import models.common.Nino
import models.connector.citizen_details.SuccessResponseSchema
import models.domain.{ApiResult, ApiResultT}
import models.error.ServiceError
import stubs.connectors.StubIFSConnector.citizenDetailsResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

final case class StubMDTPConnector(getCitizenDetailsRes: ApiResult[SuccessResponseSchema] = citizenDetailsResponse.asRight[ServiceError])
    extends MDTPConnector {

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[SuccessResponseSchema] =
    EitherT.fromEither(getCitizenDetailsRes)
}
