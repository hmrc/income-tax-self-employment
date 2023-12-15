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

package stubs.connectors

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import connectors.SelfEmploymentConnector._
import models.common.{IdType, JourneyContextWithNino}
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestData
import models.connector.api_1802.response.CreateAmendSEAnnualSubmissionResponse
import models.connector.{api_1171, api_1786, api_1803}
import models.connector.api_1894.request.CreateSEPeriodSummaryRequestData
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestData
import models.connector.api_1895.response.AmendSEPeriodSummaryResponse
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import stubs.connectors.StubSelfEmploymentConnector._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.OffsetDateTime
import scala.concurrent.{ExecutionContext, Future}

case class StubSelfEmploymentConnector(
    getBusinessesResult: Future[Api1171Response] = Future.successful(api1171EmptyResponse.asRight),
    createSEPeriodSummaryResult: Future[Api1894Response] = Future.successful(api1894SuccessResponse.asRight),
    amendSEPeriodSummaryResult: Future[Api1895Response] = Future.successful(api1895SuccessResponse.asRight),
    createAmendSEAnnualSubmissionResult: Future[Api1802Response] = Future.successful(api1802SuccessResponse.asRight),
    listSEPeriodSummariesResult: Future[Api1965Response] = Future.successful(api1965MatchedResponse.asRight),
    getPeriodicSummaryDetailResult: Future[Api1786Response] = Future.successful(api1786SuccessResponse.asRight),
    getAnnualSummariesResult: Future[Api1803Response] = Future.successful(api1803SuccessResponse.asRight)
) extends SelfEmploymentConnector {

  override def createSEPeriodSummary(
      data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] =
    createSEPeriodSummaryResult

  override def amendSEPeriodSummary(
      data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] =
    amendSEPeriodSummaryResult

  override def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] =
    listSEPeriodSummariesResult

  override def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] =
    createAmendSEAnnualSubmissionResult

  def getBusinesses(idType: IdType, idNumber: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1171Response] = ???

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response] =
    Future.successful(api1786SuccessResponse.asRight)

  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response] =
    Future.successful(api1803SuccessResponse.asRight)
}

object StubSelfEmploymentConnector {
  val api1171EmptyResponse: api_1171.SuccessResponseSchema =
    api_1171.SuccessResponseSchema(OffsetDateTime.now().toString, api_1171.ResponseType("safeId", "nino", "mtdid", None, false, None, None))

  val api1894SuccessResponse: CreateSEPeriodSummaryResponse = CreateSEPeriodSummaryResponse("id")

  val api1895SuccessResponse: AmendSEPeriodSummaryResponse = AmendSEPeriodSummaryResponse("id")

  val api1802SuccessResponse: CreateAmendSEAnnualSubmissionResponse = CreateAmendSEAnnualSubmissionResponse("id")

  val api1965MatchedResponse: ListSEPeriodSummariesResponse = ListSEPeriodSummariesResponse(
    Some(List(PeriodDetails(None, Some("2023-04-06"), Some("2024-04-05"), None))))

  val api1786SuccessResponse: api_1786.SuccessResponseSchema = api_1786.SuccessResponseSchema(
    fromTaxYearStr,
    toTaxYearStr,
    api_1786.FinancialsType(None, None)
  )

  val api1803SuccessResponse: api_1803.SuccessResponseSchema = api_1803.SuccessResponseSchema(None, None, None)

  val api1965EmptyResponse: ListSEPeriodSummariesResponse = ListSEPeriodSummariesResponse(Some(List.empty))
}
