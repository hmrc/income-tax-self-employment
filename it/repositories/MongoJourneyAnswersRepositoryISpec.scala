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
import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import models.common.JourneyName._
import models.common.JourneyStatus._
import models.common._
import models.database.JourneyAnswers
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
import models.error.ServiceError
import models.frontend.TaskList
import org.mockito.MockitoSugar.when
import org.scalatest.EitherValues._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.MongoTestSupport
import utils.BaseSpec._
import utils.EitherTTestOps._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MongoJourneyAnswersRepositoryISpec extends MongoSpec with MongoTestSupport[JourneyAnswers] {
  private val now   = mkNow()
  private val clock = mkClock(now)

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.mongoTTL) thenReturn 28
  private val TTLinSeconds = mockAppConfig.mongoTTL * 3600 * 24

  override val repository = new MongoJourneyAnswersRepository(mongoComponent, mockAppConfig, clock)

  override def beforeEach(): Unit = {
    clock.reset(now)
    await(removeAll(repository.collection))
  }

  "setStatus" should {
    "set trade details journey status with trade-details businessId" in {
      val result = (for {
        _      <- repository.setStatus(tradeDetailsCtx, NotStarted)
        answer <- repository.get(tradeDetailsCtx)
      } yield answer).rightValue

      result.value shouldBe JourneyAnswers(
        tradeDetailsCtx.mtditid,
        BusinessId("trade-details"),
        tradeDetailsCtx.taxYear,
        TradeDetails,
        NotStarted,
        JsObject.empty,
        result.value.expireAt,
        result.value.createdAt,
        result.value.updatedAt
      )
    }

    "set business id for income journey" in {
      val result = (for {
        _      <- repository.setStatus(incomeCtx, NotStarted)
        answer <- repository.get(incomeCtx)
      } yield answer).rightValue

      result.value shouldBe JourneyAnswers(
        incomeCtx.mtditid,
        incomeCtx.businessId,
        incomeCtx.taxYear,
        Income,
        NotStarted,
        JsObject.empty,
        result.value.expireAt,
        result.value.createdAt,
        result.value.updatedAt
      )
    }
  }

  "getAll" should {
    "return an empty task list" in {
      val result = repository.getAll(currTaxYear, mtditid, List.empty).value.futureValue.value
      result shouldBe TaskList(None, Nil)
    }

    "return trade details without businesses" in {
      val result = (for {
        _        <- repository.setStatus(tradeDetailsCtx, NotStarted)
        taskList <- repository.getAll(tradeDetailsCtx.taxYear, tradeDetailsCtx.mtditid, Nil)
      } yield taskList).value.futureValue.value

      result shouldBe TaskList(JourneyNameAndStatus(TradeDetails, NotStarted).some, Nil)
    }

    "return task list with businesses" in {
      val businesses = List(
        aBusiness.copy(businessId = incomeCtx.businessId.value, accountingType = Some("ACCRUAL")),
        aBusiness.copy(businessId = "business2", tradingName = Some("some other business"), accountingType = Some("CASH"))
      )

      val result = (for {
        _        <- repository.setStatus(tradeDetailsCtx, CheckOurRecords)
        _        <- repository.setStatus(incomeCtx, Completed)
        _        <- repository.setStatus(incomeCtx.copy(_businessId = BusinessId("business2")), NotStarted)
        taskList <- repository.getAll(tradeDetailsCtx.taxYear, tradeDetailsCtx.mtditid, businesses)
      } yield taskList).rightValue

      result shouldBe TaskList(
        JourneyNameAndStatus(TradeDetails, CheckOurRecords).some,
        List(
          TradesJourneyStatuses(
            BusinessId(incomeCtx.businessId.value),
            TradingName("string").some,
            TypeOfBusiness("self-employment"),
            AccountingType("ACCRUAL"),
            List(JourneyNameAndStatus(Income, Completed))
          ),
          TradesJourneyStatuses(
            BusinessId("business2"),
            TradingName("some other business").some,
            TypeOfBusiness("self-employment"),
            AccountingType("CASH"),
            List(JourneyNameAndStatus(Income, NotStarted))
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
      } yield inserted.value).rightValue

      val expectedExpireAt = now.plusSeconds(TTLinSeconds)
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
        _       <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "updated"))
        updated <- repository.get(incomeCtx)
      } yield updated.value).rightValue

      val expectedExpireAt = now.plusSeconds(TTLinSeconds)
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
        _        <- repository.setStatus(incomeCtx, Completed)
        inserted <- repository.get(incomeCtx)
      } yield inserted.value).rightValue

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
      } yield inserted).rightValue

      res shouldBe None
    }
  }

  "deleteOneOrMoreJourneys" should {
    "clear specific journey in the absence of prefix" in {
      val res = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _        <- repository.deleteOneOrMoreJourneys(incomeCtx)
        inserted <- repository.get(incomeCtx)
      } yield inserted).rightValue

      res shouldBe None
    }

    "clear all journeys starting with prefix" in {
      val (incomeJourney, expensesTailoringJourney, goodsToSellOrUseJourney) = (for {
        _                 <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _                 <- repository.upsertAnswers(expensesTailoringCtx, Json.obj("field" -> "value"))
        _                 <- repository.upsertAnswers(goodsToSellOrUseCtx, Json.obj("field" -> "value"))
        _                 <- repository.deleteOneOrMoreJourneys(incomeCtx, Some("expenses-"))
        income            <- repository.get(incomeCtx)
        expensesTailoring <- repository.get(expensesTailoringCtx)
        goodsToSellOrUse  <- repository.get(goodsToSellOrUseCtx)
      } yield (income, expensesTailoring, goodsToSellOrUse)).rightValue

      incomeJourney should not be None
      expensesTailoringJourney shouldBe None
      goodsToSellOrUseJourney shouldBe None
    }
  }

  "upsertStatus" should {
    "return correct UpdateResult for insert and update" in {
      val ctx = JourneyContext(currTaxYear, businessId, mtditid, JourneyName.Income)
      val result = (for {
        beginning     <- repository.get(ctx)
        createdResult <- EitherT.right[ServiceError](repository.upsertStatus(ctx, NotStarted))
        created       <- repository.get(ctx)
        updatedResult <- EitherT.right[ServiceError](repository.upsertStatus(ctx, Completed))
        updated       <- repository.get(ctx)
      } yield (beginning, createdResult, created, updatedResult, updated)).value

      val (beginning, createdResult, created, updatedResult, updated) = result.futureValue.value
      assert(beginning === None)

      assert(createdResult.getModifiedCount == 0)
      assert(createdResult.getMatchedCount == 0)
      assert(Option(createdResult.getUpsertedId) !== None)
      assert(created.value.status === NotStarted)

      assert(updatedResult.getModifiedCount == 1)
      assert(updatedResult.getMatchedCount == 1)
      assert(Option(updatedResult.getUpsertedId) === None)
      assert(updated.value.status === Completed)
    }

  }
}
