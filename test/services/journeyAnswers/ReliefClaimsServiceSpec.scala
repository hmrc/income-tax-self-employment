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
import models.connector.ReliefClaimType.CF
import models.connector.common.{ReliefClaim, UkProperty}
import models.error.{DownstreamError, ServiceError}
import org.mockito.ArgumentMatchers._
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReliefClaimsServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures {

  trait ReliefClaimsServiceTestSetup {
    val mockReliefClaimsConnector: ReliefClaimsConnector = mock[ReliefClaimsConnector]
    val mockJourneyAnswersRepository: JourneyAnswersRepository = mock[JourneyAnswersRepository]
    val mockAppConfig: AppConfig = mock[AppConfig]

    val service: ReliefClaimsService = new ReliefClaimsService(mockReliefClaimsConnector, mockJourneyAnswersRepository, mockAppConfig)

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val taxYear2024: TaxYear = TaxYear(2024)
    val taxYear2025: TaxYear = TaxYear(2025)

    val businessId: BusinessId = BusinessId("XH1234567890")
    val mtditid: Mtditid = Mtditid("12345")
    val nino: Nino = Nino("AB123456C")

    val ctxWithNino2025: JourneyContextWithNino = JourneyContextWithNino(taxYear2025, businessId, mtditid, nino)
    val ctxWithNino2024: JourneyContextWithNino = JourneyContextWithNino(taxYear2024, businessId, mtditid, nino)

    val ctxNoNino2025: JourneyContext = ctxWithNino2025.toJourneyContext(ProfitOrLoss)
    val ctxNoNino2024: JourneyContext = ctxWithNino2024.toJourneyContext(ProfitOrLoss)

    val claim1: ReliefClaim = ReliefClaim("XH1234567890", None, CF, "2025", "claimId1", None, LocalDate.now())
    val claim2: ReliefClaim = ReliefClaim("XH1234567891", Some(UkProperty), CF, "2025", "claimId2", None, LocalDate.now())
    val claim3: ReliefClaim = ReliefClaim("XH1234567890", None, CF, "2024", "claimId3", None, LocalDate.now())

    val claims: List[ReliefClaim] = List(claim1,
      claim2,
      claim3
    )
  }

  // So those work now. I'd personally change the fact that the service returns unit so we can handle situations where something fails
  // But for now it's fine. We can pretty it up later.
  // The next bit to do then is fix the issues in ProfitOrLossAnswersService

  "cache claim IDs for a tax year >= 2025" in new ReliefClaimsServiceTestSetup {
    val filteredClaimsJson = Json.obj("claimIds" -> Json.toJson(List("claimId1")))

    when(mockReliefClaimsConnector.getReliefClaimsPost2024(eqTo(taxYear2025.toString), eqTo(mtditid.value))(any()))
      .thenReturn(EitherT.right[DownstreamError](Future.successful(claims)).value)

    when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2025), eqTo(filteredClaimsJson)))
      .thenReturn(EitherT.right[ServiceError](Future.successful(())))

    val result: Either[ServiceError, Unit] = await(service.cacheReliefClaims(ctxWithNino2025, "2025").value)

    result mustBe Right(())
  }

  "cache claim IDs for a tax year < 2025" in new ReliefClaimsServiceTestSetup {
    val filteredClaimsJson = Json.obj("claimIds" -> Json.toJson(List("claimId3")))

    when(mockReliefClaimsConnector.getReliefClaims(eqTo(taxYear2024.toString), eqTo(mtditid.value))(any()))
      .thenReturn(EitherT.right[DownstreamError](Future.successful(claims)).value)

    when(mockJourneyAnswersRepository.upsertAnswers(eqTo(ctxNoNino2024), eqTo(filteredClaimsJson)))
      .thenReturn(EitherT.right[ServiceError](Future.successful(())))

    val result: Either[ServiceError, Unit] = await(service.cacheReliefClaims(ctxWithNino2024, "2024").value)

    result mustBe Right(())
  }

}