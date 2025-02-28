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
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import connectors.data.{Api1505Test, Api1508Test, Api1786Test, Api1803Test}
import helpers.WiremockSpec
import models.common.JourneyContextWithNino
import models.common.TaxYear.{asTys, endDate, startDate}
import models.connector.api_1505.CreateLossClaimSuccessResponse
import models.connector.api_1508.GetLossClaimSuccessResponse
import models.connector.api_1638.{RequestSchemaAPI1638, RequestSchemaAPI1638Class2Nics}
import models.connector.api_1639.{SuccessResponseAPI1639, SuccessResponseAPI1639Class2Nics}
import models.connector.api_1802.request.{AnnualAdjustments, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803.SuccessResponseSchema
import models.connector.api_1894.request._
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Incomes}
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import models.error.DownstreamError.{GenericDownstreamError, SingleDownstreamError}
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ErrorType.DownstreamErrorCode
import models.error.ServiceError
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{BAD_REQUEST, CONFLICT, CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNPROCESSABLE_ENTITY}
import play.api.libs.json.Json

class IFSConnectorImplISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector: IFSConnectorImpl = new IFSConnectorImpl(httpClient, appConfig)
  val ctx: JourneyContextWithNino = JourneyContextWithNino(taxYear, businessId, mtditid, nino)

  "getPeriodicSummaryDetail" must {
    "return successful response" in new Api1786Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )
      connector.getPeriodicSummaryDetail(ctx).futureValue shouldBe successResponse.asRight
    }
  }

  "createAmendSEAnnualSubmission" must {
    "return the transaction reference" in new Api1802Test {
      stubPutWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = OK)

      connector.createAmendSEAnnualSubmission(data).futureValue shouldBe ().asRight
    }
  }

  "getAnnualSummaries" must {
    "return the annual summaries" in new Api1803Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = successResponseRaw,
        expectedStatus = OK
      )

      connector.getAnnualSummaries(ctx).futureValue shouldBe successResponse.asRight
    }

    "return an empty annual summary if not found" in new Api1803Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedResponse = "{}",
        expectedStatus = NOT_FOUND
      )

      connector.getAnnualSummaries(ctx).futureValue shouldBe SuccessResponseSchema(None, None, None).asRight
    }
  }

  "deleteSEAnnualSummaries" must {
    "return unit" in {
      val downstreamUrl = s"/income-tax/${asTys(taxYear)}/${nino.value}/self-employments/${businessId.value}/annual-summaries"
      stubDelete(
        url = downstreamUrl,
        expectedResponse = "",
        expectedStatus = OK
      )
      val result = connector.deleteSEAnnualSummaries(ctx).value.futureValue
      assert(result === Right(()))
    }
  }

  "createUpdateOrDeleteApiAnnualSummaries" must {
    "send a PUT request when given valid data to submit" in new Api1802Test {
      stubDelete(
        url = downstreamUrl,
        expectedResponse = "",
        expectedStatus = OK
      )

      connector.createUpdateOrDeleteApiAnnualSummaries(ctx, None).value.futureValue shouldBe ().asRight
    }
    "send a DELETE request when given no data to submit" in new Api1802Test {
      stubPutWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = OK)

      connector.createUpdateOrDeleteApiAnnualSummaries(ctx, requestBody.some).value.futureValue shouldBe ().asRight
    }
  }

  "createSEPeriodSummary" must {
    "return the submission id" in new Api1894Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = CREATED)

      connector.createSEPeriodSummary(data).futureValue shouldBe ().asRight

    }
  }

  "amendSEPeriodSummary" must {
    "return the submission id" in new Api1895Test {
      stubPutWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = downstreamSuccessResponse,
        expectedStatus = OK)

      connector.amendSEPeriodSummary(data).futureValue shouldBe ().asRight

    }
  }

  "listSEPeriodSummary" must {
    "return the submissions" in new Api1965Test {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedStatus = OK,
        expectedResponse = downstreamSuccessResponse
      )

      val expectedResponse: Option[ListSEPeriodSummariesResponse] =
        Option(ListSEPeriodSummariesResponse(Some(List(PeriodDetails(None, Some("2023-04-06"), Some("2024-04-05"))))))

      connector.listSEPeriodSummary(ctx).futureValue shouldBe expectedResponse.asRight

    }
  }

  "getDisclosuresSubmission" must {
    "return None when no data" in {
      val result = connector.getDisclosuresSubmission(ctx).value.futureValue
      assert(result === Right(None))
    }

    "return existing data" in new ApiDisclosuresTest {
      stubGetWithResponseBody(
        url = downstreamUrl,
        expectedStatus = OK,
        expectedResponse = responseJson
      )
      val result: Option[SuccessResponseAPI1639] = connector.getDisclosuresSubmission(ctx).value.futureValue.value
      assert(result === Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))))
    }
  }

  "upsertDisclosuresSubmission" must {
    "return unit" in new ApiDisclosuresTest {
      val request: RequestSchemaAPI1638 = RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true))))
      stubPutWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = request,
        expectedResponse = "",
        expectedStatus = CREATED
      )
      val result: Either[ServiceError, Unit] = connector.upsertDisclosuresSubmission(ctx, request).value.futureValue
      assert(result === Right(()))
    }
  }

  "deleteDisclosuresSubmission" must {
    "return unit" in new ApiDisclosuresTest {
      stubDelete(
        url = downstreamUrl,
        expectedResponse = "",
        expectedStatus = OK
      )
      val result: Either[ServiceError, Unit] = connector.deleteDisclosuresSubmission(ctx).value.futureValue
      assert(result === Right(()))
    }
  }

  "createClaimLoss" must {
    "return a success" in new Api1505Test {
      stubPostWithRequestAndResponseBody(url = downstreamUrl, requestBody = requestBody, expectedResponse = successResponseRaw, expectedStatus = OK)

      connector.createLossClaim(ctx, requestBody).value.futureValue shouldBe successResponse.asRight
    }

    "return a ParsingError when expectedResponse is incorrect" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = badRequestResponseRaw,
        expectedStatus = OK)

      connector.createLossClaim(ctx, requestBody).value.futureValue shouldBe
        Left(SingleDownstreamError(500, SingleDownstreamErrorBody("PARSING_ERROR", "Error parsing response from API", DownstreamErrorCode)))
    }

    "returns BAD_REQUEST GenericDownstreamError when expected status is BAD_REQUEST" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = BAD_REQUEST
      )

      val result: Either[ServiceError, CreateLossClaimSuccessResponse] = connector.createLossClaim(ctx, requestBody).value.futureValue
      result match {
        case Left(GenericDownstreamError(status, message)) =>
          status shouldBe 400
          message should include(s"Downstream error when calling POST http://localhost:11111$downstreamUrl")
          message should include(s"status=$BAD_REQUEST")
          message should include(s"body:\n$successResponseRaw")
        case _ => fail("Expected a GenericDownstreamError")
      }
    }

    "returns NOT_FOUND GenericDownstreamError when expected status is NOT_FOUND" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = NOT_FOUND
      )

      val result: Either[ServiceError, CreateLossClaimSuccessResponse] = connector.createLossClaim(ctx, requestBody).value.futureValue
      result match {
        case Left(GenericDownstreamError(status, message)) =>
          status shouldBe 404
          message should include(s"Downstream error when calling POST http://localhost:11111$downstreamUrl")
          message should include(s"status=$NOT_FOUND")
          message should include(s"body:\n$successResponseRaw")
        case _ => fail("Expected a GenericDownstreamError")
      }
    }

    "returns CONFLICT GenericDownstreamError when expected status is CONFLICT" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = CONFLICT
      )

      val result: Either[ServiceError, CreateLossClaimSuccessResponse] = connector.createLossClaim(ctx, requestBody).value.futureValue
      result match {
        case Left(GenericDownstreamError(status, message)) =>
          status shouldBe 409
          message should include(s"Downstream error when calling POST http://localhost:11111$downstreamUrl")
          message should include(s"status=$CONFLICT")
          message should include(s"body:\n$successResponseRaw")
        case _ => fail("Expected a GenericDownstreamError")
      }
    }

    "returns UNPROCESSABLE_ENTITY GenericDownstreamError when expected status is UNPROCESSABLE_ENTITY" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponseRaw,
        expectedStatus = UNPROCESSABLE_ENTITY
      )

      val result: Either[ServiceError, CreateLossClaimSuccessResponse] = connector.createLossClaim(ctx, requestBody).value.futureValue
      result match {
        case Left(GenericDownstreamError(status, message)) =>
          status shouldBe 422
          message should include(s"Downstream error when calling POST http://localhost:11111$downstreamUrl")
          message should include(s"status=$UNPROCESSABLE_ENTITY")
          message should include(s"body:\n$successResponseRaw")
        case _ => fail("Expected a GenericDownstreamError")
      }
    }

    "returns downstream error when unknown status" in new Api1505Test {
      stubPostWithRequestAndResponseBody(
        url = downstreamUrl,
        requestBody = requestBody,
        expectedResponse = successResponse.claimId,
        expectedStatus = CREATED
      )

      val result: Either[ServiceError, CreateLossClaimSuccessResponse] = connector.createLossClaim(ctx, requestBody).value.futureValue

      result match {
        case Left(GenericDownstreamError(status, message)) =>
          status shouldBe 201
          message should include(s"Downstream error when calling POST http://localhost:11111$downstreamUrl")
          message should include(s"status=$CREATED")
          message should include(s"body:\n${successResponse.claimId}")
        case _ => fail("Expected a GenericDownstreamError")
      }
    }
  }

  "getClaimLoss" must {
    "return a success" in new Api1508Test {
      stubGetWithResponseBody(url = downstreamUrl, expectedResponse = successResponseRaw, expectedStatus = OK)

      connector.getLossClaim(ctx, claimId).value.futureValue shouldBe successResponse.asRight
    }

    "return a ParsingError when expectedResponse is incorrect" in new Api1508Test {
      stubGetWithResponseBody(url = downstreamUrl, expectedResponse = badRequestResponseRaw, expectedStatus = OK)

      connector.getLossClaim(ctx, claimId).value.futureValue shouldBe
        Left(SingleDownstreamError(500, SingleDownstreamErrorBody("PARSING_ERROR", "Error parsing response from API", DownstreamErrorCode)))
    }

    for (errorStatus <- Seq(BAD_REQUEST, NOT_FOUND, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR))
      s"returns $errorStatus GenericDownstreamError when expected status is $errorStatus" in new Api1508Test {
        stubGetWithResponseBody(
          url = downstreamUrl,
          expectedResponse = successResponseRaw,
          expectedStatus = errorStatus
        )

        val result: Either[ServiceError, GetLossClaimSuccessResponse] = connector.getLossClaim(ctx, claimId).value.futureValue
        result match {
          case Left(GenericDownstreamError(status, message)) =>
            status shouldBe errorStatus
            message should include(s"Downstream error when calling GET http://localhost:11111$downstreamUrl")
            message should include(s"status=$errorStatus")
            message should include(s"body:\n$successResponseRaw")
          case _ => fail("Expected a GenericDownstreamError")
        }
      }
  }

  trait Api1802Test {
    val downstreamSuccessResponse: String = Json.stringify(Json.obj("transactionReference" -> "someId"))
    val requestBody: CreateAmendSEAnnualSubmissionRequestBody =
      CreateAmendSEAnnualSubmissionRequestBody(AnnualAdjustments.empty.copy(goodsAndServicesOwnUse = Some(100)).some, None, None)
    val data: CreateAmendSEAnnualSubmissionRequestData = CreateAmendSEAnnualSubmissionRequestData(taxYear, nino, businessId, requestBody)

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

  trait ApiDisclosuresTest {
    val responseJson: String = Json.stringify(Json.parse(s"""
                  |{
                  |  "class2Nics": {
                  |     "class2VoluntaryContributions": true
                  |  }
                  |}
                  |""".stripMargin))

    val downstreamUrl = s"/income-tax/disclosures/${ctx.nino.value}/${ctx.taxYear.toYYYY_YY}"
  }

}
