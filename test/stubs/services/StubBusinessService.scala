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

import bulders.BusinessDataBuilder.{aBusinessIncomeSourcesSummaryResponse, aNetBusinessProfitValues, aUserDateOfBirth}
import cats.data.EitherT
import cats.implicits._
import models.common._
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain.{ApiResultT, Business}
import models.error.{DownstreamError, ServiceError}
import models.frontend.adjustments.NetBusinessProfitValues
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.businessId

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class StubBusinessService(
    getBusinessesResult: Either[ServiceError, List[Business]] = Right(Nil),
    getBusinessResult: Either[ServiceError, Business] = Left(ServiceError.BusinessNotFoundError(businessId)),
    getUserDateOfBirthRes: Either[DownstreamError, LocalDate] = aUserDateOfBirth.asRight[DownstreamError],
    getAllBusinessIncomeSourcesSummariesRes: Either[DownstreamError, List[BusinessIncomeSourcesSummaryResponse]] =
      List.empty[BusinessIncomeSourcesSummaryResponse].asRight[DownstreamError],
    getBusinessIncomeSourcesSummaryRes: Either[DownstreamError, BusinessIncomeSourcesSummaryResponse] = Right(aBusinessIncomeSourcesSummaryResponse),
    getNetBusinessProfitValuesRes: Either[ServiceError, NetBusinessProfitValues] = Right(aNetBusinessProfitValues)
) extends BusinessService {

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[Business]] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[Business] =
    EitherT.fromEither[Future](getBusinessResult)

  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    EitherT.fromEither[Future](getUserDateOfBirthRes)

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, nino: Nino)(implicit
      hc: HeaderCarrier): ApiResultT[List[BusinessIncomeSourcesSummaryResponse]] =
    EitherT.fromEither[Future](getAllBusinessIncomeSourcesSummariesRes)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse] =
    EitherT.fromEither[Future](getBusinessIncomeSourcesSummaryRes)

  def getNetBusinessProfitValues(journeyContextWithNino: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[NetBusinessProfitValues] =
    EitherT.fromEither[Future](getNetBusinessProfitValuesRes)
}
