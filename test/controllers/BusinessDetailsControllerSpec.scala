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

import bulders.BusinessDataBuilder._
import models.common.BusinessId
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.controllers.actions.StubAuthorisedAction
import stubs.services.StubBusinessService
import utils.BaseSpec.nino
import utils.TestUtils
import utils.TestUtils._
import play.api.http.Status._
import play.api.libs.json.Json

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
      val result = underTest.getBusiness(nino, BusinessId(aBusiness.businessId))(TestUtils.fakeRequest)
      assert(status(result) == OK)
      assert(bodyOf(result) == Json.toJson(aBusiness).toString())
    }
  }

}
