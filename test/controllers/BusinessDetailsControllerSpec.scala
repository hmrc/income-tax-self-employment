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

import builders.BusinessDataBuilder._
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status._
import play.api.libs.json.Json
import stubs.controllers.actions.StubAuthorisedAction
import stubs.services.StubBusinessService
import utils.BaseSpec.{businessId, nino, taxYear}
import utils.TestUtils
import utils.TestUtils._

class BusinessDetailsControllerSpec extends AnyWordSpecLike {
  def mkUnderTest(businessService: StubBusinessService): BusinessDetailsController =
    new BusinessDetailsController(businessService, StubAuthorisedAction(), TestUtils.stubControllerComponents)

  s"getBusinesses" should {
    val underTest = mkUnderTest(StubBusinessService(getBusinessesResult = Right(aBusinesses)))

    "return businesses" in {
      val result = underTest.getBusinesses(nino)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aBusinesses).toString())
    }
  }

  s"getBusiness" should {
    val underTest = mkUnderTest(StubBusinessService(getBusinessResult = Right(aBusiness)))

    "return businesses" in {
      val result = underTest.getBusiness(nino, aBusinessId)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aBusiness).toString())
    }
  }

  s"getUserDateOfBirth" should {
    val underTest = mkUnderTest(StubBusinessService(getUserDateOfBirthRes = Right(aUserDateOfBirth)))

    "return the date of birth" in {
      val result = underTest.getUserDateOfBirth(nino)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aUserDateOfBirth).toString())
    }
  }

  s"getAllBusinessIncomeSourcesSummaries" should {
    val underTest = mkUnderTest(StubBusinessService(getAllBusinessIncomeSourcesSummariesRes = Right(List(aBusinessIncomeSourcesSummaryResponse))))

    "return a list of business income source summaries" in {
      val result = underTest.getAllBusinessIncomeSourcesSummaries(taxYear, nino)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(List(aBusinessIncomeSourcesSummaryResponse)).toString())
    }
  }

  s"getBusinessIncomeSourcesSummary" should {
    val underTest = mkUnderTest(StubBusinessService(getBusinessIncomeSourcesSummaryRes = Right(aBusinessIncomeSourcesSummaryResponse)))

    "return a business income source summary" in {
      val result = underTest.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aBusinessIncomeSourcesSummaryResponse).toString())
    }
  }

  s"getNetBusinessProfitOrLossValues" should {
    val underTest = mkUnderTest(
      StubBusinessService(
        getBusinessIncomeSourcesSummaryRes = Right(aBusinessIncomeSourcesSummaryResponse),
        getNetBusinessProfitOrLossValuesRes = Right(aNetBusinessProfitValues)))

    "return net business profit values" in {
      val result = underTest.getNetBusinessProfitOrLossValues(taxYear, nino, businessId)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aNetBusinessProfitValues).toString())
    }
  }

  "hasOtherIncomeSource" should {
    val underTest = mkUnderTest(StubBusinessService(hasOtherIncomeSources = Right(true)))

    "return has more then one income flag" in {
      val result = underTest.hasOtherIncomeSources(taxYear, nino)(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == "true")
    }
  }
}
