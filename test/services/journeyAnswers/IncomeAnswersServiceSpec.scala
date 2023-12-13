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

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentConnector
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.JourneyContextWithNino
import models.frontend.income.IncomeJourneyAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.mockito.Mockito.times
import org.mockito.MockitoSugar.{never, verify}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.connectors.StubSelfEmploymentConnector._
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec
import utils.BaseSpec._

import scala.concurrent.Future

class IncomeAnswersServiceSpec extends BaseSpec {

  trait Test {
    val mockConnector: SelfEmploymentConnector   = mock[SelfEmploymentConnector]
    val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository()

    val service = new IncomeAnswersServiceImpl(repository, mockConnector)

  }

  "saving income answers" when {
    "no period summary submission exists" must {
      "successfully store data and create the period summary" in new Test {
        mockConnector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965EmptyResponse.asRight)

        mockConnector.createSEPeriodSummary(*)(*, *) returns
          Future.successful(api1894SuccessResponse.asRight)

        mockConnector.createAmendSEAnnualSubmission(*)(*, *) returns
          Future.successful(api1802SuccessResponse.asRight)

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        await(service.saveAnswers(ctx, answers).value) shouldBe ().asRight

        verify(mockConnector, times(1)).createSEPeriodSummary(*)(*, *)
        verify(mockConnector, never).amendSEPeriodSummary(*)(*, *)
      }
    }
    "a submission exists" must {
      "successfully store data and amend the period summary" in new Test {
        mockConnector.listSEPeriodSummary(*)(*, *) returns
          Future.successful(api1965MatchedResponse.asRight)

        mockConnector.amendSEPeriodSummary(*)(*, *) returns
          Future.successful(api1895SuccessResponse.asRight)

        mockConnector.createAmendSEAnnualSubmission(*)(*, *) returns
          Future.successful(api1802SuccessResponse.asRight)

        val answers: IncomeJourneyAnswers = incomeJourneyAnswersGen.sample.get
        val ctx: JourneyContextWithNino   = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino)

        await(service.saveAnswers(ctx, answers).value) shouldBe ().asRight

        verify(mockConnector, times(1)).amendSEPeriodSummary(*)(*, *)
        verify(mockConnector, never).createSEPeriodSummary(*)(*, *)
      }
    }
  }

}
