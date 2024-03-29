/*
 * Copyright 2024 HM Revenue & Customs
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

import models.common.{JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.frontend.abroad.SelfEmploymentAbroadAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec._
import utils.EitherTTestOps._

import scala.concurrent.ExecutionContext.Implicits.global

class AbroadAnswersServiceImplSpec extends AnyWordSpecLike {
  val service = new AbroadAnswersServiceImpl(StubJourneyAnswersRepository())

  "getAnswers" should {
    "return empty if no answers" in {
      val result = service.getAnswers(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val journeyAnswers: JourneyAnswers = mkJourneyAnswers(
        JourneyName.ExpensesTailoring,
        JourneyStatus.Completed,
        Json.obj("selfEmploymentAbroad" -> true)
      )

      val service = new AbroadAnswersServiceImpl(
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result = service.getAnswers(journeyCtxWithNino).rightValue
      assert(result === Some(SelfEmploymentAbroadAnswers(true)))
    }
  }

  "persistAnswers" should {
    "persist answers successfully" in {
      val answers = SelfEmploymentAbroadAnswers(true)
      val result  = service.persistAnswers(journeyCtxWithNino, answers).rightValue
      assert(result === ())
    }
  }
}
