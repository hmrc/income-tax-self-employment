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

package stubs.connectors

import cats.implicits.catsSyntaxEitherId
import connectors.GetBusinessDetailsConnector
import connectors.GetBusinessDetailsConnector.Api1171Response
import models.common.IdType
import models.connector.api_1171
import stubs.connectors.StubGetBusinessDetailsConnector.api1171EmptyResponse
import uk.gov.hmrc.http.HeaderCarrier

import java.time.OffsetDateTime
import scala.concurrent.{ExecutionContext, Future}

case class StubGetBusinessDetailsConnector(
    getBusinessesResult: Future[Api1171Response] = Future.successful(api1171EmptyResponse.asRight)
) extends GetBusinessDetailsConnector {

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] =
    getBusinessesResult

}

object StubGetBusinessDetailsConnector {
  val api1171EmptyResponse: api_1171.SuccessResponseSchema =
    api_1171.SuccessResponseSchema(
      OffsetDateTime.now().toString,
      api_1171.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, None))

}
