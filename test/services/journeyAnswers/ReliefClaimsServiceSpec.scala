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

import config.AppConfig
import data.CommonTestData
import mocks.connectors.{MockHipReliefClaimsConnector, MockReliefClaimsConnector}
import models.common.JourneyName.ProfitOrLoss
import models.common._
import models.connector.ReliefClaimType.{CF, CSGI}
import models.connector.api_1505._
import models.connector.common.{ReliefClaim, UkProperty}
import models.error.ServiceError
import models.frontend.adjustments._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.times
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTTestOps.whenReady

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReliefClaimsServiceSpec extends AnyWordSpecLike with Matchers with CommonTestData with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val mockAppConfig: AppConfig   = mock[AppConfig]

  override def afterEach(): Unit = {
    super.afterEach()
    reset(MockReliefClaimsConnector.mockInstance)
    reset(MockHipReliefClaimsConnector.mockInstance)
    reset(mockAppConfig)
  }

  trait ReliefClaimsServiceTestSetup {

    val service: ReliefClaimsService = new ReliefClaimsService(
      MockReliefClaimsConnector.mockInstance,
      MockHipReliefClaimsConnector.mockInstance,
      mockAppConfig
    )

    val ctxNoNino2025: JourneyContext = testContextCurrentYear.toJourneyContext(ProfitOrLoss)
    val ctxNoNino2024: JourneyContext = testContextPrevYear.toJourneyContext(ProfitOrLoss)
    val submittedDate: LocalDateTime  = LocalDateTime.parse("2024-10-01T12:13:48.763")

    val seClaimId1: ClaimId      = ClaimId("claimId1")
    val propertyClaimId: ClaimId = ClaimId("claimId2")
    val seClaimId2: ClaimId      = ClaimId("claimId3")

    val seClaim1: ReliefClaim      = ReliefClaim(testBusinessId.value, None, CF, "2025", seClaimId1.claimId, None, submittedDate)
    val propertyClaim: ReliefClaim = ReliefClaim(testBusinessId.value, Option(UkProperty), CF, "2025", propertyClaimId.claimId, None, submittedDate)
    val seClaim2: ReliefClaim      = ReliefClaim(testBusinessId.value, None, CSGI, "2024", seClaimId2.claimId, None, submittedDate)

    val claims: List[ReliefClaim] = List(seClaim1, propertyClaim, seClaim2)

    val profitOrLossJourneyAnswers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = true,
      goodsAndServicesAmount = Option(BigDecimal(1000)),
      claimLossRelief = Option(true),
      whatDoYouWantToDoWithLoss = Option(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
      carryLossForward = Option(true),
      previousUnusedLosses = true,
      unusedLossAmount = Option(BigDecimal(500)),
      whichYearIsLossReported = Option(WhichYearIsLossReported.Year2022to2023)
    )

  }

  "getAllReliefClaims" should {

    "return only the claims for a specific year (current year)" in new ReliefClaimsServiceTestSetup {
      MockReliefClaimsConnector.getAllReliefClaims(testContextCurrentYear)(claims)

      val result: Either[ServiceError, List[ReliefClaim]] = await(service.getAllReliefClaims(testContextCurrentYear).value)

      result shouldBe Right(List(seClaim1))
    }

    "return only the claims for a specific year (previous year)" in new ReliefClaimsServiceTestSetup {
      MockReliefClaimsConnector.getAllReliefClaims(testContextPrevYear)(claims)

      val result: Either[ServiceError, List[ReliefClaim]] = await(service.getAllReliefClaims(testContextPrevYear).value)

      result shouldBe Right(List(seClaim2))
    }

    "return an error when the connector fails" in new ReliefClaimsServiceTestSetup {
      MockReliefClaimsConnector.getAllReliefClaimsError(testContextCurrentYear)(testServiceError)

      val result: Either[ServiceError, List[ReliefClaim]] = await(service.getAllReliefClaims(testContextCurrentYear).value)

      result shouldBe Left(testServiceError)
    }

  }

  "createReliefClaims" should {
    "return an empty list when answers are empty" in new ReliefClaimsServiceTestSetup {
      val emptyAnswers: List[WhatDoYouWantToDoWithLoss] = List.empty[WhatDoYouWantToDoWithLoss]

      val result: Future[Either[ServiceError, Seq[ClaimId]]] =
        service.createReliefClaims(testContextCurrentYear, emptyAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List.empty[ClaimId])
      }

      verify(MockReliefClaimsConnector.mockInstance, times(0)).createReliefClaim(any(), any())(any())
    }

    "return a list with multiple successful responses when there are multiple valid answers" in new ReliefClaimsServiceTestSetup {
      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward,
        WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
      )
      val expectedResponse1: ClaimId = ClaimId("claimId1")
      val expectedResponse2: ClaimId = ClaimId("claimId2")

      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, CF)(expectedResponse1)
      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, CSGI)(expectedResponse2)

      val result: Future[Either[ServiceError, Seq[ClaimId]]] =
        service.createReliefClaims(testContextCurrentYear, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List(expectedResponse1, expectedResponse2))
      }

      verify(MockReliefClaimsConnector.mockInstance, times(2)).createReliefClaim(any(), any())(any())
    }

    "return a list with one successful response when there is one valid answer" in new ReliefClaimsServiceTestSetup {
      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward
      )
      val expectedResponse: ClaimId = ClaimId("claimId1")

      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, CF)(expectedResponse)

      val result: Future[Either[ServiceError, Seq[ClaimId]]] =
        service.createReliefClaims(testContextCurrentYear, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(List(expectedResponse))
      }

      verify(MockReliefClaimsConnector.mockInstance, times(1)).createReliefClaim(any(), any())(any())
    }

    "return an error when one of the answers results in a service error" in new ReliefClaimsServiceTestSetup {
      val validAnswers: List[WhatDoYouWantToDoWithLoss] = List(
        WhatDoYouWantToDoWithLoss.CarryItForward,
        WhatDoYouWantToDoWithLoss.DeductFromOtherTypes
      )
      val expectedResponse: ClaimId = ClaimId("claimId1")

      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, CF)(expectedResponse)
      MockReliefClaimsConnector.createReliefClaimError(testContextCurrentYear, CSGI)(testServiceError)

      val result: Future[Either[ServiceError, Seq[ClaimId]]] =
        service.createReliefClaims(testContextCurrentYear, validAnswers).value

      whenReady(result) { res =>
        res shouldBe Left(testServiceError)
      }

      verify(MockReliefClaimsConnector.mockInstance, times(2)).createReliefClaim(any(), any())(any())
    }
  }

  "updateReliefClaims" should {

    "Make a single delete call if the user un-checks one answer" in new ReliefClaimsServiceTestSetup {
      val newAnswers: Seq[WhatDoYouWantToDoWithLoss] = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)

      MockReliefClaimsConnector.deleteReliefClaim(testContextCurrentYear, seClaimId2.claimId)
      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, seClaim1.reliefClaimed)(seClaimId1)

      val result: Future[Either[ServiceError, UpdateReliefClaimsResponse]] =
        service.updateReliefClaims(testContextCurrentYear, claims, newAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(
          UpdateReliefClaimsResponse(
            created = List(WhatDoYouWantToDoWithLoss.CarryItForward),
            deleted = List(WhatDoYouWantToDoWithLoss.DeductFromOtherTypes)
          ))
      }

      verify(MockReliefClaimsConnector.mockInstance, times(1)).deleteReliefClaim(any(), any())(any())
      verify(MockReliefClaimsConnector.mockInstance, times(1)).createReliefClaim(any(), any())(any())
    }

    "Make a single delete call if the user un-checks one answer when hip enabled" in new ReliefClaimsServiceTestSetup {
      val newAnswers: Seq[WhatDoYouWantToDoWithLoss] = Seq(WhatDoYouWantToDoWithLoss.CarryItForward)

      when(mockAppConfig.hipMigration1509Enabled).thenReturn(true)
      when(mockAppConfig.hipMigration1505Enabled).thenReturn(true)

      MockHipReliefClaimsConnector.deleteReliefClaim(testContextCurrentYear, seClaimId2.claimId)
      MockHipReliefClaimsConnector.createReliefClaim(testContextCurrentYear, seClaim1.reliefClaimed)(seClaimId1)

      val result: Future[Either[ServiceError, UpdateReliefClaimsResponse]] =
        service.updateReliefClaims(testContextCurrentYear, claims, newAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(
          UpdateReliefClaimsResponse(
            created = List(WhatDoYouWantToDoWithLoss.CarryItForward),
            deleted = List(WhatDoYouWantToDoWithLoss.DeductFromOtherTypes)
          ))
      }

      verify(MockHipReliefClaimsConnector.mockInstance, times(1)).deleteReliefClaim(any(), any())(any(), any())
      verify(MockHipReliefClaimsConnector.mockInstance, times(1)).createReliefClaim(any(), any())(any())
    }

    "Make two create calls if the user checks both answers" in new ReliefClaimsServiceTestSetup {
      val newAnswers: Seq[WhatDoYouWantToDoWithLoss] = Seq(WhatDoYouWantToDoWithLoss.CarryItForward, WhatDoYouWantToDoWithLoss.DeductFromOtherTypes)

      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, seClaim1.reliefClaimed)(seClaimId1)
      MockReliefClaimsConnector.createReliefClaim(testContextCurrentYear, seClaim2.reliefClaimed)(seClaimId2)

      val result: Future[Either[ServiceError, UpdateReliefClaimsResponse]] =
        service.updateReliefClaims(testContextCurrentYear, claims, newAnswers).value

      whenReady(result) { res =>
        res shouldBe Right(
          UpdateReliefClaimsResponse(
            created = List(WhatDoYouWantToDoWithLoss.CarryItForward, WhatDoYouWantToDoWithLoss.DeductFromOtherTypes),
            deleted = List()
          ))
      }

      verify(MockReliefClaimsConnector.mockInstance, times(0)).deleteReliefClaim(any(), any())(any())
      verify(MockReliefClaimsConnector.mockInstance, times(2)).createReliefClaim(any(), any())(any())
    }

    "return an error when connector call fails" in new ReliefClaimsServiceTestSetup {
      val newAnswers: Seq[WhatDoYouWantToDoWithLoss] = Seq(WhatDoYouWantToDoWithLoss.CarryItForward, WhatDoYouWantToDoWithLoss.DeductFromOtherTypes)

      val testServiceError: ServiceError = new ServiceError {
        val errorMessage: String = "Error fetching relief claims"
      }

      MockReliefClaimsConnector.createReliefClaimError(testContextCurrentYear, seClaim1.reliefClaimed)(testServiceError)

      val result: Future[Either[ServiceError, UpdateReliefClaimsResponse]] =
        service.updateReliefClaims(testContextCurrentYear, claims, newAnswers).value

      whenReady(result) { res =>
        res shouldBe Left(testServiceError)
      }

      verify(MockReliefClaimsConnector.mockInstance, times(2)).createReliefClaim(any(), any())(any())
    }
  }

  "delete relief claims" should {
    "return success when claims list is empty" in new ReliefClaimsServiceTestSetup {
      when(mockAppConfig.hipMigration1509Enabled).thenReturn(true)

      override val claims: List[ReliefClaim] = List(seClaim1).empty

      MockReliefClaimsConnector.deleteReliefClaim(testContextCurrentYear, seClaimId1.claimId)

      val result: Either[ServiceError, Unit] = await(service.deleteReliefClaims(testContextCurrentYear, claims).value)

      result shouldBe Right(())
    }

    "return success via ifs connector when hipMigration1509Enable is disabled" in new ReliefClaimsServiceTestSetup {
      when(mockAppConfig.hipMigration1509Enabled).thenReturn(false)

      override val claims: List[ReliefClaim] = List(seClaim1)

      MockReliefClaimsConnector.deleteReliefClaim(testContextCurrentYear, seClaimId1.claimId)

      val result: Either[ServiceError, Unit] = await(service.deleteReliefClaims(testContextCurrentYear, claims).value)

      result shouldBe Right(())
    }

    "return success via hip connector when hipMigration1509Enable is true" in new ReliefClaimsServiceTestSetup {
      override val claims: List[ReliefClaim] = List(seClaim1)

      when(mockAppConfig.hipMigration1509Enabled).thenReturn(true)

      MockHipReliefClaimsConnector.deleteReliefClaim(testContextCurrentYear, seClaimId1.claimId)

      val result: Either[ServiceError, Unit] = await(service.deleteReliefClaims(testContextCurrentYear, claims).value)

      result shouldBe Right(())
    }
  }

}
