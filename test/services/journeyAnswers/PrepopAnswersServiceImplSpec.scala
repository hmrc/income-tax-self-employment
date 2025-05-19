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
import connectors.IFS.IFSConnector
import gens.PrepopJourneyAnswersGen.annualAdjustmentsTypeGen
import gens.genOne
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
import services.journeyAnswers.PrepopAnswersServiceImplSpec._
import stubs.connectors.StubIFSConnector
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrepopAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)

  "getIncomeAnswers" should {
    val filledIncomes: IncomesType = IncomesType(Some(100), Some(50), None)
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getIncomeAnswers(journeyCtxWithNino).value.futureValue shouldBe IncomePrepopAnswers(None, None).asRight
    }

    "return error if error is returned from the Connector" in new TestCase(connector =
      StubIFSConnector(getPeriodicSummaryDetailResult = Future(downstreamError.asLeft))) {
      val result: Either[ServiceError, IncomePrepopAnswers] = service.getIncomeAnswers(journeyCtxWithNino).value.futureValue
      val error: ServiceError                               = result.left.value
      error shouldBe a[DownstreamError]
    }

    "return IncomePrepopAnswers" in new TestCase(connector = StubIFSConnector(getPeriodicSummaryDetailResult = Future.successful(
      api_1786.SuccessResponseSchema(currTaxYearStart, currTaxYearEnd, api_1786.FinancialsType(None, filledIncomes.some)).asRight))) {
      val result: Either[ServiceError, IncomePrepopAnswers] = service.getIncomeAnswers(journeyCtxWithNino).value.futureValue
      result.value shouldBe IncomePrepopAnswers(filledIncomes.turnover, filledIncomes.other)
    }
  }

  "getAdjustmentsAnswers" should {
    val annualAdjustmentsType: AnnualAdjustmentsType = genOne(annualAdjustmentsTypeGen)
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getAdjustmentsAnswers(journeyCtxWithNino).value.futureValue shouldBe AdjustmentsPrepopAnswers.emptyAnswers.asRight
    }

    "return error if error is returned from the Connector" in new TestCase(connector =
      StubIFSConnector(getAnnualSummariesResult = downstreamError.asLeft)) {
      val result: Either[ServiceError, AdjustmentsPrepopAnswers] = service.getAdjustmentsAnswers(journeyCtxWithNino).value.futureValue
      val error: ServiceError                                    = result.left.value
      error shouldBe a[DownstreamError]
    }

    "return IncomePrepopAnswers" in new TestCase(connector =
      StubIFSConnector(getAnnualSummariesResult = api_1803.SuccessResponseSchema(annualAdjustmentsType.some, None, None).asRight)) {
      val result: Either[ServiceError, AdjustmentsPrepopAnswers] =
        service.getAdjustmentsAnswers(journeyCtxWithNino).value.futureValue
      val expectedAnswer: AdjustmentsPrepopAnswers = fromAnnualAdjustmentsType(annualAdjustmentsType)
      result.value shouldBe expectedAnswer
    }
  }
}

object PrepopAnswersServiceImplSpec {
  abstract class TestCase(val connector: IFSConnector = StubIFSConnector()) {
    val service = new PrepopAnswersServiceImpl(connector)
  }
}
