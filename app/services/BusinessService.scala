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

package services

import connectors.SelfEmploymentConnector
import models.common.IdType
import models.domain._
import models.error.DownstreamError
import services.BusinessService.GetBusinessResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject() (businessConnector: SelfEmploymentConnector)(implicit ec: ExecutionContext) {

  def getBusinesses(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    businessConnector
      .getBusinesses(IdType.Nino, nino)
      .map(_.map(_.taxPayerDisplayResponse)
        .map(taxPayerDisplayResponse =>
          taxPayerDisplayResponse.businessData.getOrElse(Nil).map(details => Business.mkBusiness(details, taxPayerDisplayResponse.yearOfMigration))))

  def getBusiness(nino: String, businessId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessResponse] =
    getBusinesses(nino).map(_.map(_.filter(_.businessId == businessId)))
}

object BusinessService {
  type GetBusinessResponse = Either[DownstreamError, Seq[Business]]
}
