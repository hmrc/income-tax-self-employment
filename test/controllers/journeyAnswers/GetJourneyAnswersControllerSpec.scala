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

package controllers.journeyAnswers

//import mocks.MockGetJourneyAnswersService
//import models.database.JourneyAnswers
//import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
//import org.scalatest.wordspec.AnyWordSpec
//import play.api.http.Status.OK
//import play.api.libs.json.Json
//import play.api.mvc.Result
//import play.api.test.Helpers._
//import services.journeyAnswers.GetJourneyAnswersResult.{JourneyAnswersFound, NoJourneyAnswersFound}
//import utils.TestUtils
//
//import scala.concurrent.Future
//
// TODO Merge controllers to one SASS-6363
//class GetJourneyAnswersControllerSpec extends AnyWordSpec with MockGetJourneyAnswersService with TestUtils {
//
//  private val controller = new GetJourneyAnswersController(stubControllerComponents, mockGetJourneyAnswersService)
//
//  private val id             = "some_id"
//  private val journeyAnswers = JourneyAnswers(id)
//
//  "GetJourneyAnswersController" when {
//    "handling a valid request with an id" when {
//      "the service returns some journey answers" must {
//        "return a 200 and the journey answers" in {
//          MockGetJourneyAnswersService
//            .getJourneyAnswers(id)
//            .thenReturn(Future.successful(JourneyAnswersFound(journeyAnswers)))
//
//          val result: Future[Result] = controller.handleRequest(id)(fakeRequest)
//
//          status(result) shouldBe OK
//          contentAsJson(result) shouldBe Json.toJson(journeyAnswers)
//        }
//      }
//      "the service returns no journey answers" must {
//        "return a 404 NOT_FOUND" in {
//          MockGetJourneyAnswersService
//            .getJourneyAnswers(id)
//            .thenReturn(Future.successful(NoJourneyAnswersFound))
//
//          val result: Future[Result] = controller.handleRequest(id)(fakeRequest)
//
//          status(result) shouldBe NOT_FOUND
//          contentAsJson(result) shouldBe Json.obj("code" -> "NOT_FOUND", "reason" -> s"No journey answers found for id: $id")
//        }
//      }
//    }
//  }
//
//}
