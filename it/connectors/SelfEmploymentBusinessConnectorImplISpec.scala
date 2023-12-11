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

package connectors

import base.IntegrationBaseSpec
import cats.implicits.catsSyntaxEitherId
import helpers.WiremockSpec
import models.common.JourneyAnswersContext.JourneyContextWithNino
import models.common.JourneyName.Income
import models.common.Mtditid
import models.common.TaxYear.{asTys, endDate, startDate}
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1802.response.CreateAmendSEAnnualSubmissionResponse
import models.connector.api_1894.request._
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Incomes}
import models.connector.api_1895.response.AmendSEPeriodSummaryResponse
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.Json

class SelfEmploymentBusinessConnectorImplISpec extends WiremockSpec with IntegrationBaseSpec {

  private val connector = new SelfEmploymentBusinessConnectorImpl(httpClient, appConfig)

  "creating period summaries" when {
    "downstream returns a success response" must {
      "return the submission id" in new Api1894Test {
        stubPostWithRequestAndResponseBody(
          url = downstreamUrl,
          requestBody = requestBody,
          expectedResponse = downstreamSuccessResponse,
          expectedStatus = CREATED)

        val expectedResponse: CreateSEPeriodSummaryResponse = CreateSEPeriodSummaryResponse("someId")

        connector.createSEPeriodSummary(data).futureValue shouldBe expectedResponse.asRight

      }
    }
  }
  "amending period summaries" when {
    "downstream returns a success response" must {
      "return the submission id" in new Api1895Test {
        stubPutWithRequestAndResponseBody(
          url = downstreamUrl,
          requestBody = requestBody,
          expectedResponse = downstreamSuccessResponse,
          expectedStatus = OK)

        val expectedResponse: AmendSEPeriodSummaryResponse = AmendSEPeriodSummaryResponse("someId")

        connector.amendSEPeriodSummary(data).futureValue shouldBe expectedResponse.asRight

      }
    }
  }

  "upserting annual submissions" when {
    "downstream returns a success response" must {
      "return the transaction reference" in new Api1802Test {
        stubPutWithRequestAndResponseBody(
          url = downstreamUrl,
          requestBody = requestBody,
          expectedResponse = downstreamSuccessResponse,
          expectedStatus = OK)

        val expectedResponse: CreateAmendSEAnnualSubmissionResponse = CreateAmendSEAnnualSubmissionResponse("someId")

        connector.createAmendSEAnnualSubmission(data).futureValue shouldBe expectedResponse.asRight
      }
    }
  }
  "listing periodic submissions" when {
    "downstream returns a success response" must {
      "return the transaction reference" in new Api1965Test {
        stubGetWithResponseBody(
          url = downstreamUrl,
          expectedStatus = OK,
          expectedResponse = downstreamSuccessResponse
        )

        val expectedResponse: ListSEPeriodSummariesResponse =
          ListSEPeriodSummariesResponse(Some(List(PeriodDetails(None, Some("2023-04-06"), Some("2024-04-05"), None))))

        connector.listSEPeriodSummary(ctx).futureValue shouldBe expectedResponse.asRight
      }
    }
  }

  trait Api1802Test {
    val downstreamSuccessResponse: String                     = Json.stringify(Json.obj("transactionReference" -> "someId"))
    val requestBody: CreateAmendSEAnnualSubmissionRequestBody = CreateAmendSEAnnualSubmissionRequestBody(None, None, None)
    val data: CreateAmendSEAnnualSubmissionRequestData        = CreateAmendSEAnnualSubmissionRequestData(taxYear, nino, businessId, requestBody)

    val downstreamUrl =
      s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId.value}/annual-summaries"
  }

  trait Api1965Test {
    val downstreamSuccessResponse: String = Json.stringify(
      Json
        .parse(s"""
                 |{
                 |  "periods": [
                 |    {
                 |      "from": "2023-04-06",
                 |      "to": "2024-04-05"
                 |    }
                 |  ]
                 |}
                 |""".stripMargin))

    val ctx: JourneyContextWithNino = JourneyContextWithNino(taxYear, businessId, Mtditid(mtditid), nino, Income)

    val downstreamUrl =
      s"/income-tax/${asTys(ctx.taxYear)}/${ctx.nino.value}/self-employments/${ctx.businessId.value}/periodic-summaries"
  }

  trait Api1894Test {
    val downstreamSuccessResponse: String = Json.stringify(Json.obj("ibdSubmissionPeriodId" -> "someId"))

    val requestBody: CreateSEPeriodSummaryRequestBody = CreateSEPeriodSummaryRequestBody(
      "2023-04-06",
      "2024-04-05",
      Some(
        FinancialsType(None, Some(Deductions.empty.copy(costOfGoods = Some(SelfEmploymentDeductionsDetailPosNegType(Some(100.00), Some(100.00)))))))
    )

    val data: CreateSEPeriodSummaryRequestData = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, requestBody)

    val downstreamUrl =
      s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId.value}/periodic-summaries"
  }

  trait Api1895Test {
    val downstreamSuccessResponse: String = Json.stringify(Json.obj("periodId" -> "someId"))

    val requestBody: AmendSEPeriodSummaryRequestBody = AmendSEPeriodSummaryRequestBody(Some(Incomes(Some(100.00), None, None)), None)

    val data: AmendSEPeriodSummaryRequestData = AmendSEPeriodSummaryRequestData(taxYear, nino, businessId, requestBody)

    val downstreamUrl =
      s"/income-tax/${asTys(data.taxYear)}/${data.nino.value}/self-employments/${data.businessId.value}/periodic-summaries\\?from=${startDate(
          data.taxYear)}&to=${endDate(data.taxYear)}"
  }

}
