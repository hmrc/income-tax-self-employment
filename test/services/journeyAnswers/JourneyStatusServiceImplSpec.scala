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

package services.journeyAnswers

import cats.implicits._
import models.common.JourneyStatus._
import models.common.{JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.domain.JourneyNameAndStatus
import models.frontend.TaskList
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.JsObject
import stubs.connectors.StubIFSBusinessDetailsConnector
import stubs.repositories.StubJourneyAnswersRepository
import stubs.services.StubBusinessService
import utils.BaseSpec._

import java.time.Instant

class JourneyStatusServiceImplSpec extends AnyWordSpecLike with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  val businessConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val repository: StubJourneyAnswersRepository           = StubJourneyAnswersRepository()
  val now: Instant                                       = Instant.now()

  val underTest = new JourneyStatusServiceImpl(StubBusinessService(), repository)

  "set" should {
    "return unit" in {
      val result = underTest.set(incomeCtx, Completed)
      result.value.futureValue shouldBe ().asRight
    }
  }

  "get" should {
    "return check our record status if no answers" in {
      val result = underTest.get(incomeCtx)
      result.value.futureValue shouldBe CheckOurRecords.asRight
    }

    "return status if the answer exist" in {
      val underTest = new JourneyStatusServiceImpl(
        StubBusinessService(),
        repository.copy(
          getAnswer = Some(JourneyAnswers(mtditid, businessId, taxYear, JourneyName.ExpensesTailoring, Completed, JsObject.empty, now, now, now))
        )
      )
      val result = underTest.get(expensesTailoringCtx)
      result.value.futureValue shouldBe Completed.asRight
    }
  }

  "getTaskList" should {
    "return empty task list if no answers" in {
      val result = underTest.getLegacyTaskList(taxYear, mtditid, nino)
      result.value.futureValue shouldBe TaskList.empty.asRight
    }

    "return a task list" in {
      val taskList = TaskList(Nil, None)
      val underTest = new JourneyStatusServiceImpl(
        StubBusinessService(),
        repository.copy(
          getAllResult = Right(taskList)
        )
      )

      val result = underTest.getLegacyTaskList(taxYear, mtditid, nino)
      result.value.futureValue shouldBe taskList.asRight
    }
  }

}
