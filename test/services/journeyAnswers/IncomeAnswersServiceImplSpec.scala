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

import cats.implicits._
import connectors.IFSConnector
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.{JourneyContextWithNino, JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.database.income.IncomeStorageAnswers
import models.error.ServiceError.InvalidJsonFormatError
import models.frontend.income.IncomeJourneyAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.times
import org.mockito.MockitoSugar.{mock, never, verify}
import org.mockito.matchers.MacroBasedMatchers
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.journeyAnswers.IncomeAnswersServiceImplSpec._
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector._
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IncomeAnswersServiceImplSpec extends AnyWordSpecLike with Matchers with MacroBasedMatchers {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "getAnswers" should {
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getAnswers(journeyCtxWithNino).value.futureValue shouldBe None.asRight
    }

    "return error if cannot read IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Some(brokenJourneyAnswers))
    ) {
      val result = await(service.getAnswers(journeyCtxWithNino).value)
      val error  = result.left.value
      error shouldBe a[InvalidJsonFormatError]
    }

    "return IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Some(sampleIncomeJourneyAnswers))
    ) {
      val incomeStorageAnswers = repo.getAnswer.value.data.as[IncomeStorageAnswers]
      val result               = service.getAnswers(journeyCtxWithNino).value.futureValue
      result.value shouldBe Some(
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
    "no period summary submission exists" must {
      "successfully store data and create the period summary" in new TestCase(connector = mock[IFSConnector]) {
        connector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965EmptyResponse.asRight)

        connector.createSEPeriodSummary(*)(*, *) returns
          Future.successful(api1894SuccessResponse.asRight)

        connector.createAmendSEAnnualSubmission(*)(*, *) returns
          Future.successful(api1802SuccessResponse.asRight)

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        service.saveAnswers(ctx, answers).value.futureValue shouldBe ().asRight

        verify(connector, times(1)).createSEPeriodSummary(*)(*, *)
        verify(connector, never).amendSEPeriodSummary(*)(*, *)
      }
    }
    "a submission exists" must {
      "successfully store data and amend the period summary" in new TestCase(connector = mock[IFSConnector]) {
        connector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965MatchedResponse.asRight)

        connector.amendSEPeriodSummary(*)(*, *) returns
          Future.successful(api1895SuccessResponse.asRight)

        connector.createAmendSEAnnualSubmission(*)(*, *) returns
          Future.successful(api1802SuccessResponse.asRight)

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        service.saveAnswers(ctx, answers).value.futureValue shouldBe ().asRight

        verify(connector, times(1)).amendSEPeriodSummary(*)(*, *)
        verify(connector, never).createSEPeriodSummary(*)(*, *)
      }
    }
  }

  "saveAnswers" should {
    "save data in the repository" in new TestCase() {
      service
        .saveAnswers(journeyCtxWithNino, sampleIncomeJourneyAnswersData)
        .value
        .futureValue shouldBe ().asRight
    }
  }
}

object IncomeAnswersServiceImplSpec {
  abstract class TestCase(val repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(), val connector: IFSConnector = StubIFSConnector()) {
    val service = new IncomeAnswersServiceImpl(repo, connector)
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
