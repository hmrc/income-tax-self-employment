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

package connectors

import cats.data.EitherT
import config.AppConfig
import connectors.IFSConnector._
import models.common.TaxYear.{asTys, endDate, startDate}
import models.common._
import models.connector.api_1505.{ClaimId, CreateLossClaimRequestBody}
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.connector.{ApiResponse, _}
import models.domain.ApiResultT
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

/** Keep the methods sorted by API number
  */
trait IFSConnector {
  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response]
  def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response]
  def deleteSEAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit]
  def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino, requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit]
  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response]
  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response]
  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response]
  def listSEPeriodSummary(
      ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[ApiResponse[Option[ListSEPeriodSummariesResponse]]]

  def getDisclosuresSubmission(
      ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[SuccessResponseAPI1639]]
  def upsertDisclosuresSubmission(ctx: JourneyContextWithNino, data: RequestSchemaAPI1638)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit]
  def deleteDisclosuresSubmission(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit]

  def createLossClaim(ctx: JourneyContextWithNino, request: CreateLossClaimRequestBody)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[ClaimId]

}

object IFSConnector {
  type Api1505Response = ApiResponse[api_1505.ClaimId]
  type Api1508Response = ApiResponse[api_1508.GetLossClaimSuccessResponse]
  type Api1638Response = ApiResponse[Unit]
  type Api1639Response = ApiResponseOption[SuccessResponseAPI1639]
  type Api1786Response = ApiResponse[api_1786.SuccessResponseSchema]
  type Api1802Response = ApiResponse[Unit]
  type Api1803Response = ApiResponse[api_1803.SuccessResponseSchema]
  type Api1894Response = ApiResponse[Unit]
  type Api1895Response = ApiResponse[Unit]
  type Api1965Response = ApiResponseOption[api_1965.ListSEPeriodSummariesResponse]

}

@Singleton
class IFSConnectorImpl @Inject() (http: HttpClient, appConfig: AppConfig) extends IFSConnector with Logging {

  private def baseUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${appConfig.ifsBaseUrl}/income-tax/${asTys(taxYear)}/$nino/self-employments/$incomeSourceId"

  private def annualSummariesUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/annual-summaries"
  private def periodicSummaries(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summaries"
  private def periodicSummariesFromTo(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summaries?from=${startDate(taxYear)}&to=${endDate(taxYear)}"
  private def periodicSummaryDetailUrl(nino: Nino, incomeSourceId: BusinessId, taxYear: TaxYear) =
    s"${baseUrl(nino, incomeSourceId, taxYear)}/periodic-summary-detail?from=${startDate(taxYear)}&to=${endDate(taxYear)}"

  private def disclosuresSubmissionUrl(nino: Nino, taxYear: TaxYear) =
    s"${appConfig.ifsBaseUrl}/income-tax/disclosures/$nino/${taxYear.toYYYY_YY}"

  def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] = {
    val url                                          = periodicSummaries(data.nino, data.businessId, data.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1894, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonNoBodyResponse

    post[CreateSEPeriodSummaryRequestBody, Api1894Response](http, context, data.body)
  }

  def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] = {
    val url                                          = periodicSummariesFromTo(data.nino, data.businessId, data.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1895, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonNoBodyResponse

    put[AmendSEPeriodSummaryRequestBody, Api1895Response](http, context, data.body)
  }

  def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] = {
    val url                                                                           = periodicSummaries(ctx.nino, ctx.businessId, ctx.taxYear)
    val context                                                                       = appConfig.mkMetadata(IFSApiName.Api1965, url)
    implicit val reads: HttpReads[ApiResponse[Option[ListSEPeriodSummariesResponse]]] = listSEPeriodGetReads[api_1965.ListSEPeriodSummariesResponse]

    get[Api1965Response](http, context)
  }

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response] = {
    val url     = periodicSummaryDetailUrl(ctx.nino, ctx.businessId, ctx.taxYear)
    val context = appConfig.mkMetadata(IFSApiName.Api1786, url)
    get[Api1786Response](http, context)
  }

  def getAnnualSummaries(ctx: JourneyContextWithNino)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response] = {
    val url                                                                            = annualSummariesUrl(ctx.nino, ctx.businessId, ctx.taxYear)
    val context                                                                        = appConfig.mkMetadata(IFSApiName.Api1803, url)
    implicit val reads: HttpReads[ApiResponse[Option[api_1803.SuccessResponseSchema]]] = commonGetReads[api_1803.SuccessResponseSchema]

    val result = EitherT(get[ApiResponseOption[api_1803.SuccessResponseSchema]](http, context))
    result.map(_.getOrElse(api_1803.SuccessResponseSchema.empty)).value
  }

  def createAmendSEAnnualSubmission(data: CreateAmendSEAnnualSubmissionRequestData)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] = {
    val url                                          = annualSummariesUrl(data.nino, data.businessId, data.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1802, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonNoBodyResponse

    put[CreateAmendSEAnnualSubmissionRequestBody, Api1802Response](http, context, data.body)
  }

  def deleteSEAnnualSummaries(ctx: JourneyContextWithNino)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url                                          = annualSummariesUrl(ctx.nino, ctx.businessId, ctx.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1787, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonDeleteReads

    EitherT(delete(http, context))
  }

  def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino,
                                             requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody])
                                            (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    requestBody match {
      case Some(body) =>
        val requestData = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, body)
        EitherT(createAmendSEAnnualSubmission(requestData))
      case None => deleteSEAnnualSummaries(ctx)
    }

  def getDisclosuresSubmission(ctx: JourneyContextWithNino)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[SuccessResponseAPI1639]] = {
    val url                                                                    = disclosuresSubmissionUrl(ctx.nino, ctx.taxYear)
    val context                                                                = appConfig.mkMetadata(IFSApiName.Api1639, url)
    implicit val reads: HttpReads[ApiResponse[Option[SuccessResponseAPI1639]]] = commonGetReads[SuccessResponseAPI1639]

    EitherT(get[Api1639Response](http, context))
  }

  def upsertDisclosuresSubmission(ctx: JourneyContextWithNino,
                                  data: RequestSchemaAPI1638)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url                                          = disclosuresSubmissionUrl(ctx.nino, ctx.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1638, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonNoBodyResponse

    EitherT(put[RequestSchemaAPI1638, Api1638Response](http, context, data))
  }

  def deleteDisclosuresSubmission(ctx: JourneyContextWithNino)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    val url                                          = disclosuresSubmissionUrl(ctx.nino, ctx.taxYear)
    val context                                      = appConfig.mkMetadata(IFSApiName.Api1640, url)
    implicit val reads: HttpReads[ApiResponse[Unit]] = commonDeleteReads

    EitherT(delete(http, context))
  }

  private def createLossClaimUrl(nino: Nino) =
    s"${appConfig.ifsBaseUrl}/income-tax/claims-for-relief/$nino"

  def createLossClaim(ctx: JourneyContextWithNino,
                      requestBody: CreateLossClaimRequestBody)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[ClaimId] = {
    val url                                                                    = createLossClaimUrl(ctx.nino)
    val context                                                                = appConfig.mkMetadata(IFSApiName.Api1505, url)
    implicit val reads: HttpReads[ApiResponse[ClaimId]] = lossClaimReads[ClaimId]

    EitherT(post[CreateLossClaimRequestBody, Api1505Response](http, context, requestBody))
  }

}
