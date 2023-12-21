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
package controllers

import cats.data.EitherT
import cats.implicits.catsSyntaxOptionId
import gens.ExpensesJourneyAnswersGen.entertainmentCostsJourneyAnswersGen
import gens.genOne
import models.error.ServiceError
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class controllersSpec extends AnyWordSpec with Matchers with Logging {

  private val journeyAnswers = genOne(entertainmentCostsJourneyAnswersGen)

  "handleOptionalApiResult" when {
    "result is a success" should {
      "return Ok and answers as json" in {
        val success = EitherT.right[ServiceError](Future.successful(journeyAnswers.some))
        val result  = handleOptionalApiResult(success)

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.toJson(journeyAnswers)
      }
    }
    "unable to fetch answers from db" should {
      "return a 204" in {
        val failure = EitherT.right[ServiceError](Future.successful(Option.empty[EntertainmentJourneyAnswers]))
        val result  = handleOptionalApiResult(failure)

        status(result) shouldBe 204
      }
    }
  }
  "handleApiResult" when {
    "result is a success" should {
      "return Ok and answers as json" in {
        val success = EitherT.right[ServiceError](Future.successful(journeyAnswers))

        val result = handleApiResult(success)

        status(result) shouldBe 200
        contentAsJson(result) shouldBe Json.toJson(journeyAnswers)
      }
    }
  }

}
