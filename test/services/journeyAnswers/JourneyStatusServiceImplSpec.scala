/*
 * Copyright 2025 HM Revenue & Customs
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

package services.journeyAnswers

import builders.BusinessDataBuilder.aBusiness
import mocks.repositories.MockJourneyAnswersRepository
import mocks.services.MockBusinessService
import models.common.JourneyName
import models.common.JourneyStatus._
import models.database.JourneyAnswers
import models.frontend.TaskList
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.JsObject
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class JourneyStatusServiceImplSpec extends AnyWordSpecLike with Matchers with DefaultAwaitTimeout {

  val now: Instant                                       = Instant.now()

  val testService: JourneyStatusServiceImpl = new JourneyStatusServiceImpl(MockBusinessService.mockInstance, MockJourneyAnswersRepository.mockInstance)

  "set" should {
    "return unit" in {
      val result = await(testService.set(incomeCtx, Completed).value)
      result shouldBe Right(())
    }
  }

  "get" should {
    "return check our record status if no answers" in {
      MockJourneyAnswersRepository.get(incomeCtx)(None)

      val result = await(testService.get(incomeCtx).value)

      result shouldBe Right(CheckOurRecords)
    }

    "return status if the answer exist" in {
      val journeyAnswers = Some(JourneyAnswers(mtditid, businessId, taxYear, JourneyName.ExpensesTailoring, Completed, JsObject.empty, now, now, now))
      MockJourneyAnswersRepository.get(expensesTailoringCtx)(journeyAnswers)

      val result = await(testService.get(expensesTailoringCtx).value)

      result shouldBe Right(Completed)
    }
  }

  "getLegacyTaskList" should {
    "return empty task list if no answers" in {
      MockBusinessService.getBusinesses(mtditid, nino)(List(aBusiness))
      MockJourneyAnswersRepository.getAll(taxYear, mtditid, List(aBusiness))(TaskList.empty)

      val result = await(testService.getLegacyTaskList(taxYear, mtditid, nino).value)

      result shouldBe Right(TaskList.empty)
    }

    "return a task list" in {
      val taskList = TaskList(Nil, None)
      MockBusinessService.getBusinesses(mtditid, nino)(List(aBusiness))
      MockJourneyAnswersRepository.getAll(taxYear, mtditid, Nil)(taskList)

      val result = await(testService.getLegacyTaskList(taxYear, mtditid, nino).value)

      result shouldBe Right(taskList)
    }
  }

}
