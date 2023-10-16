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

package controllers.persistedUserAnswers

import mocks.MockGetPersistedUserAnswersService
import models.mdtp.PersistedUserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import services.persistedUserAnswers.GetPersistedUserAnswersResult.{NoPersistedUserAnswersFound, PersistedUserAnswersFound}
import utils.TestUtils

import scala.concurrent.Future

class GetPersistedUserAnswersControllerSpec extends AnyWordSpec with MockGetPersistedUserAnswersService with TestUtils {

  private val controller = new GetPersistedUserAnswersController(stubControllerComponents, mockGetPersistedUserAnswersService)

  private val id              = "some_id"
  private val someUserAnswers = PersistedUserAnswers(id)

  "GetPersistedUserAnswersController" when {
    "handling a valid request with an id" when {
      "the service returns some user answers" must {
        "return a 200 and the user answers" in {
          MockGetPersistedUserAnswersService
            .getPersistedUserAnswers(id)
            .thenReturn(Future.successful(PersistedUserAnswersFound(someUserAnswers)))

          val result: Future[Result] = controller.handleRequest(id)(fakeRequest)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe Json.toJson(someUserAnswers)
        }
      }
      "the service returns no user answers" must {
        "return a 404 NOT_FOUND" in {
          MockGetPersistedUserAnswersService
            .getPersistedUserAnswers(id)
            .thenReturn(Future.successful(NoPersistedUserAnswersFound))

          val result: Future[Result] = controller.handleRequest(id)(fakeRequest)

          status(result) shouldBe NOT_FOUND
          contentAsJson(result) shouldBe Json.obj("code" -> "NOT_FOUND", "reason" -> s"No user answers found for id: $id")
        }
      }
    }
  }

}
