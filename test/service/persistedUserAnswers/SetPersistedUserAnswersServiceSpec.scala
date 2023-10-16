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

package service.persistedUserAnswers

import mocks.MockPersistedUserAnswersRepository
import models.mdtp.PersistedUserAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import services.persistedUserAnswers.SetPersistedUserAnswersService

import java.time.Instant
import scala.concurrent.Future

class SetPersistedUserAnswersServiceSpec extends AnyWordSpec with MockPersistedUserAnswersRepository with ScalaFutures {

  val service = new SetPersistedUserAnswersService(mockPersistedUserAnswersRepository)

  private val id              = "some_id"
  private val data            = Json.obj("field" -> "value")
  private val timestamp       = Instant.parse("2022-01-01T22:02:03.000Z")
  private val someUserAnswers = PersistedUserAnswers(id, data, timestamp)

  "SetPersistedUserAnswersService" when {
    "user answers have been successfully set by the repository" must {
      "return unit" in {
        MockPersistedUserAnswersRepository
          .set(someUserAnswers)
          .thenReturn(Future.successful(()))

        service.setPersistedUserAnswers(someUserAnswers).futureValue shouldBe ()
      }
    }

  }

}
