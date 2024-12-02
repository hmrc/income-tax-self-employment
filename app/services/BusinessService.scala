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
import cats.implicits.toTraverseOps
import connectors.{IFSBusinessDetailsConnector, IFSConnector, MDTPConnector}
import models.common.{BusinessId, JourneyContextWithNino, Nino, TaxYear}
import models.connector.api_1803
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain._
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait BusinessService {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[Business]]

  def getBusiness(nino: Nino, businessId: BusinessId)(implicit hc: HeaderCarrier): ApiResultT[Business]

  def getUserBusinessIds(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[BusinessId]]

  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[LocalDate]

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, nino: Nino)(implicit
      hc: HeaderCarrier): ApiResultT[List[BusinessIncomeSourcesSummaryResponse]]

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse]

  def getNetBusinessProfitOrLossValues(journeyContextWithNino: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[NetBusinessProfitOrLossValues]

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[Boolean]
}

@Singleton
class BusinessServiceImpl @Inject() (businessConnector: IFSBusinessDetailsConnector, mdtpConnector: MDTPConnector, ifsConnector: IFSConnector)(
    implicit ec: ExecutionContext)
    extends BusinessService {

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

  def getUserBusinessIds(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[List[BusinessId]] =
    getBusinesses(nino).map(_.map(business => BusinessId(business.businessId)))

  def getUserDateOfBirth(nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    for {
      citizenDetails <- mdtpConnector.getCitizenDetails(nino)
      dateOfBirth    <- EitherT.fromEither[Future](citizenDetails.parseDoBToLocalDate)
    } yield dateOfBirth

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, nino: Nino)(implicit
      hc: HeaderCarrier): ApiResultT[List[BusinessIncomeSourcesSummaryResponse]] =
    for {
      businesses <- getBusinesses(nino)
      businessIds = businesses.map(business => BusinessId(business.businessId))
      summaryList <- businessIds.traverse { businessId =>
        businessConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)
      }
    } yield summaryList

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse] =
    businessConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)

  def getNetBusinessProfitOrLossValues(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[NetBusinessProfitOrLossValues] =
    for {
      incomeSummary    <- businessConnector.getBusinessIncomeSourcesSummary(ctx.taxYear, ctx.nino, ctx.businessId)
      periodSummary    <- EitherT(ifsConnector.getPeriodicSummaryDetail(ctx))
      annualSubmission <- EitherT[Future, ServiceError, api_1803.SuccessResponseSchema](ifsConnector.getAnnualSummaries(ctx))
      result           <- EitherT.fromEither[Future](NetBusinessProfitOrLossValues.fromApiAnswers(incomeSummary, periodSummary, annualSubmission))
    } yield result

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[Boolean] =
    businessConnector.getListOfIncomeSources(taxYear, nino).map(_.selfEmployments.sizeIs > 1)
}
