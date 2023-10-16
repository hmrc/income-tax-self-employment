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
import models.mdtp.JourneyState
import models.mdtp.JourneyState.JourneyStateData
import org.mongodb.scala.model.Filters
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import support.MongoTestSupport
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time._
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoJourneyStateRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with MongoSupport
    with MongoTestSupport[JourneyState]
    with Injecting
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite {

  private val timestampNow = Instant.parse("2022-01-01T22:02:03.000Z")
  private val localDateNow = LocalDate.of(2022, 1, 1)
  private val clock        = Clock.fixed(timestampNow, ZoneOffset.UTC)

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[Clock].toInstance(clock))
    .configure("mongodb.timeToLive" -> "2days")
    .build()

  private val appConfig   = inject[AppConfig]
  override val repository = new MongoJourneyStateRepository(mongoComponent, appConfig, clock)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(removeAll())
  }

  private val businessId = "some_business_id"
  private val taxYear    = 2024
  private val journey    = "some_journey"

  private val journeyStateData = JourneyStateData(businessId, journey, taxYear, completedState = false)

  private val someOldDate    = localDateNow.minusYears(1)
  private val journeyStateId = "some_id"

  private val someJourneyState = JourneyState(journeyStateId, journeyStateData, someOldDate)

  "MongoJourneyStateRepository" when {
    "initialised" must {
      "include a TTL index for the `lastUpdated` field (where expiry is set via the app config)" in {
        checkIndex(indexWithField("lastUpdated")) { indexModel =>
          indexModel.getOptions.getExpireAfter(TimeUnit.DAYS) shouldBe 2
        }
      }
      "include a unique compound index (called businessIdJourneyTaxYear)" in {
        checkIndex(indexByName("businessIdJourneyTaxYear")) { indexModel =>
          indexModel.getOptions.isUnique shouldBe true
        }
      }
    }
    "setting journey state" when {
      "no journey state currently exists (for the supplied businessId, taxYear and journey)" must {
        "create a journey state and update the `lastUpdated` field to now" in {
          await(repository.get(businessId, taxYear, journey)) shouldBe None

          await(repository.set(someJourneyState)) shouldBe ()

          withClue("lastUpdated was not updated to now") {
            lastUpdated(someJourneyState.id) shouldBe Some(localDateNow)
          }

        }
      }
      "journey state already exists" must {
        "update it" in {
          await(repository.set(someJourneyState)) shouldBe ()

          val updatedJourneyStateData = journeyStateData.copy(completedState = true)
          val updatedJourneyState     = someJourneyState.copy(journeyStateData = updatedJourneyStateData)

          await(repository.set(updatedJourneyState)) shouldBe ()

          repository.get(businessId, taxYear, journey).futureValue.map(_.journeyStateData) shouldBe Some(updatedJourneyStateData)
        }
      }
    }
    // Unsure on how we can test that `lastUpdated` gets updated to the current time upon retrieval (provided we want this feature).
    "getting journey state" when {
      "by businessId, taxYear, and journey" when {
        "no journey state exists" must {
          "not retrieve it" in {
            await(repository.set(someJourneyState)) shouldBe ()

            repository.get("some_other_businessId", taxYear, journey).futureValue shouldBe None
          }
        }
        "journey state exists for the supplied params" must {
          "get it and update the `lastUpdated` field" in {
            await(repository.set(someJourneyState)) shouldBe ()

            val expectedJourneyState = someJourneyState.copy(lastUpdated = localDateNow)

            repository.get(businessId, taxYear, journey).futureValue shouldBe Some(expectedJourneyState)
          }
        }
      }

      "by businessId and taxYear" when {
        "no journey state exists for the params" must {
          "not be able to retrieve it" in {
            await(repository.set(someJourneyState)) shouldBe ()

            repository.get("some_other_businessId", taxYear).futureValue shouldBe Seq.empty
          }
        }
        "journey state exists for the supplied params" must {
          "get it" in {
            await(repository.set(someJourneyState)) shouldBe ()

            val expectedJourneyState = someJourneyState.copy(lastUpdated = localDateNow)

            repository.get(businessId, taxYear).futureValue shouldBe Seq(expectedJourneyState)
          }
        }
      }
    }

  }

  private def lastUpdated(id: String): Option[LocalDate] = {
    repository.collection
      .find[JourneyState](Filters.equal("_id", id))
      .headOption()
      .map(maybeJourneyState => maybeJourneyState.map(_.lastUpdated))
      .futureValue
  }

  private def removeAll(): Future[Unit] =
    repository.collection
      .deleteMany(Filters.empty())
      .toFuture()
      .map(_ => ())

}
