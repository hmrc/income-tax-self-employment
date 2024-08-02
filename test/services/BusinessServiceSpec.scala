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

package services

import bulders.BusinessDataBuilder._
import connectors.BusinessDetailsConnector
import connectors.BusinessDetailsConnector.{Api1171Response, Api1871Response, CitizenDetailsResponse}
import models.common.{BusinessId, IdType, Nino, TaxYear}
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec.taxYear
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class BusinessServiceSpec extends TestUtils {

  val mockBusinessConnector = mock[BusinessDetailsConnector]

  lazy val service = new BusinessServiceImpl(mockBusinessConnector)
  val nino         = Nino(aTaxPayerDisplayResponse.nino)
  val businessId   = BusinessId(aBusiness.businessId)

  for ((getMethodName, svcMethod) <- Seq(
      ("getBusinesses", () => service.getBusinesses(nino)),
      ("getBusiness", () => service.getBusiness(nino, businessId))
    ))
    s"$getMethodName" should { // scalastyle:off magic.number
      val expectedRight = Right(Seq(aBusiness))
      behave like rightResponse(svcMethod, expectedRight, () => stubConnectorGetBusiness(Right(aGetBusinessDataResponse)))

      val expectedLeft = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))
      behave like leftResponse(svcMethod, expectedLeft, () => stubConnectorGetBusiness(expectedLeft))
    }

  "getUserDateOfBirth" should {
    val expectedRight = Right(aUserDateOfBirth)
    behave like rightResponse(
      () => service.getUserDateOfBirth(nino),
      expectedRight,
      () => stubConnectorGetCitizenDetails(Right(getCitizenDetailsResponse)))

    val expectedLeft = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))
    behave like leftResponse(() => service.getUserDateOfBirth(nino), expectedLeft, () => stubConnectorGetCitizenDetails(expectedLeft))
  }

  "getBusinessIncomeSourcesSummary" should {
    val expectedRight = Right(BusinessIncomeSourcesSummaryResponse.empty)
    behave like rightResponse(
      () => service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId),
      expectedRight,
      () => stubConnectorGetBusinessIncomeSourcesSummary(expectedRight))

    val expectedLeft = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))
    behave like leftResponse(
      () => service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId),
      expectedLeft,
      () => stubConnectorGetBusinessIncomeSourcesSummary(expectedLeft))
  }

  def rightResponse[A](svcMethod: () => Future[A], expectedResult: A, stubs: () => Unit): Unit =
    "return a Right with the correct ResponseModel" in {
      stubs()
      await(svcMethod()) mustBe expectedResult
    }

  def leftResponse[A](svcMethod: () => Future[A], expectedResult: A, stubs: () => Unit, remark: String = ""): Unit =
    s"$remark error - return a Left when $remark returns an error" in {
      stubs()
      await(svcMethod()) mustBe expectedResult
    }

  private def stubConnectorGetBusiness(expectedResult: Api1171Response): Unit = {
    (mockBusinessConnector
      .getBusinesses(_: IdType, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(IdType.Nino, nino.nino, *, *)
      .returning(Future.successful(expectedResult))
    ()
  }

  private def stubConnectorGetCitizenDetails(expectedResult: CitizenDetailsResponse): Unit = {
    (mockBusinessConnector
      .getCitizenDetails(_: Nino)(_: HeaderCarrier, _: ExecutionContext))
      .expects(nino, *, *)
      .returning(Future.successful(expectedResult))
    ()
  }

  private def stubConnectorGetBusinessIncomeSourcesSummary(expectedResult: Api1871Response): Unit = {
    (mockBusinessConnector
      .getBusinessIncomeSourcesSummary(_: TaxYear, _: Nino, _: BusinessId)(_: HeaderCarrier, _: ExecutionContext))
      .expects(taxYear, nino, businessId, *, *)
      .returning(Future.successful(expectedResult))
    ()
  }

}
