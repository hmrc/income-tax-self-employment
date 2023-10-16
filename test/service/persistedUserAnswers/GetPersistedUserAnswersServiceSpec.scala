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
import services.persistedUserAnswers.GetPersistedUserAnswersResult.{NoPersistedUserAnswersFound, PersistedUserAnswersFound}
import services.persistedUserAnswers.GetPersistedUserAnswersService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetPersistedUserAnswersServiceSpec extends AnyWordSpec with MockPersistedUserAnswersRepository with ScalaFutures {

  private val service = new GetPersistedUserAnswersService(mockPersistedUserAnswersRepository)

  private val id          = "some_id"
  private val userAnswers = PersistedUserAnswers(id)

  "GetPersistedUserAnswersService" when {
    "getting user answers tied to an id" when {
      "some user answers are found" must {
        "return the user answers wrapped in PersistedUserAnswersFound" in {
          MockPersistedUserAnswersRepository
            .get(id)
            .thenReturn(Future.successful(Some(userAnswers)))

          service.getPersistedUserAnswers(id).futureValue shouldBe PersistedUserAnswersFound(userAnswers)
        }
      }
      "no user answers are found" must {
        "return NoPersistedUserAnswersFound" in {
          MockPersistedUserAnswersRepository
            .get(id)
            .thenReturn(Future.successful(None))

          service.getPersistedUserAnswers(id).futureValue shouldBe NoPersistedUserAnswersFound
        }
      }
    }
  }

}
