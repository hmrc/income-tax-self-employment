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
import models.common.TaxYear.{asTys, endDate, startDate}
import models.common.{BusinessId, Nino, TaxYear}
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.await


class TravelExpensesControllerISpec extends IntegrationBaseSpec with AuthStub {

  private def url(taxYear: TaxYear, businessId: BusinessId, nino: Nino) = s"/income-tax-self-employment$taxYear/$businessId/travel-expenses/$nino/answers"

  val api1786ResponseJson: String =
    """{
      |   "from": "2001-01-01",
      |   "to": "2001-01-01",
      |   "financials": {
      |   "deductions": {},
      |      "incomes": {
      |         "turnover": 100
      |      }
      |   }
      |}
      |""".stripMargin

  "GET /:taxYear/:businessId/travel-expenses/:nino/answers" should {
    "Return answers from the IFS get self-employment period summary" in new Api1786Test {
      stubAuthorisedIndividual()
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedStatus = OK,
        expectedResponse = this.api1786ResponseJson
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).get())

      result.status mustBe OK
    }

    "Return OK when no body found" in new Api1786Test {
      stubAuthorisedIndividual()
      stubGetWithoutResponseBody(
        url = downstreamUrl,
        expectedStatus = NOT_FOUND
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).get())

      result.status mustBe OK
    }
  }

  "PUT /:taxYear/:businessId/travel-expenses/:nino/answers" should {
    "Update travel expenses and return OK when the request is valid" in new Api1895Test {
      stubAuthorisedIndividual()

      stubGetWithResponseBody(
        url = s"/income-tax/${asTys(testTaxYear)}/$testNino/self-employments/$testBusinessId/periodic-summary-detail\\?" +
          s"from=${testTaxYear.fromAnnualPeriod}&to=${testTaxYear.toAnnualPeriod}",
        expectedStatus = OK,
        expectedResponse = api1786ResponseJson
      )
      stubPutWithRequestAndResponseBody(
        url = s"/income-tax/${asTys(testTaxYear)}/$testNino/self-employments/$testBusinessId/periodic-summaries\\?" +
          s"from=${startDate(testTaxYear)}&to=${endDate(testTaxYear)}",
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = NO_CONTENT
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino))
        .put(Json.toJson(travelExpensesDb)))

      result.status mustBe NO_CONTENT
    }

    "Return NOT_FOUND when the request is called incorrectly" in new Api1895Test {
      stubAuthorisedIndividual()

      stubPutWithRequestAndResponseBody(
        url = s"/income-tax/${asTys(testTaxYear)}/$testNino/self-employments/$testBusinessId/periodic-summaries\\?" +
          s"from=${startDate(testTaxYear)}&to=${endDate(testTaxYear)}",
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = NO_CONTENT
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino))
        .put(Json.toJson(travelExpensesDb)))

      result.status mustBe NOT_FOUND
    }

    "Return BAD_REQUEST when the request is invalid" in new Api1895Test {
      stubAuthorisedIndividual()
      stubPutWithRequestAndResponseBody(
        url = s"/income-tax-self-employment/$testTaxYear/$testBusinessId/travel-expenses/$testNino/answers",
        requestBody = invalidRequestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = BAD_REQUEST
      )

      val result: WSResponse = await(buildClient(url(testTaxYear, testBusinessId, testNino)).put(travelExpensesDb.toString))

      result.status mustBe BAD_REQUEST
    }
  }

}