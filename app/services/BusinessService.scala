/*
 * Copyright 2025 HM Revenue & Customs
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
import config.{AppConfig, FeatureSwitchConfig}
import connectors.HIP.{BusinessDetailsConnector, IncomeSourcesConnector}
import connectors.IFS.{IFSBusinessDetailsConnector, IFSConnector}
import connectors.MDTP.MDTPConnector
import models.common.{BusinessId, JourneyContextWithNino, Mtditid, Nino, TaxYear}
import models.connector.api_1803
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.domain._
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions
import utils.Logging

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessService @Inject()(ifsBusinessDetailsConnector: IFSBusinessDetailsConnector,
                                mdtpConnector: MDTPConnector,
                                hipBusinessDetailsConnector: BusinessDetailsConnector,
                                ifsConnector: IFSConnector,
                                hipIncomeSourceConnector: IncomeSourcesConnector,
                                appConfig: FeatureSwitchConfig)
                               (implicit ec: ExecutionContext)
  extends Logging {

  def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[BusinessDetailsSuccessResponseSchema]] =
    if (appConfig.hipMigration1171Enabled)
      hipBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino).map {
        case Some(successWrapper) => Some(successWrapper.success)
        case None => None
      }
    else {
      ifsBusinessDetailsConnector.getBusinesses(nino).bimap(
        error => {
          logger.warn(s"IFS Business Details Connector returned status ${error.status} with message '${error.errorMessage}'")
          error
        },
        response =>
          Some(response)
      )
    }

  def getBusinesses(mtditid: Mtditid, nino: Nino)
                   (implicit hc: HeaderCarrier): ApiResultT[List[Business]] =
    for {
      maybeBusinesses <- getBusinessDetails(None, mtditid, nino)
      businessList = maybeBusinesses.map(_.toBusinesses).getOrElse(Nil)
    } yield businessList

  def getBusiness(businessId: BusinessId, mtditid: Mtditid, nino: Nino)
                 (implicit hc: HeaderCarrier): ApiResultT[Business] =
    for {
      maybeResponse <- getBusinessDetails(Some(businessId), mtditid, nino)
      maybeBusiness = maybeResponse.flatMap(_.toBusinesses.headOption)
      business <- EitherT.fromOption[Future](maybeBusiness, BusinessNotFoundError(businessId)).leftAs[ServiceError]
    } yield business

  def getUserBusinessIds(mtditid: Mtditid, nino: Nino)
                        (implicit hc: HeaderCarrier): ApiResultT[List[BusinessId]] =
    getBusinesses(mtditid, nino).map(_.map(business => BusinessId(business.businessId)))

  def getUserDateOfBirth(nino: Nino)
                        (implicit hc: HeaderCarrier): ApiResultT[LocalDate] =
    for {
      citizenDetails <- mdtpConnector.getCitizenDetails(nino)
      dateOfBirth <- EitherT.fromEither[Future](citizenDetails.parseDoBToLocalDate)
    } yield dateOfBirth

  def getAllBusinessIncomeSourcesSummaries(taxYear: TaxYear, mtditid: Mtditid, nino: Nino)
                                          (implicit hc: HeaderCarrier): ApiResultT[List[BusinessIncomeSourcesSummaryResponse]] =
    for {
      businessIds <- getUserBusinessIds(mtditid, nino)
      summaryList <- businessIds.traverse { businessId =>
        ifsBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)
      }
    } yield summaryList

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)
                                     (implicit hc: HeaderCarrier): ApiResultT[BusinessIncomeSourcesSummaryResponse] =
    ifsBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)

  def getNetBusinessProfitOrLossValues(ctx: JourneyContextWithNino)
                                      (implicit hc: HeaderCarrier): ApiResultT[NetBusinessProfitOrLossValues] =
    for {
      incomeSummary <- ifsBusinessDetailsConnector.getBusinessIncomeSourcesSummary(ctx.taxYear, ctx.nino, ctx.businessId)
      periodSummary <- EitherT(ifsConnector.getPeriodicSummaryDetail(ctx))
      annualSubmission <- EitherT[Future, ServiceError, api_1803.SuccessResponseSchema](ifsConnector.getAnnualSummaries(ctx))
      result <- EitherT.fromEither[Future](NetBusinessProfitOrLossValues.fromApiAnswers(incomeSummary, periodSummary, annualSubmission))
    } yield result

  def hasOtherIncomeSources(taxYear: TaxYear, nino: Nino)
                           (implicit hc: HeaderCarrier): ApiResultT[Boolean] = {
    val incomeSources = if (appConfig.hipMigration2085Enabled) {
      hipIncomeSourceConnector.getIncomeSources(nino)
    } else {
      ifsBusinessDetailsConnector.getListOfIncomeSources(taxYear, nino)
    }

    incomeSources.map(_.selfEmployments.sizeIs > 1)
  }
}
