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

package service

import bulders.BusinessDataBuilder.{aBusiness, aGetBusinessDataResponse, aTaxPayerDisplayResponse, aTradesJourneyStatusesSeq}
import bulders.JourneyStateDataBuilder.aJourneyState
import connectors.SelfEmploymentConnector
import connectors.SelfEmploymentConnector.Api1171Response
import models.common.IdType
import models.common.IdType.Nino
import models.database.JourneyState
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.error.ServiceError.DatabaseError.MongoError
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import repositories.MongoJourneyStateRepository
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BusinessServiceSpec extends TestUtils {
  val mockBusinessConnector = mock[SelfEmploymentConnector]
  val mockSessionRepository = MockitoSugar.mock[MongoJourneyStateRepository]

  lazy val service = new BusinessService(mockBusinessConnector, mockSessionRepository)
  val nino         = aTaxPayerDisplayResponse.nino
  val businessId   = aBusiness.businessId
  val taxYear      = LocalDate.now.getYear

  for ((getMethodName, svcMethod) <- Seq(
      ("getBusinesses", () => service.getBusinesses(nino)),
      ("getBusiness", () => service.getBusiness(nino, businessId))
    ))
    s"$getMethodName" should { // scalastyle:off magic.number
      val expectedRight = Right(Seq(aBusiness))
      behave like rightResponse(svcMethod, expectedRight, () => stubConnectorGetBusiness(Right(aGetBusinessDataResponse)))

      val expectedLeft = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))
      behave like leftResponse(svcMethod, expectedLeft, () => stubConnectorGetBusiness(expectedLeft))
    }

  "getBusinessesJourneyStates" should {
    behave like rightResponse(
      () => service.getBusinessJourneyStates(nino, taxYear),
      Right(aTradesJourneyStatusesSeq),
      () => {
        stubConnectorGetBusiness(Right(aGetBusinessDataResponse))
        stubSessionRepositoryGetSeq(Right(Seq(aJourneyState)))
      }
    )

    val connectorResult = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))
    behave like leftResponse(
      () => service.getBusinessJourneyStates(nino, taxYear),
      connectorResult,
      () => stubConnectorGetBusiness(connectorResult),
      "Connector problems"
    )

    val sessionRepoResult = Left(MongoError("db error"))
    behave like leftResponse(
      () => service.getBusinessJourneyStates(nino, taxYear),
      sessionRepoResult,
      () => {
        stubConnectorGetBusiness(Right(aGetBusinessDataResponse))
        stubSessionRepositoryGetSeq(sessionRepoResult)
      },
      "Repository problems"
    )
  }

  def rightResponse[A](svcMethod: () => Future[A], expectedResult: A, stubs: () => Unit): Unit =
    "return a Right with GetBusinessDataRequest model" in {
      stubs()
      await(svcMethod()) mustBe expectedResult
    }

  def leftResponse[A](svcMethod: () => Future[A], expectedResult: A, stubs: () => Unit, remark: String = ""): Unit =
    s"$remark error - return a Left when $remark returns an error" in {
      stubs()
      await(svcMethod()) mustBe expectedResult
    }

  private def stubConnectorGetBusiness(expectedResult: Api1171Response): Unit = {
    (mockBusinessConnector
      .getBusinesses(_: IdType, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(Nino, nino, *, *)
      .returning(Future.successful(expectedResult))
    ()
  }

  type GetJourneyStatesResponse = Either[ServiceError, Seq[JourneyState]]

  private def stubSessionRepositoryGetSeq(expectedResult: GetJourneyStatesResponse): Unit = {
    when(mockSessionRepository.get(any, meq(taxYear))) thenReturn (expectedResult match {
      case Right(journeyStates)    => Future.successful(journeyStates)
      case Left(MongoError(error)) => Future.failed(new RuntimeException(error))
      case Left(error)             => Future.failed(new RuntimeException(error.errorMessage))
    })
    ()
  }

}
