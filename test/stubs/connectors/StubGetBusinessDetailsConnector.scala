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

import bulders.BusinessDataBuilder.citizenDetailsDateOfBirth
import cats.implicits.catsSyntaxEitherId
import connectors.GetBusinessDetailsConnector
import connectors.GetBusinessDetailsConnector.{Api1171Response, CitizenDetailsResponse}
import models.common.{IdType, Nino}
import models.connector.citizen_details.{Ids, LegalNames, Name}
import models.connector.{api_1171, citizen_details}
import stubs.connectors.StubGetBusinessDetailsConnector.{api1171EmptyResponse, citizenDetailsResponse}
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.OffsetDateTime
import scala.concurrent.{ExecutionContext, Future}

case class StubGetBusinessDetailsConnector(
    getBusinessesResult: Future[Api1171Response] = Future.successful(api1171EmptyResponse.asRight),
    getCitizenDetailsResult: Future[CitizenDetailsResponse] = Future.successful(citizenDetailsResponse.asRight)
) extends GetBusinessDetailsConnector {

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] =
    getBusinessesResult

  def getCitizenDetails(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CitizenDetailsResponse] = getCitizenDetailsResult

}

object StubGetBusinessDetailsConnector {
  val api1171EmptyResponse: api_1171.SuccessResponseSchema =
    api_1171.SuccessResponseSchema(
      OffsetDateTime.now().toString,
      api_1171.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, None))

  val citizenDetailsResponse: citizen_details.SuccessResponseSchema =
    citizen_details.SuccessResponseSchema(
      name = LegalNames(current = Name(firstName = "Mike", lastName = "Wazowski"), previous = List(Name(firstName = "Jess", lastName = "Smith"))),
      ids = Ids(nino = nino.value),
      dateOfBirth = citizenDetailsDateOfBirth
    )

}
