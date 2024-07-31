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
import models.common.{BusinessId, IdType, Nino, TaxYear}
import models.domain._
import models.error.DownstreamError
import services.BusinessService.{GetBusinessIncomeSourcesSummaryResponse, GetBusinessResponse, GetUserDateOfBirthResponse, getBusinessIncomeSourcesSummaryResponseStub}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessService @Inject()(businessConnector: GetBusinessDetailsConnector)(implicit ec: ExecutionContext) {

  def getBusinesses(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessResponse] =
    businessConnector
      .getBusinesses(IdType.Nino, nino)
      .map(_.map(_.taxPayerDisplayResponse)
        .map(taxPayerDisplayResponse =>
          taxPayerDisplayResponse.businessData.getOrElse(Nil).map(details => Business.mkBusiness(details, taxPayerDisplayResponse.yearOfMigration))))

  def getBusiness(nino: String, businessId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[GetBusinessResponse] =
    getBusinesses(nino).map(_.map(_.filter(_.businessId == businessId)))

  def getUserDateOfBirth(nino: String)(implicit hc: HeaderCarrier): Future[GetUserDateOfBirthResponse] =
    businessConnector
      .getCitizenDetails(nino)
      .map(_.map { userDetails =>
        parseDoBToLocalDate(userDetails.dateOfBirth)
      })

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): Future[GetBusinessIncomeSourcesSummaryResponse] = getBusinessIncomeSourcesSummaryResponseStub()

}

object BusinessService {
  type GetBusinessResponse                     = Either[DownstreamError, Seq[Business]]
  type GetUserDateOfBirthResponse              = Either[DownstreamError, LocalDate]
  type GetBusinessIncomeSourcesSummaryResponse = Either[DownstreamError, BusinessIncomeSourcesSummary]

  def parseDoBToLocalDate(dob: String): LocalDate = {
    val (year, month, day) = (dob.substring(4, 8), dob.substring(2, 4), dob.substring(0, 2))
    LocalDate.parse(s"$year-$month-$day")
  }

  def getBusinessIncomeSourcesSummaryResponseStub()(implicit ec: ExecutionContext): Future[GetBusinessIncomeSourcesSummaryResponse] = Future(
    Right(BusinessIncomeSourcesSummary.empty))
}
