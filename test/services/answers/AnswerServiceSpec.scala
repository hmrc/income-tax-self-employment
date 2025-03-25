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

package services.answers

import data.CommonTestData
import mocks.repositories.MockJourneyAnswersRepository
import models.common.JourneyName.TravelExpenses
import models.common.{JourneyContext, JourneyContextWithNino}
import models.database.JourneyAnswers
import models.database.expenses.travel.{OwnVehicles, TravelExpensesDb}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerServiceSpec extends AnyWordSpec with Matchers with CommonTestData with DefaultAwaitTimeout {

  val service                             = new AnswerService(MockJourneyAnswersRepository.mockInstance)
  val testContext: JourneyContextWithNino = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdId, testNino)
  val testJourneyContext: JourneyContext  = testContext.toJourneyContext(TravelExpenses)

  val testTravelExpenses: TravelExpensesDb = TravelExpensesDb(
    expensesToClaim = Some(Seq(OwnVehicles)),
    allowablePublicTransportExpenses = Some(BigDecimal("100.00")),
    disallowablePublicTransportExpenses = Some(BigDecimal("50.00"))
  )

  val testTravelExpensesJson: JsValue = Json.toJson(testTravelExpenses)

  val testJourneyAnswers: JourneyAnswers = testJourneyAnswers(TravelExpenses, testTravelExpensesJson)

  "getJourneyAnswers" should {
    "return None if the journey answers don't exist" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testJourneyContext, TravelExpenses)(Future.successful(None))

      val result = await(service.getJourneyAnswers[TravelExpensesDb](testContext, TravelExpenses))

      result mustBe None
    }

    "Return data in the correct format where journey answers exist" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testJourneyContext, TravelExpenses)(Future.successful(Some(testJourneyAnswers)))

      val result = await(service.getJourneyAnswers[TravelExpensesDb](testContext, TravelExpenses))

      result mustBe Some(testTravelExpenses)
    }
  }

  "upsertJourneyAnswers" should {
    "return None if the journey answers don't exist" in {
      MockJourneyAnswersRepository.upsertJourneyAnswers(testJourneyContext, testTravelExpensesJson)(Future.successful(None))

      val result = await(service.upsertJourneyAnswers(testContext, TravelExpenses, testTravelExpenses))

      result mustBe None
    }

    "Return data in the correct format where journey answers exist" in {
      val update     = testTravelExpenses.copy(allowablePublicTransportExpenses = None)
      val updateJson = Json.toJson(update)
      MockJourneyAnswersRepository.upsertJourneyAnswers(testJourneyContext, updateJson)(Future.successful(Some(updateJson)))

      val result = await(service.upsertJourneyAnswers(testContext, TravelExpenses, update))

      result mustBe Some(update)
    }
  }

  "deleteJourneyAnswers" should {
    "return true if the journey answers are deleted" in {
      MockJourneyAnswersRepository.deleteJourneyAnswers(testJourneyContext, TravelExpenses)(wasDeleted = true)

      val result = await(service.deleteJourneyAnswers(testContext, TravelExpenses))

      result mustBe true
    }

    "return false if the journey answers are not deleted" in {
      MockJourneyAnswersRepository.deleteJourneyAnswers(testJourneyContext, TravelExpenses)(wasDeleted = false)

      val result = await(service.deleteJourneyAnswers(testContext, TravelExpenses))

      result mustBe false
    }
  }

}
