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

package service.journeyAnswers

import mocks.MockJourneyAnswersRepository
import models.database.JourneyAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import repositories.SetResult.{JourneyAnswersCreated, JourneyAnswersUpdated}
import services.journeyAnswers.SetJourneyAnswersService

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SetJourneyAnswersServiceSpec extends AnyWordSpec with MockJourneyAnswersRepository with ScalaFutures {

  val service = new SetJourneyAnswersService(mockJourneyAnswersRepository)

  private val id                 = "some_id"
  private val data               = Json.obj("field" -> "value")
  private val timestamp          = Instant.parse("2022-01-01T22:02:03.000Z")
  private val someJourneyAnswers = JourneyAnswers(id, data, timestamp)

  "SetJourneyAnswersService" when {
    "journey answers have been successfully created by the repository" must {
      "return JourneyAnswersCreated" in {
        MockJourneyAnswersRepository
          .set(someJourneyAnswers)
          .thenReturn(Future.successful(JourneyAnswersCreated))

        service.setJourneyAnswers(someJourneyAnswers).futureValue shouldBe ()
      }
    }
    "journey answers have been successfully updated by the repository" must {
      "return JourneyAnswersUpdated" in {
        MockJourneyAnswersRepository
          .set(someJourneyAnswers)
          .thenReturn(Future.successful(JourneyAnswersUpdated))

        service.setJourneyAnswers(someJourneyAnswers).futureValue shouldBe ()
      }
    }

  }

}
