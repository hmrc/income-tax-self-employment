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

package services.journeyAnswers

import bulders.BusinessDataBuilder
import cats.data.EitherT
import cats.implicits._
import connectors.IFSConnector
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.{BusinessId, JourneyContextWithNino, JourneyName, JourneyStatus, Nino}
import models.database.JourneyAnswers
import models.database.income.IncomeStorageAnswers
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import models.error.ServiceError.InvalidJsonFormatError
import models.frontend.income.IncomeJourneyAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.{reset, times, when}
import org.mockito.MockitoSugar.{mock, never, verify}
import org.mockito.matchers.MacroBasedMatchers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.{AuditService, BusinessService}
import services.journeyAnswers.IncomeAnswersServiceImplSpec._
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector._
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncomeAnswersServiceImplSpec extends AnyWordSpecLike with Matchers with MacroBasedMatchers with BeforeAndAfterEach {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    reset(mockAuditService, mockBusinessService)
    super.beforeEach()
  }

  "getAnswers" should {
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getAnswers(journeyCtxWithNino).value.futureValue shouldBe None.asRight
    }

    "return error if cannot read IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Option(brokenJourneyAnswers))
    ) {
      val result: Either[ServiceError, Option[IncomeJourneyAnswers]] = await(service.getAnswers(journeyCtxWithNino).value)
      val error: ServiceError                                        = result.left.value
      error shouldBe a[InvalidJsonFormatError]
    }

    "return IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Option(sampleIncomeJourneyAnswers))
    ) {
      val incomeStorageAnswers: IncomeStorageAnswers                 = repo.getAnswer.value.data.as[IncomeStorageAnswers]
      val result: Either[ServiceError, Option[IncomeJourneyAnswers]] = service.getAnswers(journeyCtxWithNino).value.futureValue
      result.value shouldBe Option(
        IncomeJourneyAnswers(
          incomeStorageAnswers.incomeNotCountedAsTurnover,
          None,
          BigDecimal("0"),
          incomeStorageAnswers.anyOtherIncome,
          None,
          incomeStorageAnswers.turnoverNotTaxable,
          None,
          incomeStorageAnswers.tradingAllowance,
          incomeStorageAnswers.howMuchTradingAllowance,
          None
        ))
    }
  }

  "saving income answers" when {
    "no period summary or annual submission data exists" must {
      "successfully store data and create the period summary" in new TestCase(connector = mock[IFSConnector]) {
        when(mockBusinessService.getBusiness(any[Nino], any[BusinessId])(any[HeaderCarrier])).thenReturn(EitherT.rightT(BusinessDataBuilder.aBusiness))

        connector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965EmptyResponse.asRight)

        connector.createSEPeriodSummary(*)(*, *) returns
          Future.successful(().asRight)

        connector.getAnnualSummaries(*)(*, *) returns
          Future.successful(SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound).asLeft)

        connector.createUpdateOrDeleteApiAnnualSummaries(*, *)(*, *) returns
          EitherT.rightT(())

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        service.saveAnswers(ctx, answers).value.futureValue shouldBe ().asRight

        verify(connector, times(1)).createSEPeriodSummary(*)(*, *)
        verify(auditService, times(1)).sendAuditEvent(*, *)(*, *)
        verify(mockBusinessService, times(1)).getBusiness(any[Nino], any[BusinessId])(any[HeaderCarrier])
        verify(connector, never).amendSEPeriodSummary(*)(*, *)
      }
    }

    "prior submission data exists" must {
      "successfully store data and amend the period summary" in new TestCase(connector = mock[IFSConnector]) {
        when(mockBusinessService.getBusiness(any[Nino], any[BusinessId])(any[HeaderCarrier])).thenReturn(EitherT.rightT(BusinessDataBuilder.aBusiness))

        connector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965MatchedResponse.asRight)

        connector.getPeriodicSummaryDetail(*)(*, *) returns
          Future.successful(api1786DeductionsSuccessResponse.asRight)

        connector.amendSEPeriodSummary(*)(*, *) returns Future.successful(().asRight)

        connector.getAnnualSummaries(*)(*, *) returns
          Future.successful(api1803SuccessResponse.asRight)

        connector.createUpdateOrDeleteApiAnnualSummaries(*, *)(*, *) returns
          EitherT.rightT(())

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        service.saveAnswers(ctx, answers).value.futureValue shouldBe ().asRight

        verify(connector, times(1)).amendSEPeriodSummary(*)(*, *)
        verify(auditService, times(1)).sendAuditEvent(*, *)(*, *)
        verify(mockBusinessService, times(1)).getBusiness(any[Nino], any[BusinessId])(any[HeaderCarrier])
        verify(connector, never).createSEPeriodSummary(*)(*, *)
      }
    }
  }

  "saveAnswers" should {
    "save data in the repository" in new TestCase() {
      when(mockBusinessService.getBusiness(any[Nino], any[BusinessId])(any[HeaderCarrier])).thenReturn(EitherT.rightT(BusinessDataBuilder.aBusiness))

      service
        .saveAnswers(journeyCtxWithNino, sampleIncomeJourneyAnswersData)
        .value
        .futureValue shouldBe ().asRight
    }
  }
}

object IncomeAnswersServiceImplSpec {
  val mockAuditService: AuditService = mock[AuditService]
  val mockBusinessService: BusinessService = mock[BusinessService]
  abstract class TestCase(val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(),
                          val connector: IFSConnector = StubIFSConnector(),
                          val auditService: AuditService = mockAuditService) {
    val service = new IncomeAnswersServiceImpl(repo, connector, auditService, mockBusinessService)
  }

  val brokenJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    JourneyName.Income,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )

  val sampleIncomeJourneyAnswersData: IncomeJourneyAnswers = gens.genOne(incomeJourneyAnswersGen)

  val sampleIncomeJourneyAnswers: JourneyAnswers = brokenJourneyAnswers.copy(
    data = Json.toJson(sampleIncomeJourneyAnswersData).as[JsObject]
  )

}
