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

package service

import bulders.BusinessDataBuilder.{aBusiness, aGetBusinessDataResponse, aTaxPayerDisplayResponse}
import connectors.SelfEmploymentConnector
import connectors.SelfEmploymentConnector.Api1171Response
import models.common.IdType
import models.common.IdType.Nino
import models.database.JourneyState
import models.database.JourneyState.JourneyStateData
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BusinessServiceSpec extends TestUtils {
  val mockBusinessConnector = mock[SelfEmploymentConnector]

  lazy val aJourneyState = JourneyState(
    journeyStateData = JourneyStateData(businessId = aBusiness.businessId, journey = "income", taxYear = 2023, completedState = true)
  )

  lazy val service = new BusinessService(mockBusinessConnector)
  val nino         = aTaxPayerDisplayResponse.nino
  val businessId   = aBusiness.businessId
  val taxYear      = LocalDate.now.getYear

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

  def rightResponse[A](svcMethod: () => Future[A], expectedResult: A, stubs: () => Unit): Unit =
    "return a Right with GetBusinessDataRequest model" in {
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
      .expects(Nino, nino, *, *)
      .returning(Future.successful(expectedResult))
    ()
  }
}
