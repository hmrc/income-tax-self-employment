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

package connectors

import cats.data.EitherT
import config.AppConfig
import connectors.IFSBusinessDetailsConnector._
import models.common.TaxYear.asTys
import models.common._
import models.connector._
import models.connector.api_1500.{CreateBroughtForwardLossRequestBody, CreateBroughtForwardLossRequestData}
import models.connector.api_1501.{UpdateBroughtForwardLossRequestBody, UpdateBroughtForwardLossRequestData}
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait IFSBusinessDetailsConnector {
  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema]
  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse]

  def createBroughtForwardLoss(
      data: CreateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1500.SuccessResponseSchema]
  def updateBroughtForwardLoss(
      data: UpdateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1501.SuccessResponseSchema]
  def getBroughtForwardLoss(nino: Nino, lossId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1502.SuccessResponseSchema]
  def deleteBroughtForwardLoss(nino: Nino, lossId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit]
  def listBroughtForwardLosses(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1870.SuccessResponseSchema]
}

object IFSBusinessDetailsConnector {
  type Api1171Response = ApiResponse[api_1171.SuccessResponseSchema]
  type Api1871Response = ApiResponse[api_1871.BusinessIncomeSourcesSummaryResponse]
  type Api1500Response = ApiResponse[api_1500.SuccessResponseSchema]
  type Api1501Response = ApiResponse[api_1501.SuccessResponseSchema]
  type Api1502Response = ApiResponse[api_1502.SuccessResponseSchema]
  type Api1870Response = ApiResponse[api_1870.SuccessResponseSchema]
}

@Singleton
class IFSBusinessDetailsConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends IFSBusinessDetailsConnector with Logging {
  private def api1171BusinessDetailsUrl(idType: IdType, idNumber: String) =
    s"${appConfig.ifsApi1171}/registration/business-details/$idType/$idNumber"

  private def businessIncomeSourcesSummaryUrl(taxYear: TaxYear, nino: Nino, businessId: BusinessId) =
    s"${appConfig.ifsBaseUrl}/income-tax/income-sources/${asTys(taxYear)}/$nino/$businessId/self-employment/biss"

  private def createBroughtForwardLossUrl(nino: Nino) =
    s"${appConfig.ifsBaseUrl}/individuals/losses/$nino/brought-forward-losses"

  private def updateBroughtForwardLossUrl(nino: Nino, lossId: String) =
    s"${appConfig.ifsBaseUrl}/individuals/losses/$nino/brought-forward-losses/$lossId/change-loss-amount"

  private def getBroughtForwardLossUrl(nino: Nino, lossId: String) =
    s"${appConfig.ifsBaseUrl}/individuals/losses/$nino/brought-forward-losses/$lossId"

  private def deleteBroughtForwardLossUrl(nino: Nino, lossId: String) =
    s"${appConfig.ifsBaseUrl}/individuals/losses/$nino/brought-forward-losses/$lossId"

  private def listBroughtForwardLossesUrl(nino: Nino, taxYear: TaxYear) =
    s"${appConfig.ifsBaseUrl}/individuals/losses/$nino/brought-forward-losses/tax-year/${taxYear.toYYYY_YY}"

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema] = {
    val url                                                                    = api1171BusinessDetailsUrl(IdType.Nino, nino.value)
    val context                                                                = appConfig.mkMetadata(IFSApiName.Api1171, url)
    implicit val reads: HttpReads[ApiResponse[api_1171.SuccessResponseSchema]] = commonReads[api_1171.SuccessResponseSchema]

    EitherT(get[Api1171Response](http, context))
  }

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse] = {
    val url     = businessIncomeSourcesSummaryUrl(taxYear, nino, businessId)
    val context = appConfig.mkMetadata(IFSApiName.Api1871, url)
    implicit val reads: HttpReads[ApiResponse[api_1871.BusinessIncomeSourcesSummaryResponse]] =
      commonReads[api_1871.BusinessIncomeSourcesSummaryResponse]

    EitherT(get[Api1871Response](http, context))
  }

  def createBroughtForwardLoss(
      data: CreateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1500.SuccessResponseSchema] = {
    val url     = createBroughtForwardLossUrl(data.nino)
    val context = appConfig.mkMetadata(IFSApiName.Api1500, url)

    EitherT(post[CreateBroughtForwardLossRequestBody, Api1500Response](http, context, data.body))
  }

  def updateBroughtForwardLoss(
      data: UpdateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1501.SuccessResponseSchema] = {
    val url     = updateBroughtForwardLossUrl(data.nino, data.lossId)
    val context = appConfig.mkMetadata(IFSApiName.Api1501, url)

    EitherT(post[UpdateBroughtForwardLossRequestBody, Api1501Response](http, context, data.body))
  }

  def getBroughtForwardLoss(nino: Nino, lossId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1502.SuccessResponseSchema] = {
    val url     = getBroughtForwardLossUrl(nino, lossId)
    val context = appConfig.mkMetadata(IFSApiName.Api1502, url)
    implicit val reads: HttpReads[ApiResponse[api_1502.SuccessResponseSchema]] =
      commonReads[api_1502.SuccessResponseSchema]

    EitherT(get[Api1502Response](http, context))
  }

  def deleteBroughtForwardLoss(nino: Nino, lossId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url                                          = deleteBroughtForwardLossUrl(nino, lossId)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1504, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonDeleteReads

    EitherT(delete(http, context))
  }

  def listBroughtForwardLosses(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1870.SuccessResponseSchema] = {
    val url     = listBroughtForwardLossesUrl(nino, taxYear)
    val context = appConfig.mkMetadata(IFSApiName.Api1870, url)
    implicit val reads: HttpReads[ApiResponse[api_1870.SuccessResponseSchema]] =
      commonReads[api_1870.SuccessResponseSchema]

    EitherT(get[Api1870Response](http, context))
  }
}
