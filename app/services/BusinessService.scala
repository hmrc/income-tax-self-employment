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

package services

import connectors.GetBusinessDetailsConnector
import models.common.{BusinessId, IdType, Nino}
import models.domain._
import models.error.DownstreamError
import services.BusinessService.GetBusinessResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait BusinessService {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): Future[GetBusinessResponse]
  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): Future[GetBusinessResponse]
}

@Singleton
class BusinessServiceImpl @Inject() (businessConnector: GetBusinessDetailsConnector)(implicit ec: ExecutionContext) extends BusinessService {

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    businessConnector
      .getBusinesses(IdType.Nino, nino.value)
      .map(_.map(_.taxPayerDisplayResponse)
        .map(taxPayerDisplayResponse =>
          taxPayerDisplayResponse.businessData.getOrElse(Nil).map(details => Business.mkBusiness(details, taxPayerDisplayResponse.yearOfMigration))))

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    getBusinesses(nino).map(_.map(_.filter(_.businessId == businessId.value)))

}

object BusinessService {
  type GetBusinessResponse = Either[DownstreamError, Seq[Business]]
}
