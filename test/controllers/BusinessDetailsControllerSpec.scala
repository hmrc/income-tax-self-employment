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

package controllers

import builders.BusinessDataBuilder._
import mocks.services.MockBusinessService
import models.common.JourneyContextWithNino
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status._
import play.api.libs.json.Json
import stubs.controllers.actions.StubAuthorisedAction
import data.CommonTestData
import utils.BaseSpec.mtditid
import utils.TestUtils
import utils.TestUtils._

class BusinessDetailsControllerSpec extends AnyWordSpecLike with Matchers with CommonTestData {

  private val mockBusinessService = MockBusinessService.mockInstance
  private val authorisedAction = StubAuthorisedAction()
  private val controllerComponents = TestUtils.stubControllerComponents

  private val underTest = new BusinessDetailsController(mockBusinessService, authorisedAction, controllerComponents)

  s"getBusinesses" should {
    "return businesses" in {
      MockBusinessService.getBusinesses(mtditid, testNino)(aBusinesses)

      val result = underTest.getBusinesses(testNino)(TestUtils.fakeRequest.withHeaders("mtditid" -> mtditid.value))

      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(aBusinesses).toString()
    }
  }

  s"getBusiness" should {
    "return businesses" in {
      MockBusinessService.getBusiness(testBusinessId, mtditid, testNino)(aBusiness)

      val result = underTest.getBusiness(testNino, testBusinessId)(TestUtils.fakeRequest.withHeaders("mtditid" -> mtditid.value))

      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(aBusiness).toString()
    }
  }

  s"getUserDateOfBirth" should {
    "return the date of birth" in {
      MockBusinessService.getUserDateOfBirth(testNino)(aUserDateOfBirth)

      val result = underTest.getUserDateOfBirth(testNino)(TestUtils.fakeRequest)
      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(aUserDateOfBirth).toString()
    }
  }

  s"getAllBusinessIncomeSourcesSummaries" should {
    "return a list of business income source summaries" in {
      MockBusinessService.getAllBusinessIncomeSourcesSummaries(testTaxYear, mtditid, testNino)(List(aBusinessIncomeSourcesSummaryResponse))

      val result = underTest.getAllBusinessIncomeSourcesSummaries(testTaxYear, testNino)(TestUtils.fakeRequest.withHeaders("mtditid" -> mtditid.value))
      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(List(aBusinessIncomeSourcesSummaryResponse)).toString()
    }
  }

  s"getBusinessIncomeSourcesSummary" should {
    "return a business income source summary" in {
      MockBusinessService.getBusinessIncomeSourcesSummary(testTaxYear, testNino, testBusinessId)(aBusinessIncomeSourcesSummaryResponse)

      val result = underTest.getBusinessIncomeSourcesSummary(testTaxYear, testNino, testBusinessId)(TestUtils.fakeRequest)
      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(aBusinessIncomeSourcesSummaryResponse).toString()
    }
  }

  s"getNetBusinessProfitOrLossValues" should {
    "return net business profit values" in {
      val ctx = JourneyContextWithNino(testTaxYear, testBusinessId, mtditid, testNino)

      MockBusinessService.getBusinessIncomeSourcesSummary(testTaxYear, testNino, testBusinessId)(aBusinessIncomeSourcesSummaryResponse)
      MockBusinessService.getNetBusinessProfitOrLossValues(ctx)(aNetBusinessProfitValues)

      val result = underTest.getNetBusinessProfitOrLossValues(testTaxYear, testNino, testBusinessId)(TestUtils.fakeRequest.withHeaders("mtditid" -> mtditid.value))
      status(result) shouldBe OK
      bodyOf(result) shouldBe Json.toJson(aNetBusinessProfitValues).toString()
    }
  }

  "hasOtherIncomeSource" should {
    "return has more then one income flag" in {
      MockBusinessService.hasOtherIncomeSources(testTaxYear, testNino)(returnValue = true)
      val result = underTest.hasOtherIncomeSources(testTaxYear, testNino)(TestUtils.fakeRequest)
      status(result) shouldBe OK
      bodyOf(result) shouldBe "true"
    }
  }
}
