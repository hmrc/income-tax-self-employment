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
import connectors.SelfEmploymentConnector
import gens.IncomeJourneyAnswersGen.incomePrepopAnswersGen
import models.common.{JourneyName, JourneyStatus}
import models.connector.api_1786
import models.connector.api_1786.IncomesType
import models.database.JourneyAnswers
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.{DownstreamError, ServiceError}
import models.frontend.prepop.IncomePrepopAnswers
import org.mockito.matchers.MacroBasedMatchers
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import services.journeyAnswers.PrepopAnswersServiceImplSpec._
import stubs.connectors.StubSelfEmploymentConnector
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._
import utils.EitherTTestOps.convertScalaFuture

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PrepopAnswersServiceImplSpec extends AnyWordSpecLike with Matchers with MacroBasedMatchers {
  implicit val hc: HeaderCarrier = HeaderCarrier()
  private val downstreamError    = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)

  "getIncomeAnswers" should {
    val filledIncomes: IncomesType = IncomesType(Some(100), Some(50), None)
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getIncomeAnswers(journeyCtxWithNino).value.futureValue shouldBe IncomePrepopAnswers(None, None).asRight
    }

    "return error if error is returned from the Connector" in new TestCase(connector =
      StubSelfEmploymentConnector(getPeriodicSummaryDetailResult = Future(downstreamError.asLeft))) {
      val result: Either[ServiceError, IncomePrepopAnswers] = service.getIncomeAnswers(journeyCtxWithNino).value.futureValue
      val error: ServiceError                               = result.left.value
      error shouldBe a[DownstreamError]
    }

    "return IncomePrepopAnswers" in new TestCase(connector = StubSelfEmploymentConnector(getPeriodicSummaryDetailResult = Future.successful(
      api_1786.SuccessResponseSchema(currTaxYearStart, currTaxYearEnd, api_1786.FinancialsType(None, filledIncomes.some)).asRight))) {
      val result: Either[ServiceError, IncomePrepopAnswers] = service.getIncomeAnswers(journeyCtxWithNino).value.futureValue
      result.value shouldBe IncomePrepopAnswers(filledIncomes.turnover, filledIncomes.other)
    }
  }
}

object PrepopAnswersServiceImplSpec {
  abstract class TestCase(val connector: SelfEmploymentConnector = StubSelfEmploymentConnector()) {
    val service = new PrepopAnswersServiceImpl(connector)
  }

  def getJourneyAnswers(journey: JourneyName): JourneyAnswers = JourneyAnswers(
    mtditid,
    businessId,
    currTaxYear,
    journey,
    JourneyStatus.Completed,
    JsObject.empty,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )

  val sampleIncomePrepopAnswers: JourneyAnswers = getJourneyAnswers(JourneyName.IncomePrepop).copy(
    data = Json.toJson(gens.genOne(incomePrepopAnswersGen)).as[JsObject]
  )

}
