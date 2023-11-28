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
import models.common.TaxYear
import models.connector.api_1894.request._
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import org.mockito.IdiomaticMockito.StubbingOps
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import services.journeyAnswers.expenses.goodsToSellOrUse.SelfEmploymentBusinessService
import utils.BaseSpec

import scala.concurrent.Future

class SelfEmploymentBusinessServiceSpec extends BaseSpec {

  private val mockConnector = mock[SelfEmploymentBusinessConnector]

  private val service = new SelfEmploymentBusinessService(mockConnector)

  "SelfEmploymentBusinessService" when {
    "connector returns a success response" must {
      "evaluate to unit" in new Test {
        mockConnector
          .createSEPeriodSummary(eqTo(requestData))(*, *) returns Future.successful(successResponse.asRight)

        service.createSEPeriodSummary(requestData).futureValue shouldBe ().asRight
      }
    }
    "connector returns a downstream error" must {
      "return the error" in new Test {
        mockConnector
          .createSEPeriodSummary(eqTo(requestData))(*, *) returns Future.successful(singleDownstreamError.asLeft)

        service.createSEPeriodSummary(requestData).futureValue shouldBe singleDownstreamError.asLeft
      }
    }
  }

  trait Test {
    protected val successResponse: CreateSEPeriodSummaryResponse = CreateSEPeriodSummaryResponse("someSubmissionId")

    // Pull out somewhere as used a lot
    protected val expectedRequestBody: CreateSEPeriodSummaryRequestBody = CreateSEPeriodSummaryRequestBody(
      TaxYear.startDate(taxYear),
      TaxYear.endDate(taxYear),
      Some(
        FinancialsType(
          None,
          Some(
            DeductionsType(
              Some(SelfEmploymentDeductionsDetailPosNegType(Some(100.00), Some(100.00))),
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None))
        ))
    )

    protected val requestData: CreateSEPeriodSummaryRequestData = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, expectedRequestBody)
  }

}
