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

import models.common.JourneyName
import models.common.JourneyStatus._
import models.database.JourneyAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import play.api.test.Injecting
import support.MongoTestSupport
import uk.gov.hmrc.mongo.test.MongoSupport
import utils.BaseSpec._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MongoJourneyAnswersRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with MongoSupport
    with MongoTestSupport[JourneyAnswers]
    with BeforeAndAfterEach
    with GuiceOneAppPerSuite
    with OptionValues {

  private val now   = mkNow()
  private val clock = mkClock(now)

  override val repository = new MongoJourneyAnswersRepository(mongoComponent, clock)

  override def beforeEach(): Unit = {
    clock.reset(now)
    await(removeAll(repository.collection))
  }

  "upsertData" should {
    "insert a new journey answers in in-progress status and calculate dates" in {
      val result = (for {
        _        <- repository.upsertData(mtditid, currTaxYear, businessId, JourneyName.Income, Json.obj("field" -> "value"))
        inserted <- repository.get(mtditid, currTaxYear, businessId, JourneyName.Income)
      } yield inserted.value).futureValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        InProgress,
        Json.obj("field" -> "value"),
        expectedExpireAt,
        now,
        now)
    }

    "update already existing answers (values, updateAt)" in {
      val result = (for {
        _ <- repository.upsertData(mtditid, currTaxYear, businessId, JourneyName.Income, Json.obj("field" -> "value"))
        _ = clock.advanceBy(1.day)
        _       <- repository.upsertData(mtditid, currTaxYear, businessId, JourneyName.Income, Json.obj("field" -> "updated"))
        updated <- repository.get(mtditid, currTaxYear, businessId, JourneyName.Income)
      } yield updated.value).futureValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        InProgress,
        Json.obj("field" -> "updated"),
        expectedExpireAt,
        now,
        now.plus(Duration.ofDays(1))
      )
    }
  }

  "updateStatus" should {
    "update status to a new one" in {
      val result = (for {
        _ <- repository.upsertData(mtditid, currTaxYear, businessId, JourneyName.Income, Json.obj("field" -> "value"))
        _ = clock.advanceBy(2.day)
        _        <- repository.updateStatus(mtditid, currTaxYear, businessId, JourneyName.Income, Completed)
        inserted <- repository.get(mtditid, currTaxYear, businessId, JourneyName.Income)
      } yield inserted.value).futureValue

      result.status shouldBe Completed
      result.updatedAt shouldBe now.plus(Duration.ofDays(2))
    }
  }

}
