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
import connectors.IFS.IFSConnector.{Api1786Response, Api1803Response}
import models.common.JourneyContextWithNino
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestBody
import models.domain.ApiResultT
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object MockIFSConnector {

  val mockInstance: IFSConnector = mock[IFSConnector]

  def getDisclosuresSubmission(ctx: JourneyContextWithNino)
                              (returnValue: Option[SuccessResponseAPI1639]): ScalaOngoingStubbing[ApiResultT[Option[SuccessResponseAPI1639]]] =
    when(mockInstance.getDisclosuresSubmission(eqTo(ctx))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.rightT(returnValue))

  def upsertDisclosuresSubmission(ctx: JourneyContextWithNino, data: RequestSchemaAPI1638): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.upsertDisclosuresSubmission(eqTo(ctx), eqTo(data))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.rightT(()))

  def deleteDisclosuresSubmission(ctx: JourneyContextWithNino): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.deleteDisclosuresSubmission(eqTo(ctx))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.rightT(()))

  def getAnnualSummaries(ctx: JourneyContextWithNino)(response: Api1803Response): ScalaOngoingStubbing[Future[Api1803Response]] =
    when(mockInstance.getAnnualSummaries(eqTo(ctx))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(response))

  def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino,
                                             requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody]): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.createUpdateOrDeleteApiAnnualSummaries(eqTo(ctx), eqTo(requestBody))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.rightT(()))

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(returnValue: Api1786Response): ScalaOngoingStubbing[Future[Api1786Response]] =
    when(mockInstance.getPeriodicSummaryDetail(eqTo(ctx))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(returnValue))

}