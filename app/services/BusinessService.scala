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

import cats.data.EitherT
import connectors.IFSBusinessDetailsConnector
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain._
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait BusinessService {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[Business]]
  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[Business]
  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[LocalDate]
  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse]
}

@Singleton
class BusinessServiceImpl @Inject() (businessConnector: IFSBusinessDetailsConnector)(implicit ec: ExecutionContext) extends BusinessService {

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[Business]] =
    for {
      maybeBusinesses <- businessConnector.getBusinesses(nino)
      maybeYearOfMigration = maybeBusinesses.taxPayerDisplayResponse.yearOfMigration
      businesses           = maybeBusinesses.taxPayerDisplayResponse.businessData.getOrElse(Nil)
    } yield businesses.map(b => Business.mkBusiness(b, maybeYearOfMigration))

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[Business] =
    for {
      businesses <- getBusinesses(nino)
      maybeBusiness = businesses.find(_.businessId == businessId.value)
      business <- EitherT.fromOption[Future](maybeBusiness, BusinessNotFoundError(businessId)).leftAs[ServiceError]
    } yield business

  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    businessConnector.getCitizenDetails(nino).map(_.parseDoBToLocalDate)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse] =
    businessConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)
}
