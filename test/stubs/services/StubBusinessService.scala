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

package stubs.services

import bulders.BusinessDataBuilder.aUserDateOfBirth
import cats.implicits._
import models.common._
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain.Business
import models.error.DownstreamError
import services.BusinessService
import services.BusinessService.{GetBusinessIncomeSourcesSummaryResponse, GetBusinessResponse, GetUserDateOfBirthResponse}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

case class StubBusinessService(
    getBusinessesRes: Either[DownstreamError, Seq[Business]] = Seq.empty.asRight[DownstreamError],
    getBusinessRes: Either[DownstreamError, Seq[Business]] = Seq.empty.asRight[DownstreamError],
    getUserDateOfBirthRes: Either[DownstreamError, LocalDate] = aUserDateOfBirth.asRight[DownstreamError],
    getBusinessIncomeSourcesSummaryRes: Either[DownstreamError, BusinessIncomeSourcesSummaryResponse] =
      BusinessIncomeSourcesSummaryResponse.empty.asRight[DownstreamError]
) extends BusinessService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] = Future.successful(getBusinessesRes)

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    Future.successful(getBusinessRes)

  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): Future[GetUserDateOfBirthResponse] =
    Future.successful(getUserDateOfBirthRes)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): Future[GetBusinessIncomeSourcesSummaryResponse] =
    Future.successful(getBusinessIncomeSourcesSummaryRes)

}
