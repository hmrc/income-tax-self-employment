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

import cats.implicits._
import models.common._
import models.domain.Business
import models.error.DownstreamError
import services.BusinessService
import services.BusinessService.GetBusinessResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubBusinessService(
    getBusinessesRes: Either[DownstreamError, Seq[Business]] = Seq.empty.asRight[DownstreamError],
    getBusinessRes: Either[DownstreamError, Seq[Business]] = Seq.empty.asRight[DownstreamError]
) extends BusinessService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] = Future.successful(getBusinessesRes)

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    Future.successful(getBusinessRes)

}
