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

import bulders.BusinessDataBuilder.aBusiness
import cats.implicits._
import models.common.JourneyName._
import models.common.JourneyStatus._
import models.common.{BusinessId, JourneyName, TradingName}
import models.database.JourneyAnswers
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
import models.frontend.TaskList
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.MongoTestSupport
import utils.BaseSpec._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MongoJourneyAnswersRepositoryISpec extends MongoSpec with MongoTestSupport[JourneyAnswers] {
  private val now   = mkNow()
  private val clock = mkClock(now)

  override val repository = new MongoJourneyAnswersRepository(mongoComponent, clock)

  override def beforeEach(): Unit = {
    clock.reset(now)
    await(removeAll(repository.collection))
  }

  "setStatus" should {
    "set trade details journey status with trade-details businessId" in {
      val result = (for {
        _      <- repository.setStatus(tradeDetailsCtx, InProgress)
        answer <- repository.get(tradeDetailsCtx)
      } yield answer).futureValue

      result.value shouldBe JourneyAnswers(
        tradeDetailsCtx.mtditid,
        BusinessId("trade-details"),
        tradeDetailsCtx.taxYear,
        TradeDetails,
        InProgress,
        JsObject.empty,
        result.value.expireAt,
        result.value.createdAt,
        result.value.updatedAt
      )
    }

    "set business id for income journey" in {
      val result = (for {
        _      <- repository.setStatus(incomeCtx, InProgress)
        answer <- repository.get(incomeCtx)
      } yield answer).futureValue

      result.value shouldBe JourneyAnswers(
        incomeCtx.mtditid,
        incomeCtx.businessId,
        incomeCtx.taxYear,
        Income,
        InProgress,
        JsObject.empty,
        result.value.expireAt,
        result.value.createdAt,
        result.value.updatedAt
      )
    }
  }

  "getAll" should {
    "return an empty task list" in {
      val result = repository.getAll(currTaxYear, mtditid, List.empty).futureValue
      result shouldBe TaskList(None, Nil)
    }

    "return trade details without businesses" in {
      val result = (for {
        _        <- repository.setStatus(tradeDetailsCtx, InProgress)
        taskList <- repository.getAll(tradeDetailsCtx.taxYear, tradeDetailsCtx.mtditid, Nil)
      } yield taskList).futureValue

      result shouldBe TaskList(JourneyNameAndStatus(TradeDetails, InProgress).some, Nil)
    }

    "return task list with businesses" in {
      val businesses = List(
        aBusiness.copy(businessId = incomeCtx.businessId.value),
        aBusiness.copy(businessId = "business2", tradingName = Some("some other business"))
      )

      val result = (for {
        _        <- repository.setStatus(tradeDetailsCtx, CheckOurRecords)
        _        <- repository.setStatus(incomeCtx, Completed)
        _        <- repository.setStatus(incomeCtx.copy(_businessId = BusinessId("business2")), InProgress)
        taskList <- repository.getAll(tradeDetailsCtx.taxYear, tradeDetailsCtx.mtditid, businesses)
      } yield taskList).futureValue

      result shouldBe TaskList(
        JourneyNameAndStatus(TradeDetails, CheckOurRecords).some,
        List(
          TradesJourneyStatuses(
            BusinessId(incomeCtx.businessId.value),
            TradingName("string").some,
            List(JourneyNameAndStatus(Income, Completed))
          ),
          TradesJourneyStatuses(
            BusinessId("business2"),
            TradingName("some other business").some,
            List(JourneyNameAndStatus(Income, InProgress))
          )
        )
      )
    }
  }

  "upsertData + get" should {
    "insert a new journey answers in in-progress status and calculate dates" in {
      val result = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        inserted <- repository.get(incomeCtx)
      } yield inserted.value).futureValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        NotStarted,
        Json.obj("field" -> "value"),
        expectedExpireAt,
        now,
        now)
    }

    "update already existing answers (values, updateAt)" in {
      val result = (for {
        _ <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _ = clock.advanceBy(1.day)
        updatedResult <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "updated"))
        updated       <- repository.get(incomeCtx)
        _ = updatedResult.getModifiedCount shouldBe 1
      } yield updated.value).futureValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        NotStarted,
        Json.obj("field" -> "updated"),
        expectedExpireAt,
        now,
        now.plus(Duration.ofDays(1))
      )
    }
  }

  "updateStatus + get" should {
    "update status to a new one" in {
      val result = (for {
        _ <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _ = clock.advanceBy(2.day)
        updatedResult <- repository.setStatus(incomeCtx, Completed)
        inserted      <- repository.get(incomeCtx)
        _ = updatedResult.getModifiedCount shouldBe 1
      } yield inserted.value).futureValue

      result.status shouldBe Completed
      result.updatedAt shouldBe now.plus(Duration.ofDays(2))
    }
  }

  "testOnlyClearAllData" should {
    "clear all the data" in {
      val res = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _        <- repository.testOnlyClearAllData()
        inserted <- repository.get(incomeCtx)
      } yield inserted).futureValue

      res shouldBe None
    }
  }

}
