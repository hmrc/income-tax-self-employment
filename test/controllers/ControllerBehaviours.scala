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

import play.api.libs.json.{Json, Writes}
import play.api.mvc._
import play.api.test.FakeRequest
import utils.TestUtils

class ControllerBehaviours extends TestUtils {

  def controllerSpec(expectedStatus: Int,
                     expectedBody: String,
                     stubs: () => Unit,
                     methodBlock: () => Action[AnyContent],
                     testName: String = ""): Unit =
    s"$testName - return a $expectedStatus response and a result value" in {
      runControllerSpec(fakeRequest, expectedStatus, expectedBody, stubs, methodBlock)
    }

  def testRoute(expectedStatus: Int, expectedBody: String, methodBlock: () => Action[AnyContent], request: Request[AnyContent] = fakeRequest): Unit =
    runControllerSpec(request, expectedStatus, expectedBody, () => (), methodBlock)

  private def runControllerSpec(request: Request[AnyContent],
                                expectedStatus: Int,
                                expectedBody: String,
                                stubs: () => Unit,
                                methodBlock: () => Action[AnyContent]): Unit = {
    val result = {
      mockAuth()
      stubs()
      methodBlock()(request)
    }
    status(result) mustBe expectedStatus
    bodyOf(result) mustBe expectedBody
    ()
  }
}

object ControllerBehaviours {
  def buildRequest[A: Writes](body: A): FakeRequest[AnyContentAsJson] = FakeRequest()
    .withHeaders("mtditid" -> "1234567890")
    .withJsonBody(Json.toJson(body))

  def buildRequestNoContent: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withHeaders("mtditid" -> "1234567890")

}
