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

import mocks.MockSetPersistedUserAnswersService
import models.mdtp.PersistedUserAnswers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import utils.TestUtils

import java.time._
import scala.concurrent.Future

class SetPersistedUserAnswersControllerSpec extends TestUtils with MockSetPersistedUserAnswersService with GuiceOneAppPerSuite {

  private val controller = new SetPersistedUserAnswersController(stubControllerComponents, mockSetPersistedUserAnswersService)

  private val id              = "some_id"
  private val data            = Json.obj("field" -> "value")
  private val timestamp       = Instant.parse("2022-01-01T22:02:03.000Z")
  private val someUserAnswers = PersistedUserAnswers(id, data, timestamp)

  private val validRequestJson   = Json.toJson(someUserAnswers)
  private val invalidRequestJson = validRequestJson.as[JsObject] - "lastUpdated"

  "SetPersistedUserAnswersController" when {
    "handling a request where the request json can be read as PersistedUserAnswers" when {
      "the service returns a successful future" must {
        "return NO_CONTENT" in {
          MockSetPersistedUserAnswersService
            .setPersistedUserAnswers(someUserAnswers)
            .thenReturn(Future.successful(()))

          val result: Future[Result] = controller.handleRequest()(fakeRequest.withBody(validRequestJson))
          status(result) shouldBe NO_CONTENT
        }
      }
    }
    "handling a request where the request json does not conform to PersistedUserAnswers reads" must {
      "return BAD_REQUEST" in {
        val result: Future[Result] = controller.handleRequest()(fakeRequest.withBody(invalidRequestJson))

        status(result) shouldBe BAD_REQUEST
        contentAsJson(result) shouldBe Json.obj(
          "code"   -> "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
          "reason" -> "An empty or non-matching body was submitted")
      }
    }
  }

}
