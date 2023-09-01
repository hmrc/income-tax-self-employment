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

import bulders.BusinessDataBuilder.aGetBusinessDataRequestStr
import connectors.BusinessConnector
import connectors.BusinessConnector.IdType
import connectors.BusinessConnector.IdType.Nino
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import models.api.BusinessData.GetBusinessDataRequest
import models.error.APIErrorBody.{APIError, APIStatusError}
import org.scalamock.handlers.CallHandler3
import play.api.libs.json.Json
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class BusinessServiceSpec extends TestUtils {
  val mockBusinessConnector = mock[BusinessConnector]

  val service = new BusinessService(mockBusinessConnector)
  val nino = "FI290077A"
  val businessId = "SJPR05893938418"

  def stubGetBusiness(expectedResult: GetBusinessesResponse): CallHandler3[IdType, String, HeaderCarrier, Future[GetBusinessesResponse]] =
    (mockBusinessConnector.getBusinesses(_: IdType, _: String)(_: HeaderCarrier))
      .expects(Nino, nino, *)
      .returning(Future.successful(expectedResult))
  

  for ((getMethodName, aGet) <- Seq(("getBusinesses", () => service.getBusinesses(nino)),
                                    ("getBusiness", () => service.getBusiness(nino, businessId)))) {
    s"$getMethodName" should {
      behave like returnRight(aGet)
      behave like returnLeft(aGet)
    }
  }
  
  def returnRight(getRequest: () => Future[GetBusinessesResponse]): Unit =
    "return a Right with GetBusinessDataRequest model" in {
      val expectedResult = Right(Json.parse(aGetBusinessDataRequestStr).as[GetBusinessDataRequest])
      stubGetBusiness(expectedResult)
      val result = await(getRequest())
      result mustBe expectedResult
    }

  def returnLeft(getRequest: () => Future[GetBusinessesResponse]): Unit =
    "return a Left when connector returns an error" in { //scalastyle:off magic.number
      val apiError = Left(APIStatusError(999, APIError("API_ERROR", "Error response from API")))
      stubGetBusiness(apiError)
      val result = await(getRequest())
      result mustBe apiError
    }
}


