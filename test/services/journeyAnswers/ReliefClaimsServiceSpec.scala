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

package services.journeyAnswers

import cats.data.EitherT
import config.AppConfig
import connectors.ReliefClaimsConnector
import models.common.JourneyName.ProfitOrLoss
import models.common._
import models.connector.{ApiResponse, ReliefClaimType}
import models.connector.ReliefClaimType.CF
import models.connector.api_1505._
import models.connector.common.{ReliefClaim, UkProperty}
import models.error.ServiceError
import models.frontend.adjustments._
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.times
import org.mockito.MockitoSugar.{verify, when}
import org.mockito.invocation.InvocationOnMock
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTTestOps.whenReady

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class ReliefClaimsServiceSpec extends AnyWordSpecLike with Matchers {

  trait ReliefClaimsServiceTestSetup {

    val mockJourneyAnswersRepository: JourneyAnswersRepository = mock[JourneyAnswersRepository]
    val mockConnector: ReliefClaimsConnector                   = mock[ReliefClaimsConnector]
    val mockAppConfig: AppConfig                               = mock[AppConfig]

    val service: ReliefClaimsService = new ReliefClaimsService(
      mockConnector,
      mockJourneyAnswersRepository,
      mockAppConfig
    )

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val taxYear2024: TaxYear       = TaxYear(2024)
    val taxYear2025: TaxYear       = TaxYear(2025)

    val businessId: BusinessId = BusinessId("XH1234567890")
    val mtditid: Mtditid       = Mtditid("12345")
    val nino: Nino             = Nino("AB123456C")

    val ctxWithNino2025: JourneyContextWithNino = JourneyContextWithNino(taxYear2025, businessId, mtditid, nino)
    val ctxWithNino2024: JourneyContextWithNino = JourneyContextWithNino(taxYear2024, businessId, mtditid, nino)

    val ctxNoNino2025: JourneyContext = ctxWithNino2025.toJourneyContext(ProfitOrLoss)
    val ctxNoNino2024: JourneyContext = ctxWithNino2024.toJourneyContext(ProfitOrLoss)

    val claim1: ReliefClaim = ReliefClaim("XH1234567890", None, CF, "2025", "claimId1", None, LocalDate.now())
    val claim2: ReliefClaim = ReliefClaim("XH1234567891", Some(UkProperty), CF, "2025", "claimId2", None, LocalDate.now())
    val claim3: ReliefClaim = ReliefClaim("XH1234567890", None, CF, "2024", "claimId3", None, LocalDate.now())

    val claims: List[ReliefClaim] = List(claim1, claim2, claim3)

    val profitOrLossJourneyAnswers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = true,
      goodsAndServicesAmount = Some(BigDecimal(1000)),
      claimLossRelief = Some(true),
      whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
      carryLossForward = Some(true),
      previousUnusedLosses = true,
      unusedLossAmount = Some(BigDecimal(500)),
      whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
    )

    val oldAnswers: List[ReliefClaim] = List(
      ReliefClaim(
        incomeSourceId = "source1",
        incomeSourceType = None,
        reliefClaimed = ReliefClaimType.CF,
        taxYearClaimedFor = "2023",
        claimId = "claim1",
        sequence = Some(1),
        submissionDate = LocalDate.now()
      )
    )

    val newAnswers: Seq[WhatDoYouWantToDoWithLoss] = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)
  }

  "cacheReliefClaims" should {

    "cache claim IDs for a tax year >= 2025" in new ReliefClaimsServiceTestSetup {
      val filteredClaimsJson: JsObject = Json.obj("claimIds" -> Json.toJson(List("claimId1")))

      when(mockConnector.getAllReliefClaims(eqTo(taxYear2025), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2025), eqTo(filteredClaimsJson)))
        .thenReturn(EitherT.right[ServiceError](Future.successful(())))

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, Some(profitOrLossJourneyAnswers)).value)

      result shouldBe Right(Some(profitOrLossJourneyAnswers))
    }

    "cache claim IDs for a tax year < 2025" in new ReliefClaimsServiceTestSetup {
      val filteredClaimsJson: JsObject = Json.obj("claimIds" -> Json.toJson(List("claimId3")))

      when(mockConnector.getAllReliefClaims(eqTo(taxYear2024), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2024), eqTo(filteredClaimsJson)))
        .thenReturn(EitherT.right[ServiceError](Future.successful(())))

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, Some(profitOrLossJourneyAnswers)).value)

      result shouldBe Right(Some(profitOrLossJourneyAnswers))
    }

    "return None when optProfitOrLoss is None" in new ReliefClaimsServiceTestSetup {
      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, None).value)

      result shouldBe Right(None)
    }

    "return existing ProfitOrLossJourneyAnswers when whatDoYouWantToDoWithLoss is not empty" in new ReliefClaimsServiceTestSetup {
      val answersWithLoss: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
        goodsAndServicesForYourOwnUse = false,
        goodsAndServicesAmount = None,
        claimLossRelief = None,
        whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
        carryLossForward = None,
        previousUnusedLosses = false,
        unusedLossAmount = None,
        whichYearIsLossReported = None
      )

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, Some(answersWithLoss)).value)

      result shouldBe Right(Some(answersWithLoss))
    }
  }

  "getAllReliefClaims" should {

    "return a list of filtered relief claims when the connector returns claims" in new ReliefClaimsServiceTestSetup {
      when(mockConnector.getAllReliefClaims(eqTo(taxYear2024), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      val result: Either[ServiceError, List[ReliefClaim]] =
        await(service.getAllReliefClaims(ctxWithNino2024).value)

      result shouldBe Right(List(claim3))
    }

    "return an error when the connector fails" in new ReliefClaimsServiceTestSetup {
      val error: ServiceError = new ServiceError {
        val errorMessage: String = "Error fetching relief claims"
      }

      when(mockConnector.getAllReliefClaims(eqTo(taxYear2025), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.leftT[Future, List[ReliefClaim]](error))

      val result: Either[ServiceError, List[ReliefClaim]] =
        await(service.getAllReliefClaims(ctxWithNino2025).value)

      result shouldBe Left(error)
    }
  }

  "createReliefClaims" should {

    "return an empty list when answers are empty" in new ReliefClaimsServiceTestSetup  {

      val emptyAnswers: List[WhatDoYouWantToDoWithLoss] = List.empty[WhatDoYouWantToDoWithLoss]

      val result: Future[Either[ServiceError, List[CreateLossClaimSuccessResponse]]] =
        service.createReliefClaims(ctxWithNino2024, emptyAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List.empty[CreateLossClaimSuccessResponse])
      }
      verify(mockConnector, times(0)).createReliefClaim(any(), any())(any(), any())
    }

    "return a list with multiple successful responses when there are multiple valid answers" in new ReliefClaimsServiceTestSetup {

      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward,
        WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
      )
      val expectedResponse1: CreateLossClaimSuccessResponse = CreateLossClaimSuccessResponse("claimId1")
      val expectedResponse2: CreateLossClaimSuccessResponse = CreateLossClaimSuccessResponse("claimId2")

      var callCount = 0

      when(mockConnector.createReliefClaim(any(), any())(any(), any()))
        .thenAnswer { _: InvocationOnMock =>
          callCount += 1
          if (callCount == 1) {
            Future.successful(Right(expectedResponse1))
          } else {
            Future.successful(Right(expectedResponse2))
          }
        }

      val result: Future[Either[ServiceError, List[CreateLossClaimSuccessResponse]]] =
        service.createReliefClaims(ctxWithNino2024, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List(expectedResponse1, expectedResponse2))
      }

      verify(mockConnector, times(2)).createReliefClaim(any(), any())(any(), any())
    }

    "return a list with one successful response when there is one valid answer" in new ReliefClaimsServiceTestSetup {

      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward
      )
      val expectedResponse: CreateLossClaimSuccessResponse = CreateLossClaimSuccessResponse("claimId1")

      when(mockConnector.createReliefClaim(any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(expectedResponse)))

      val result: Future[Either[ServiceError, List[CreateLossClaimSuccessResponse]]] =
        service.createReliefClaims(ctxWithNino2024, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List(expectedResponse))
      }

      verify(mockConnector, times(1)).createReliefClaim(any(), any())(any(), any())
    }

    "return an error when one of the answers results in a service error" in new ReliefClaimsServiceTestSetup {

      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward,
        WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
      )
      val expectedResponse: CreateLossClaimSuccessResponse = CreateLossClaimSuccessResponse("claimId1")

      val error: ServiceError = new ServiceError {
        val errorMessage: String = "Error fetching relief claims"
      }

      var callCount = 0

      when(mockConnector.createReliefClaim(any(), any())(any(), any()))
        .thenAnswer { _: InvocationOnMock =>
          callCount += 1
          if (callCount == 1) {
            Future.successful(Right(expectedResponse).asInstanceOf[ApiResponse[CreateLossClaimSuccessResponse]])
          } else {
            Future.successful(Left(error).asInstanceOf[ApiResponse[CreateLossClaimSuccessResponse]])
          }
        }

      val result: Future[Either[ServiceError, List[CreateLossClaimSuccessResponse]]] =
        service.createReliefClaims(ctxWithNino2024, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Left(error)
      }

      verify(mockConnector, times(2)).createReliefClaim(any(), any())(any(), any())
    }

    "updateReliefClaims" should {
      "return updated list of WhatDoYouWantToDoWithLoss when connector call is successful" in new ReliefClaimsServiceTestSetup {

        when(mockConnector.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers))
          .thenReturn(EitherT.right(Future.successful(List(WhatDoYouWantToDoWithLoss.CarryItForward))))

        val result: Future[Either[ServiceError, List[WhatDoYouWantToDoWithLoss]]] =
          service.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers).value

        whenReady(result) { res =>
          res shouldBe Right(List(WhatDoYouWantToDoWithLoss.CarryItForward))
        }

        verify(mockConnector, times(1)).updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers)
      }

      "return updated list with multiple WhatDoYouWantToDoWithLoss items when connector call is successful" in new ReliefClaimsServiceTestSetup {

        when(mockConnector.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers))
          .thenReturn(EitherT.right(Future.successful(List(
            WhatDoYouWantToDoWithLoss.CarryItForward,
            WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
          ))))

        val result: Future[Either[ServiceError, List[WhatDoYouWantToDoWithLoss]]] =
          service.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers).value

        whenReady(result) { res =>
          res shouldBe Right(List(
            WhatDoYouWantToDoWithLoss.CarryItForward,
            WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
          ))
        }

        verify(mockConnector, times(1)).updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers)
      }

      "return an error when connector call fails" in new ReliefClaimsServiceTestSetup {
        val error: ServiceError = new ServiceError {
          val errorMessage: String = "Error fetching relief claims"
        }

        when(mockConnector.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers))
          .thenReturn(EitherT.left(Future.successful(error)))

        val result: Future[Either[ServiceError, List[WhatDoYouWantToDoWithLoss]]] =
          service.updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers).value

        whenReady(result) { res =>
          res shouldBe Left(error)
        }

        verify(mockConnector, times(1)).updateReliefClaims(ctxWithNino2024, oldAnswers, newAnswers)
      }
    }
  }

}
