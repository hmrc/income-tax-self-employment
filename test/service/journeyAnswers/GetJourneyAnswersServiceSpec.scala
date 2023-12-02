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

package service.journeyAnswers

import mocks.MockJourneyAnswersRepository
import models.common.{JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import services.journeyAnswers.GetJourneyAnswersResult.{JourneyAnswersFound, NoJourneyAnswersFound}
import services.journeyAnswers.GetJourneyAnswersService
import utils.BaseSpec._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetJourneyAnswersServiceSpec extends AnyWordSpec with MockJourneyAnswersRepository with ScalaFutures {

  private val service = new GetJourneyAnswersService(mockJourneyAnswersRepository)

  private val id  = "some_id"
  private val now = Instant.now()
  private val journeyAnswers =
    JourneyAnswers(mtditid, businessId, currTaxYear, JourneyName.Income, JourneyStatus.InProgress, Json.obj(), now, now, now)

  "GetJourneyAnswersService" when {
    "getting journey answers tied to an id" when {
      "some journey answers are found" must {
        "return the journey answers wrapped in JourneyAnswersFound" in {
          MockJourneyAnswersRepository
            .get(id)
            .thenReturn(Future.successful(Some(journeyAnswers)))

          service.getJourneyAnswers(id).futureValue shouldBe JourneyAnswersFound(journeyAnswers)
        }
      }
      "no journey answers are found" must {
        "return NoJourneyAnswersFound" in {
          MockJourneyAnswersRepository
            .get(id)
            .thenReturn(Future.successful(None))

          service.getJourneyAnswers(id).futureValue shouldBe NoJourneyAnswersFound
        }
      }
    }
  }

}
