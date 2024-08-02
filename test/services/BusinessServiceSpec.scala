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
import models.common.BusinessId
import models.connector.api_1171._
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain.BusinessTestData
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.connectors.StubIFSBusinessDetailsConnector
import utils.BaseSpec._
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessServiceSpec extends AnyWordSpecLike {

  val ifsConnector = StubIFSBusinessDetailsConnector()
  val service      = new BusinessServiceImpl(ifsConnector)

  private val error = Left(SingleDownstreamError(999, SingleDownstreamErrorBody("API_ERROR", "Error response from API")))

  "getBusinesses" should {
    "return an empty list" in {
      val result = service.getBusinesses(nino).value.futureValue.value
      assert(result === Nil)
    }

    "return a list of businesses" in {
      val businesses = SuccessResponseSchemaTestData.mkExample(
        nino,
        mtditid,
        List(
          BusinessDataDetailsTestData.mkExample(BusinessId("id1")),
          BusinessDataDetailsTestData.mkExample(BusinessId("id2"))
        )
      )
      val service = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(getBusinessesResult = Right(businesses)))

      val result = service.getBusinesses(nino).value.futureValue.value

      val expectedBusiness = List(
        BusinessTestData.mkExample(BusinessId("id1")),
        BusinessTestData.mkExample(BusinessId("id2"))
      )

      assert(result === expectedBusiness)
    }
  }

  "getBusiness" should {
    "return Not Found if no business exist" in {
      val id     = BusinessId("id")
      val result = service.getBusiness(nino, id).value.futureValue.left.value
      assert(result === ServiceError.BusinessNotFoundError(id))
    }

    "return a business" in {
      val business = SuccessResponseSchemaTestData.mkExample(nino, mtditid, List(BusinessDataDetailsTestData.mkExample(businessId)))
      val service  = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(getBusinessesResult = Right(business)))
      val result   = service.getBusiness(nino, businessId).value.futureValue.value
      assert(result === BusinessTestData.mkExample(businessId))
    }
  }

  "getUserDateOfBirth" should {
    "return a user's date of birth as a LocalDate" in {
      val expectedResult = Right(aUserDateOfBirth)
      val service        = new BusinessServiceImpl(StubIFSBusinessDetailsConnector())
      val result         = service.getUserDateOfBirth(nino).value
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val service = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(error))
      val result  = service.getUserDateOfBirth(nino).value
      assert(result === error)
    }
  }

  "getBusinessIncomeSourcesSummary" should {
    "return a business' IncomeSourcesSummary" in {
      val expectedResult = Right(BusinessIncomeSourcesSummaryResponse.empty)
      val service        = new BusinessServiceImpl(StubIFSBusinessDetailsConnector())
      val result         = service.getUserDateOfBirth(nino).value
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val service = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(error))
      val result  = service.getUserDateOfBirth(nino).value
      assert(result === error)
    }
  }

}
