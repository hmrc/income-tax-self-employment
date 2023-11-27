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

package service.journeyAnswers.expenses.goodsToSellOrUse

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentBusinessConnector
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import models.frontend.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import services.journeyAnswers.expenses.goodsToSellOrUse.SelfEmploymentBusinessService
import utils.BaseSpec

import scala.concurrent.Future

class SelfEmploymentBusinessServiceSpec extends BaseSpec {

  private val mockConnector = mock[SelfEmploymentBusinessConnector]

  private val someJourneyAnswers = GoodsToSellOrUseJourneyAnswers(100.00, Some(100.00))
  private val successResponse    = CreateSEPeriodSummaryResponse("someSubmissionId")

  private val service = new SelfEmploymentBusinessService(mockConnector)

  "SelfEmploymentBusinessService" when {
    "connector returns a success response" must {
      "evaluate to unit" in {
        mockConnector
          .createSEPeriodSummary(eqTo(requestData), eqTo(someJourneyAnswers))(*, *, *) returns Future.successful(successResponse.asRight)

        service.createSEPeriodSummary(requestData, someJourneyAnswers).futureValue shouldBe ().asRight
      }
    }
    "connector returns a downstream error" must {
      "return the error" in {
        mockConnector
          .createSEPeriodSummary(eqTo(requestData), eqTo(someJourneyAnswers))(*, *, *) returns Future.successful(someDownstreamError.asLeft)

        service.createSEPeriodSummary(requestData, someJourneyAnswers).futureValue shouldBe someDownstreamError.asLeft
      }
    }
  }

}
