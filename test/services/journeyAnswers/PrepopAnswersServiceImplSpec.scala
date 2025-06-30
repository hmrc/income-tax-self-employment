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

import cats.implicits._
import gens.PrepopJourneyAnswersGen.annualAdjustmentsTypeGen
import gens.genOne
import mocks.connectors.MockIFSConnector
import models.connector.api_1786.IncomesType
import models.connector.api_1803.AnnualAdjustmentsType
import models.connector.{api_1786, api_1803}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.{DownstreamError, ServiceError}
import models.frontend.prepop.AdjustmentsPrepopAnswers.fromAnnualAdjustmentsType
import models.frontend.prepop.{AdjustmentsPrepopAnswers, IncomePrepopAnswers}
import org.scalatest.EitherValues._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import data.IFSConnectorTestData.{api1786EmptySuccessResponse, api1803EmptyResponse}
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global

class PrepopAnswersServiceImplSpec extends AnyWordSpecLike
  with Matchers
  with DefaultAwaitTimeout
  with MockIFSConnector {

  val service = new PrepopAnswersServiceImpl(mockIFSConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)

  "getIncomeAnswers" should {
    val filledIncomes: IncomesType = IncomesType(Some(100), Some(50), None)

    "return empty answers if there is no answers submitted" in {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(api1786EmptySuccessResponse.asRight)

      await(service.getIncomeAnswers(journeyCtxWithNino).value) shouldBe IncomePrepopAnswers(None, None).asRight
    }

    "return error if error is returned from the Connector" in {
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(downstreamError.asLeft)

      val result: Either[ServiceError, IncomePrepopAnswers] = await(service.getIncomeAnswers(journeyCtxWithNino).value)

      val error: ServiceError                               = result.left.value
      error shouldBe a[DownstreamError]
    }

    "return IncomePrepopAnswers" in {
      val periodicSummaryDetailResponse = api_1786.SuccessResponseSchema(
        currTaxYearStart, currTaxYearEnd, api_1786.FinancialsType(None, filledIncomes.some)
      ).asRight

      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(periodicSummaryDetailResponse)

      val result: Either[ServiceError, IncomePrepopAnswers] = await(service.getIncomeAnswers(journeyCtxWithNino).value)

      result.value shouldBe IncomePrepopAnswers(filledIncomes.turnover, filledIncomes.other)
    }

  }

  "getAdjustmentsAnswers" should {
    val annualAdjustmentsType: AnnualAdjustmentsType = genOne(annualAdjustmentsTypeGen)

    "return empty answers if there is no answers submitted" in {
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(api1803EmptyResponse.asRight)

      await(service.getAdjustmentsAnswers(journeyCtxWithNino).value) shouldBe AdjustmentsPrepopAnswers.emptyAnswers.asRight
    }

    "return error if error is returned from the Connector" in {
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(downstreamError.asLeft)

      val result: Either[ServiceError, AdjustmentsPrepopAnswers] = service.getAdjustmentsAnswers(journeyCtxWithNino).value.futureValue

      val error: ServiceError                                    = result.left.value
      error shouldBe a[DownstreamError]
    }

    "return IncomePrepopAnswers" in {
      val annualSummariesResponse = api_1803.SuccessResponseSchema(annualAdjustmentsType.some, None, None).asRight

      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(annualSummariesResponse)

      val result: Either[ServiceError, AdjustmentsPrepopAnswers] = await(service.getAdjustmentsAnswers(journeyCtxWithNino).value)

      val expectedAnswer: AdjustmentsPrepopAnswers = fromAnnualAdjustmentsType(annualAdjustmentsType)
      result.value shouldBe expectedAnswer
    }

  }

}

