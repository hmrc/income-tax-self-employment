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

package services.journeyAnswers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import cats.data.EitherT
import connectors.IFSConnector
import models.common.JourneyContextWithNino
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.domain.ApiResultT
import models.error.{DownstreamError, ServiceError}
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.journeyCtxWithNino
import utils.EitherTTestOps.convertScalaFuture

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

  val mockServiceError: ServiceError = mock[ServiceError]

  trait Setup {
    val mockIFSConnector: IFSConnector = mock[IFSConnector]
    val service                        = new CreateLossClaimService(mockIFSConnector)
    implicit val hc: HeaderCarrier     = HeaderCarrier()
  }

  "CreateLossClaimService" should {

    "return success response when IFSConnector returns success " in new Setup {
      val apiResult: ApiResultT[CreateLossClaimSuccessResponse] =
        EitherT(Future.successful(Right(successResponse): Either[DownstreamError, CreateLossClaimSuccessResponse]))

      when(mockIFSConnector.createLossClaim(any[JourneyContextWithNino], any[CreateLossClaimRequestBody])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(apiResult)

      val result: Either[ServiceError, Option[CreateLossClaimSuccessResponse]] =
        service.createLossClaimType(journeyCtxWithNino, requestBody).value.futureValue

      result shouldBe Right(Some(successResponse))
    }
  }

}
