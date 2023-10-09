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

import bulders.JourneyStateDataBuilder.aJourneyState
import config.AppConfig
import models.mdtp.JourneyState
import org.mockito.MockitoSugar.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[JourneyState]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val journeyState = aJourneyState

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied journey state to `now`, and save them" in {

      val expectedResult = journeyState copy (lastUpdated = LocalDate.now(stubClock))
      val setResult      = repository.set(journeyState).futureValue
      val updatedRecord  = find(Filters.equal("_id", journeyState.id)).futureValue.headOption.value

      setResult mustEqual true
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    def testGet(getArgs: String)(gettingAJourney: () => Future[Option[JourneyState]], gettingNoJourney: () => Future[Option[JourneyState]]): Unit = {
      s"for this $getArgs" - {

        "when there is a record" - {

          "must update the lastUpdated time and get the record" in {
            insert(journeyState).futureValue

            val result         = gettingAJourney().futureValue
            val expectedResult = journeyState copy (lastUpdated = LocalDate.now(stubClock))

            result.value mustEqual expectedResult
          }
        }

        "when there is no record" - {
          "must return None" in {
            gettingNoJourney().futureValue must not be defined
          }
        }
      }
    }

    behave like testGet("id")(() => repository.get(journeyState.id), () => repository.get("id that does not exist"))

    behave like testGet("taxYear, businessId and journey")(
      () => repository.get(journeyState.journeyStateData.businessId, journeyState.journeyStateData.journey, journeyState.journeyStateData.taxYear),
      () => repository.get("businessId with no journey", "non existing journey", journeyState.journeyStateData.taxYear)
    )

  }

  ".clear" - {

    "must remove a record" in {

      insert(journeyState).futureValue

      val result = repository.clear(journeyState.id).futureValue

      result mustEqual true
      repository.get(journeyState.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {

    def testKeepAlive(getArgs: String)(keepAliveMatchingJourney: () => Future[Boolean], keepAliveNotMatchingAJourney: () => Future[Boolean]): Unit = {

      s"for this $getArgs" - {

        s"when there is a record" - {

          "must update its lastUpdated to `now` and return true" in {

            insert(journeyState).futureValue

            val result = keepAliveMatchingJourney().futureValue

            val expectedUpdatedAnswers = journeyState copy (lastUpdated = LocalDate.now(stubClock))

            result mustEqual true
            val updatedAnswers = find(Filters.equal("_id", journeyState.id)).futureValue.headOption.value
            updatedAnswers mustEqual expectedUpdatedAnswers
          }
        }

        "when there is no record" - {

          "must return true" in {
            keepAliveNotMatchingAJourney().futureValue mustEqual true
          }
        }
      }
    }

    behave like testKeepAlive("id")(
      () => repository.keepAlive(journeyState.id),
      () => repository.keepAlive("id that does not exist")
    )
    behave like testKeepAlive(" businessId, journey and taxYear")(
      () =>
        repository.keepAlive(journeyState.journeyStateData.businessId, journeyState.journeyStateData.journey, journeyState.journeyStateData.taxYear),
      () => repository.keepAlive("businessId with no journey", "non existing journey", journeyState.journeyStateData.taxYear)
    )

  }

}
