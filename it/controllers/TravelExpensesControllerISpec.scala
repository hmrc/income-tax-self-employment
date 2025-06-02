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

import base.IntegrationBaseSpec
import connectors.data.{Api1786Test, Api1895Test}
import helpers.AuthStub
import models.common.{BusinessId, Nino, TaxYear}
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, OK}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.await


class TravelExpensesControllerISpec extends IntegrationBaseSpec with AuthStub {

  private def url(taxYear: TaxYear, businessId: BusinessId, nino: Nino) = s"/$taxYear/$businessId/travel-expenses/$nino/answers"

  "GET /:taxYear/:businessId/travel-expenses/:nino/answers" should {
    "Return answers from the IFS get self-employment period summary" in new Api1786Test {
      stubAuthorisedIndividual()
      stubGetWithResponseBody(
        url = s"/$testTaxYear/$testBusinessId/travel-expenses/$testNino/answers",
        expectedStatus = OK,
        expectedResponse = api1786ResponseJson
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).get())

      result.status mustBe OK
    }

    "Return OK when no body found" in new Api1786Test {
      stubAuthorisedIndividual()
      stubGetWithoutResponseBody(
        url = s"/$testTaxYear/$testBusinessId/travel-expenses/$testNino/answers",
        expectedStatus = NOT_FOUND
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).get())

      result.status mustBe OK
    }
  }

  "PUT /:taxYear/:businessId/travel-expenses/:nino/answers" should {
    "Update travel expenses and return OK when the request is valid" in new Api1895Test {
      stubAuthorisedIndividual()
      stubPutWithRequestAndResponseBody(
        url = s"/$testTaxYear/$testBusinessId/travel-expenses/$testNino/answers",
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = OK
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).put(api1895RequestJson))

      result.status mustBe OK
    }

    "Return BAD_REQUEST when the request is invalid" in new Api1895Test {
      stubAuthorisedIndividual()
      stubPutWithRequestAndResponseBody(
        url = s"/$testTaxYear/$testBusinessId/travel-expenses/$testNino/answers",
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = OK
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).put(requestBody.toString))

      result.status mustBe BAD_REQUEST
    }

  }

}
