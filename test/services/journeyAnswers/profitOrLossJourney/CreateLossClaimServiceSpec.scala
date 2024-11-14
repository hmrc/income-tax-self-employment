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

package services.journeyAnswers.profitOrLossJourney

import cats.data.EitherT
import connectors.IFSConnector
import models.common.JourneyContextWithNino
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.domain.ApiResultT
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.{DownstreamError, ServiceError}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.journeyCtxWithNino
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CreateLossClaimServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val requestBody: CreateLossClaimRequestBody = CreateLossClaimRequestBody(
    incomeSourceId = "012345678912345",
    reliefClaimed = "CF",
    taxYear = 2020
  )

  val successResponse: CreateLossClaimSuccessResponse = CreateLossClaimSuccessResponse(
    claimId = "1234568790ABCDE"
  )

  val mockServiceError: ServiceError                           = mock[ServiceError]
  val mockSingleDownstreamErrorBody: SingleDownstreamErrorBody = mock[SingleDownstreamErrorBody]

  trait Setup {
    val mockIFSConnector: IFSConnector = mock[IFSConnector]
    val service                        = new CreateLossClaimService(mockIFSConnector)
    implicit val hc: HeaderCarrier     = HeaderCarrier()
  }

  "CreateLossClaimService" should {
    "return success response" when {
      "IFSConnector successfully creates a loss claim" in new Setup {
        val apiResult: ApiResultT[CreateLossClaimSuccessResponse] =
          EitherT(Future.successful(Right(successResponse): Either[DownstreamError, CreateLossClaimSuccessResponse]))

        when(
          mockIFSConnector.createLossClaim(any[JourneyContextWithNino], any[CreateLossClaimRequestBody])(any[HeaderCarrier], any[ExecutionContext]))
          .thenReturn(apiResult)

        val result: Either[ServiceError, Option[CreateLossClaimSuccessResponse]] =
          service.createLossClaimType(journeyCtxWithNino, requestBody).value.futureValue

        result shouldBe Right(Some(successResponse))
      }
    }

    "handle DownstreamError and return ServiceError" when {
      "INVALID_TAXABLE_ENTITY_ID error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.invalidTaxableEntityId.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.invalidTaxableEntityId.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.FormatNinoError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "INVALID_PAYLOAD error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.invalidPayload.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.invalidPayload.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.InternalServerError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "INVALID_CORRELATIONID error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.invalidCorrelationId.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.invalidCorrelationId.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.InternalServerError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "DUPLICATE error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.duplicate.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.duplicate.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.RuleDuplicateSubmissionError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "ACCOUNTING_PERIOD_NOT_ENDED error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.accountingPeriodNotEnded.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.accountingPeriodNotEnded.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.RuleAccountingPeriodNotEndedError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "INVALID_CLAIM_TYPE error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.invalidClaimType.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.invalidClaimType.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.RuleTypeOfClaimInvalidError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "NO_ACCOUNTING_PERIOD error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.noAccountingPeriod.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.noAccountingPeriod.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.RuleNoAccountingPeriodError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "TAX_YEAR_NOT_SUPPORTED error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.taxYearNotSupported.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.taxYearNotSupported.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.RuleTaxYearNotSupportedError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "INCOME_SOURCE_NOT_FOUND error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.incomeSourceNotFound.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.incomeSourceNotFound.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.MatchingResourceNotFoundError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "SERVER_ERROR error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.serverError.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.serverError.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.InternalServerError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }

      "SERVICE_UNAVAILABLE error is returned by IFSConnector" in new Setup {

        when(mockSingleDownstreamErrorBody.status).thenReturn(SingleDownstreamErrorBody.serviceUnavailable.status)
        when(mockSingleDownstreamErrorBody.errorMessage).thenReturn(SingleDownstreamErrorBody.serviceUnavailable.errorMessage)

        when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
          .thenReturn(EitherT.leftT(mockSingleDownstreamErrorBody))

        val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
          service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

        result.map {
          case Left(error) => error shouldBe ServiceError.InternalServerError
          case Right(_)    => fail("Expected a ServiceError but got a success response")
        }
      }
    }

    "return ServiceUnavailableError for an unexpected error type" in new Setup {
      val unexpectedError: ServiceError = new ServiceError {
        override val errorMessage: String = "Unexpected error"
        override val status: Int          = 500
      }

      when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
        .thenReturn(EitherT.leftT(unexpectedError))

      val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
        service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

      result.map {
        case Left(error) => error shouldBe ServiceError.ServiceUnavailableError("Unexpected error occurred.")
        case Right(_)    => fail("Expected a ServiceError but got a success response")
      }
    }

    "return ServiceUnavailableError for an unexpected SingleDownstreamErrorBody" in new Setup {
      val unexpectedError: SingleDownstreamErrorBody = SingleDownstreamErrorBody("UNEXPECTED_ERROR", "500")

      when(mockIFSConnector.createLossClaim(any(), any())(any(), any()))
        .thenReturn(EitherT.leftT(unexpectedError))

      val result: Future[Either[ServiceError, Option[CreateLossClaimSuccessResponse]]] =
        service.createLossClaimType(mock[JourneyContextWithNino], requestBody).value

      result.map {
        case Left(error) => error shouldBe ServiceError.ServiceUnavailableError("Unexpected error occurred.")
        case Right(_)    => fail("Expected a ServiceError but got a success response")
      }
    }
  }

}
