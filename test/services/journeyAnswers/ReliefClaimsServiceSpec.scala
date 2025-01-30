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
import cats.implicits._
import config.AppConfig
import connectors.ReliefClaimsConnector
import models.common.JourneyName.ProfitOrLoss
import models.common._
import models.connector.ReliefClaimType.CF
import models.connector.common.{ReliefClaim, UkProperty}
import models.error.ServiceError
import models.frontend.adjustments._
import org.mockito.MockitoSugar.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec

import java.time.LocalDate
import scala.concurrent.Future

class ReliefClaimsServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures with BaseSpec {

  trait ReliefClaimsServiceTestSetup {

    val mockReliefClaimsConnector: ReliefClaimsConnector       = mock[ReliefClaimsConnector]
    val mockJourneyAnswersRepository: JourneyAnswersRepository = mock[JourneyAnswersRepository]
    val mockConnector: ReliefClaimsConnector                   = mock[ReliefClaimsConnector]
    val mockAppConfig: AppConfig                               = mock[AppConfig]

    val service: ReliefClaimsService = new ReliefClaimsService(
      mockReliefClaimsConnector,
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

    val answers: ProfitOrLossJourneyAnswers = ProfitOrLossJourneyAnswers(
      goodsAndServicesForYourOwnUse = true,
      goodsAndServicesAmount = Some(BigDecimal(1000)),
      claimLossRelief = Some(true),
      whatDoYouWantToDoWithLoss = Some(Seq(WhatDoYouWantToDoWithLoss.CarryItForward)),
      carryLossForward = Some(true),
      previousUnusedLosses = true,
      unusedLossAmount = Some(BigDecimal(500)),
      whichYearIsLossReported = Some(WhichYearIsLossReported.Year2022to2023)
    )

  }

  "cacheReliefClaims" should {

    // So those work now. I'd personally change the fact that the service returns unit so we can handle situations where something fails
    // But for now it's fine. We can pretty it up later.
    // The next bit to do then is fix the issues in ProfitOrLossAnswersService

    "cache claim IDs for a tax year >= 2025" in new ReliefClaimsServiceTestSetup {
      val filteredClaimsJson: JsObject = Json.obj("claimIds" -> Json.toJson(List("claimId1")))

      when(mockConnector.getAllReliefClaims(eqTo(taxYear2025), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2025), eqTo(filteredClaimsJson)))
        .thenReturn(EitherT.right[ServiceError](Future.successful(())))

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, Some(answers)).value)

      result mustBe Right(Some(answers))
    }

    "cache claim IDs for a tax year < 2025" in new ReliefClaimsServiceTestSetup {
      val filteredClaimsJson: JsObject = Json.obj("claimIds" -> Json.toJson(List("claimId3")))

      when(mockConnector.getAllReliefClaims(eqTo(taxYear2024), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2024), eqTo(filteredClaimsJson)))
        .thenReturn(EitherT.right[ServiceError](Future.successful(())))

      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, Some(answers)).value)

      result mustBe Right(Some(answers))
    }

    "return None when optProfitOrLoss is None" in new ReliefClaimsServiceTestSetup {
      val result: Either[ServiceError, Option[ProfitOrLossJourneyAnswers]] =
        await(service.cacheReliefClaims(ctxWithNino2025, None).value)

      result mustBe Right(None)
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

      result mustBe Right(Some(answersWithLoss))
    }
  }

  "getAllReliefClaims" should {

    "return a list of filtered relief claims when the connector returns claims" in new ReliefClaimsServiceTestSetup {
      when(mockReliefClaimsConnector.getAllReliefClaims(eqTo(taxYear2024), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT[Future, ServiceError](claims))

      val result: Either[ServiceError, List[ReliefClaim]] =
        await(service.getAllReliefClaims(ctxWithNino2024).value)

      result mustBe Right(List(claim3))
    }

//    "return an empty list when no claims match the filter criteria" in new ReliefClaimsServiceTestSetup {
//      val filteredClaimsJson: JsObject = Json.obj("claimIds3" -> Json.toJson(List("claimId10")))
//
//      when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2024), eqTo(filteredClaimsJson)))
//        .thenReturn(EitherT.right[ServiceError](Future.successful(())))
//
//      when(mockReliefClaimsConnector.getAllReliefClaims(eqTo(taxYear2024), eqTo(businessId))(any[HeaderCarrier]))
//        .thenReturn(EitherT.rightT[Future, ServiceError](List.empty))
//
//      val result: Either[ServiceError, List[ReliefClaim]] =
//        await(service.getAllReliefClaims(ctxWithNino2025).value)
//
//      result mustBe Right(List.empty)
//    }

    "return an error when the connector fails" in new ReliefClaimsServiceTestSetup {
      val error: ServiceError = new ServiceError {
        val errorMessage: String = "Error fetching relief claims"
      }

      when(mockReliefClaimsConnector.getAllReliefClaims(eqTo(taxYear2025), eqTo(businessId))(any[HeaderCarrier]))
        .thenReturn(EitherT.leftT[Future, List[ReliefClaim]](error))

      val result: Either[ServiceError, List[ReliefClaim]] =
        await(service.getAllReliefClaims(ctxWithNino2025).value)

      result mustBe Left(error)
    }

  }

}
