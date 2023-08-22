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

import connectors.BusinessConnector
import connectors.BusinessConnector.Nino
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject()(businessConnector: BusinessConnector) {

  def getBusinesses(nino: String, businessId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessesResponse] =
    businessConnector.getBusinesses(Nino, nino).map(_.map(_.map({ getBusinessDataRequest =>
      getBusinessDataRequest.copy(businessData = getBusinessDataRequest.businessData.filter(_.incomeSourceId == businessId))
    })))
}
