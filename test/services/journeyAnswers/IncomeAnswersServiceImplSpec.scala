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
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.{JourneyContextWithNino, JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.error.ServiceError.DatabaseError.InvalidJsonFormatError
import models.frontend.income.IncomeJourneyAnswers
import org.scalatest.EitherValues._
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import services.journeyAnswers.IncomeAnswersServiceImplSpec._
import stubs.connectors.StubSelfEmploymentConnector
import stubs.repositories.StubJourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class IncomeAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {
  implicit val hc = HeaderCarrier()

  "getAnswers" should {
    "return empty answers if there is no answers submitted" in new TestCase() {
      service.getAnswers(businessId, currTaxYear, mtditid).value.futureValue shouldBe None.asRight
    }

    "return error if cannot read IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Some(brokenJourneyAnswers))
    ) {
      val result = service.getAnswers(businessId, currTaxYear, mtditid).value.futureValue
      val error  = result.left.value
      error shouldBe a[InvalidJsonFormatError]
    }

    "return IncomeJourneyAnswers" in new TestCase(
      repo = StubJourneyAnswersRepository(getAnswer = Some(incomeJourneyAnswers))
    ) {
      val result = service.getAnswers(businessId, currTaxYear, mtditid).value.futureValue
      result.value shouldBe incomeJourneyAnswersData.some
    }
  }

  "saveAnswers" should {
    "save data in the repository" in new TestCase() {
      service
        .saveAnswers(JourneyContextWithNino(currTaxYear, businessId, mtditid, nino), incomeJourneyAnswersData)
        .value
        .futureValue shouldBe ().asRight
    }
  }
}

object IncomeAnswersServiceImplSpec {
  abstract class TestCase(repo: StubJourneyAnswersRepository = StubJourneyAnswersRepository(),
                          connector: SelfEmploymentConnector = StubSelfEmploymentConnector()) {
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

  val incomeJourneyAnswersData: IncomeJourneyAnswers = gens.genOne(incomeJourneyAnswersGen)

  val incomeJourneyAnswers: JourneyAnswers = brokenJourneyAnswers.copy(
    data = Json.toJson(incomeJourneyAnswersData).as[JsObject]
  )

}
