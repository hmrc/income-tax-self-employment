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

import models.mdtp.PersistedUserAnswers
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import repositories.MongoPersistedUserAnswersRepository
import utils.IntegrationBaseSpec

import java.time.{Clock, Instant, ZoneOffset}

class SetPersistedUserAnswersControllerISpec extends IntegrationBaseSpec with Injecting {

  private val timestamp = Instant.parse("2022-01-01T22:02:03.000Z")
  private val clock     = Clock.fixed(timestamp, ZoneOffset.UTC)

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[Clock].toInstance(clock))
    .build()

  private val repository = inject[MongoPersistedUserAnswersRepository]

  private val id          = "some_id"
  private val data        = Json.obj("field" -> "value")
  private val userAnswers = PersistedUserAnswers(id, data, timestamp)

  private val requestBody = Json.toJson(userAnswers)

  def request(): WSRequest =
    buildClient(s"income-tax-self-employment/check-your-answers/set").withBody(requestBody)

  "SetPersistedUserAnswersController" when {
    "receiving request json that can be read as PersistedUserAnswers" must {
      "create or update user answers and return a 204" in {
        val response = await(request().put(requestBody))

        repository.get(userAnswers.id).futureValue shouldBe Some(userAnswers)

        response.status shouldBe 204
      }
    }
  }

}
