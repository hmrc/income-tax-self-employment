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

package repositories

import config.AppConfig
import models.mdtp.PersistedUserAnswers
import org.mongodb.scala.model.Filters
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import support.MongoTestSupport
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoPersistedUserAnswersRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with MongoSupport
    with MongoTestSupport[PersistedUserAnswers]
    with Injecting
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite {

  private val now   = Instant.parse("2022-01-01T22:02:03.000Z")
  private val clock = Clock.fixed(now, ZoneOffset.UTC)

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[Clock].to(clock))
    .configure("mongodb.timeToLive" -> "2days")
    .build()

  private val appConfig   = inject[AppConfig]
  override val repository = new MongoPersistedUserAnswersRepository(mongoComponent, appConfig, clock)

  private val someOldTimestamp             = Instant.parse("2022-01-01T21:02:03.000Z")
  private val id                           = "some_id"
  private val someUserAnswers = PersistedUserAnswers(id, Json.obj("field" -> "value"), someOldTimestamp)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(removeAll())
  }

  "PersistedUserAnswersRepository" when {
    "initialised" must {
      "include an TTL index for the `lastUpdated` field (where expiry is set through the app config)" in {
        checkIndex(indexWithField("lastUpdated")) { indexModel =>
          indexModel.getOptions.getExpireAfter(TimeUnit.DAYS) shouldBe 2
        }
      }
    }
    "setting user answers" when {
      "no users answers exist for the supplied id" must {
        "store them and update the `lastUpdated` field to now" in {
          repository.set(someUserAnswers).futureValue shouldBe SetResult.UserAnswersCreated

          withClue("lastUpdated was not updated to now") {
            lastUpdated(someUserAnswers.id) shouldBe Some(now)
          }
        }
      }
      "user answers exist for the supplied id" must {
        "update them " in {
          await(repository.set(someUserAnswers)) shouldBe SetResult.UserAnswersCreated

          val updatedData        = Json.obj("field" -> "updatedValue")
          val updatedUserAnswers = someUserAnswers.copy(data = updatedData)

          await(repository.set(updatedUserAnswers)) shouldBe SetResult.UserAnswersUpdated

          repository.get(id).futureValue.map(_.data) shouldBe Some(updatedData)

          withClue("lastUpdated was not updated to now") {
            lastUpdated(updatedUserAnswers.id) shouldBe Some(now)
          }
        }
      }
    }
    "getting user answers" when {
      "no user answers exist for that id" must {
        "not be able to retrieve it and not update the `lastUpdated` field" in {
          await(repository.set(someUserAnswers)) shouldBe SetResult.UserAnswersCreated

          repository.get("some_other_id").futureValue shouldBe None
        }
      }
      "user answers exist for the supplied id" must {
        "get them and update the `lastUpdated` field" in {
          await(repository.set(someUserAnswers)) shouldBe SetResult.UserAnswersCreated

          val expectedUserAnswers = someUserAnswers.copy(lastUpdated = now)

          repository.get(id).futureValue shouldBe Some(expectedUserAnswers)
        }
      }
    }

  }

  private def lastUpdated(id: String): Option[Instant] = {
    repository.collection
      .find[PersistedUserAnswers](Filters.equal("_id", id))
      .headOption()
      .map(maybeUserAnswers => maybeUserAnswers.map(_.lastUpdated))
      .futureValue
  }

  private def removeAll(): Future[Unit] =
    repository.collection
      .deleteMany(Filters.empty())
      .toFuture()
      .map(_ => ())

}
