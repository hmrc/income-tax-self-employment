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

package mocks.connectors

import cats.data.EitherT
import cats.implicits._
import connectors.IFS.IFSConnector
import connectors.IFS.IFSConnector.{Api1786Response, Api1803Response, Api1894Response, Api1895Response}
import models.common.JourneyContextWithNino
import models.connector.ApiResponse
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1894.request.CreateSEPeriodSummaryRequestData
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestData
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.domain.ApiResultT
import models.error.{DownstreamError, ServiceError}
import org.scalamock.handlers.{CallHandler3, CallHandler4}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

trait MockIFSConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockIFSConnector: IFSConnector = mock[IFSConnector]

  object IFSConnectorMock {

    def getDisclosuresSubmission(ctx: JourneyContextWithNino)
                                (returnValue: Option[SuccessResponseAPI1639]): CallHandler3[JourneyContextWithNino, HeaderCarrier, ExecutionContext, ApiResultT[Option[SuccessResponseAPI1639]]] =
      (mockIFSConnector.getDisclosuresSubmission(_: JourneyContextWithNino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, *, *)
        .returning(EitherT.rightT(returnValue))

    def upsertDisclosuresSubmission(ctx: JourneyContextWithNino,
                                    data: RequestSchemaAPI1638): CallHandler4[JourneyContextWithNino, RequestSchemaAPI1638, HeaderCarrier, ExecutionContext, ApiResultT[Unit]] =
      (mockIFSConnector.upsertDisclosuresSubmission(_: JourneyContextWithNino, _: RequestSchemaAPI1638)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, data, *, *)
        .returning(EitherT.rightT(()))

    def deleteDisclosuresSubmission(ctx: JourneyContextWithNino): CallHandler3[JourneyContextWithNino, HeaderCarrier, ExecutionContext, ApiResultT[Unit]] =
      (mockIFSConnector.deleteDisclosuresSubmission(_: JourneyContextWithNino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, *, *)
        .returning(EitherT.rightT(()))

    def getAnnualSummaries(ctx: JourneyContextWithNino)
                          (response: Api1803Response): CallHandler3[JourneyContextWithNino, HeaderCarrier, ExecutionContext, Future[Api1803Response]] =
      (mockIFSConnector.getAnnualSummaries(_: JourneyContextWithNino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, *, *)
        .returning(Future.successful(response))

    def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino,
                                               requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody])
                                              (returnValue: Either[ServiceError, Unit] = Right(())): CallHandler4[JourneyContextWithNino, Option[CreateAmendSEAnnualSubmissionRequestBody], HeaderCarrier, ExecutionContext, ApiResultT[Unit]] =
      (mockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(
        _: JourneyContextWithNino,
        _: Option[CreateAmendSEAnnualSubmissionRequestBody]
      )(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, requestBody, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)
                                (returnValue: Api1786Response): CallHandler3[JourneyContextWithNino, HeaderCarrier, ExecutionContext, Future[Api1786Response]] =
      (mockIFSConnector.getPeriodicSummaryDetail(_: JourneyContextWithNino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, *, *)
        .returning(Future.successful(returnValue))

    def listSEPeriodSummary(ctx: JourneyContextWithNino)
                           (returnValue: Either[DownstreamError, Option[ListSEPeriodSummariesResponse]]): CallHandler3[JourneyContextWithNino, HeaderCarrier, ExecutionContext, Future[ApiResponse[Option[ListSEPeriodSummariesResponse]]]] =
      (mockIFSConnector.listSEPeriodSummary(_: JourneyContextWithNino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(ctx, *, *)
        .returning(Future.successful(returnValue))

    def createSEPeriodSummary(data: CreateSEPeriodSummaryRequestData)
                             (returnValue: Either[DownstreamError, Unit] = Right(())): CallHandler3[CreateSEPeriodSummaryRequestData, HeaderCarrier, ExecutionContext, Future[Api1894Response]] =
      (mockIFSConnector.createSEPeriodSummary(_: CreateSEPeriodSummaryRequestData)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(returnValue))

    def amendSEPeriodSummary(data: AmendSEPeriodSummaryRequestData)
                            (returnValue: Api1895Response): CallHandler3[AmendSEPeriodSummaryRequestData, HeaderCarrier, ExecutionContext, Future[Api1895Response]] =
      (mockIFSConnector.amendSEPeriodSummary(_: AmendSEPeriodSummaryRequestData)(_: HeaderCarrier, _: ExecutionContext))
        .expects(data, *, *)
        .returning(Future.successful(returnValue))

    def amendSEPeriodSummaryAny(returnValue: Api1895Response): CallHandler3[AmendSEPeriodSummaryRequestData, HeaderCarrier, ExecutionContext, Future[Api1895Response]] =
      (mockIFSConnector.amendSEPeriodSummary(_: AmendSEPeriodSummaryRequestData)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returning(Future.successful(returnValue))

  }
}