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

package controllers.journeyAnswers

import models.mdtp.JourneyAnswers
import org.mongodb.scala.model.Filters
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import repositories.MongoJourneyAnswersRepository
import utils.IntegrationBaseSpec

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.Future

class GetJourneyAnswersControllerISpec extends IntegrationBaseSpec with Injecting with BeforeAndAfterEach with GuiceOneAppPerSuite {

  private val timestamp = Instant.parse("2022-01-01T22:02:03.000Z")
  private val clock     = Clock.fixed(timestamp, ZoneOffset.UTC)

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[Clock].toInstance(clock))
    .build()

  private val repository = inject[MongoJourneyAnswersRepository]

  private val id   = "some_id"
  private val data = Json.obj("field" -> "value")

  def request(): WSRequest = buildClient(s"income-tax-self-employment/check-your-answers/get/$id")

  override def beforeEach(): Unit = await(removeAll())

  "GetJourneyAnswersController" when {
    "journey answers exist for the requested id" must {
      "return Ok and the answers as json" in {
        val someJourneyAnswers = JourneyAnswers(id, data, timestamp)

        createJourneyAnswers(someJourneyAnswers) shouldBe ()

        val response: WSResponse = await(request().get())

        response.status shouldBe 200
        response.json shouldBe Json.toJson(someJourneyAnswers)
      }
    }
    "no journey answers exist for the requested id" must {
      "return NOT_FOUND" in {
        val someOtherId         = "some_other_id"
        val otherJourneyAnswers = JourneyAnswers(someOtherId, data, timestamp)

        createJourneyAnswers(otherJourneyAnswers) shouldBe ()

        val response: WSResponse = await(request().get())

        response.status shouldBe 404
        response.json shouldBe Json.obj("code" -> "NOT_FOUND", "reason" -> s"No journey answers found for id: $id")
      }
    }
  }

  private def createJourneyAnswers(answers: JourneyAnswers): Unit =
    await(repository.set(answers))

  private def removeAll(): Future[Unit] =
    repository.collection
      .deleteMany(Filters.empty())
      .toFuture()
      .map(_ => ())

}
