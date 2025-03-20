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
import models.database.expenses.travel.TravelExpensesDb
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
import models.error.ServiceError
import models.frontend.TaskList
import org.mockito.MockitoSugar.{reset, when}
import org.scalatest.EitherValues._
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.await
import support.MongoTestSupport
import utils.BaseSpec._
import utils.EitherTTestOps._

import java.time.{Clock, Duration}
import scala.concurrent.ExecutionContext.Implicits.global

class MongoJourneyAnswersRepositoryISpec extends MongoSpec with MongoTestSupport[JourneyAnswers] {

  private val mockAppConfig = mock[AppConfig]
  private val mockClock     = mock[Clock]

  when(mockAppConfig.mongoTTL) thenReturn 30

  private val TTLinSeconds = mockAppConfig.mongoTTL * 3600 * 24

  override val repository                           = new MongoJourneyAnswersRepository(mongoComponent, mockAppConfig, mockClock)
  override val mongo: MongoJourneyAnswersRepository = repository // Required by JourneyAnswersHelper

  override def beforeEach(): Unit = {
    reset(mockClock)
    await(removeAll(repository.collection))
  }

  "getJourneyAnswers" should {
    "return Some(data) when answers exist for the journey" in {
      val ctx     = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)
      val answers = TravelExpensesDb(allowablePublicTransportExpenses = Some(BigDecimal("1")))
      DbHelper.insertOne(TravelExpenses, answers)

      val result         = await(repository.getJourneyAnswers(ctx))
      val expectedResult = testBaseJourneyAnswers.copy(data = Json.toJson(answers).as[JsObject])

      result shouldBe Some(expectedResult)
    }

    "return None when answers don't exist for the journey" in {
      val ctx = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)

      val result = await(repository.getJourneyAnswers(ctx))

      result shouldBe None
    }
  }

  "upsertAnswers" should {
    "update existing answers" in {
      when(mockClock.instant()).thenReturn(testInstant)

      val ctx     = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)
      val answers = TravelExpensesDb(allowablePublicTransportExpenses = Some(BigDecimal("1")))
      val update  = Json.toJson(answers.copy(allowablePublicTransportExpenses = Some(BigDecimal("2")))).as[JsObject]
      DbHelper.insertOne(TravelExpenses, answers)

      val result = await(repository.upsertJourneyAnswers(ctx, update))

      result shouldBe Some(update)
      DbHelper.get[TravelExpensesDb](TravelExpenses) shouldBe Some(answers.copy(allowablePublicTransportExpenses = Some(BigDecimal("2"))))
    }

    "insert new answers" in {
      when(mockClock.instant()).thenReturn(testInstant)

      val ctx     = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)
      val answers = TravelExpensesDb(allowablePublicTransportExpenses = Some(BigDecimal("1")))
      val update  = Json.toJson(answers).as[JsObject]

      val result = await(repository.upsertJourneyAnswers(ctx, update))

      result shouldBe Some(update)
      DbHelper.get[TravelExpensesDb](TravelExpenses) shouldBe Some(answers)
    }
  }

  "deleteJourneyAnswers" should {
    "delete answers and return true" in {
      val ctx     = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)
      val answers = TravelExpensesDb(allowablePublicTransportExpenses = Some(BigDecimal("1")))
      DbHelper.insertOne(TravelExpenses, answers)

      val result = await(repository.deleteJourneyAnswers(ctx, TravelExpenses))

      result shouldBe true
      DbHelper.get[TravelExpensesDb](TravelExpenses) shouldBe None
    }

    "return false if answers don't exist" in {
      val ctx = JourneyContext(testTaxYear, testBusinessId, testMtdItId, TravelExpenses)
      DbHelper.get[TravelExpensesDb](TravelExpenses) shouldBe None

      val result = await(repository.deleteJourneyAnswers(ctx, TravelExpenses))

      result shouldBe false
    }
  }

  "setStatus" should {
    "set trade details journey status with trade-details businessId" in {
      when(mockClock.instant()).thenReturn(testInstant)

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
      when(mockClock.instant()).thenReturn(testInstant)
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
      when(mockClock.instant()).thenReturn(testInstant)
      val result = repository.getAll(currTaxYear, mtditid, List.empty).value.futureValue.value
      result shouldBe TaskList(None, Nil, None)
    }

    "return trade details without businesses" in {
      when(mockClock.instant()).thenReturn(testInstant)
      val result = (for {
        _        <- repository.setStatus(tradeDetailsCtx, NotStarted)
        taskList <- repository.getAll(tradeDetailsCtx.taxYear, tradeDetailsCtx.mtditid, Nil)
      } yield taskList).value.futureValue.value

      result shouldBe TaskList(JourneyNameAndStatus(TradeDetails, NotStarted).some, Nil, None)
    }

    "return task list with businesses" in {
      when(mockClock.instant()).thenReturn(testInstant)
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
        ),
        None
      )
    }
  }

  "upsertData + get" should {
    "insert a new journey answers in in-progress status and calculate dates" in {
      when(mockClock.instant()).thenReturn(testInstant)
      val result = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        inserted <- repository.get(incomeCtx)
      } yield inserted.value).rightValue

      val expectedExpireAt = testInstant.plusSeconds(TTLinSeconds)

      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        NotStarted,
        Json.obj("field" -> "value"),
        expectedExpireAt,
        testInstant,
        testInstant)
    }

    "update already existing answers (values, updateAt)" in {
      val updatedAt = testInstant.plus(Duration.ofDays(1))
      when(mockClock.instant()).thenReturn(testInstant, updatedAt)
      val result = (for {
        _       <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _       <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "updated"))
        updated <- repository.get(incomeCtx)
      } yield updated.value).rightValue

      val expectedExpireAt = testInstant.plusSeconds(TTLinSeconds)
      result shouldBe JourneyAnswers(
        mtditid,
        businessId,
        currTaxYear,
        JourneyName.Income,
        NotStarted,
        Json.obj("field" -> "updated"),
        expectedExpireAt,
        testInstant,
        updatedAt
      )
    }
  }

  "updateStatus + get" should {
    "update status to a new one" in {
      val updatedAt = testInstant.plus(Duration.ofDays(2))
      when(mockClock.instant()).thenReturn(testInstant, updatedAt)
      val result = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _        <- repository.setStatus(incomeCtx, Completed)
        inserted <- repository.get(incomeCtx)
      } yield inserted.value).rightValue

      result.status shouldBe Completed
      result.updatedAt shouldBe updatedAt
    }
  }

  "testOnlyClearAllData" should {
    "clear all the data" in {
      when(mockClock.instant()).thenReturn(testInstant)
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
      when(mockClock.instant()).thenReturn(testInstant)
      val res = (for {
        _        <- repository.upsertAnswers(incomeCtx, Json.obj("field" -> "value"))
        _        <- repository.deleteOneOrMoreJourneys(incomeCtx)
        inserted <- repository.get(incomeCtx)
      } yield inserted).rightValue

      res shouldBe None
    }

    "clear all journeys starting with prefix" in {
      when(mockClock.instant()).thenReturn(testInstant)
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
      when(mockClock.instant()).thenReturn(testInstant)
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
