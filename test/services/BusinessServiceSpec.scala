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
import cats.implicits.catsSyntaxEitherId
import models.common.BusinessId
import models.connector.api_1171._
import models.connector.api_1803
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain.BusinessTestData
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import stubs.connectors.{StubIFSBusinessDetailsConnector, StubIFSConnector, StubMDTPConnector}
import utils.BaseSpec._
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessServiceSpec extends AnyWordSpecLike {

  val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val mdtpConnector: StubMDTPConnector                             = StubMDTPConnector()
  val ifsConnector: StubIFSConnector                               = StubIFSConnector()
  val service                                                      = new BusinessServiceImpl(ifsBusinessDetailsConnector, mdtpConnector, ifsConnector)

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
      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(getBusinessesResult = Right(businesses)),
        StubMDTPConnector(),
        StubIFSConnector()
      )

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
      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(getBusinessesResult = Right(business)),
        StubMDTPConnector(),
        StubIFSConnector()
      )
      val result = service.getBusiness(nino, businessId).value.futureValue.value
      assert(result === BusinessTestData.mkExample(businessId))
    }
  }

  "getUserDateOfBirth" should {
    "return a user's date of birth as a LocalDate" in {
      val expectedResult = Right(aUserDateOfBirth)
      val service        = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(), StubMDTPConnector(), StubIFSConnector())
      val result         = service.getUserDateOfBirth(nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val service = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(), StubMDTPConnector(getCitizenDetailsRes = error), StubIFSConnector())
      val result  = service.getUserDateOfBirth(nino).value.futureValue
      assert(result === error)
    }
  }

  "getAllBusinessIncomeSourcesSummaries" should {
    "return an empty list if a user has no businesses" in {
      val expectedResult = Right(List.empty[BusinessIncomeSourcesSummaryResponse])
      val service        = new BusinessServiceImpl(StubIFSBusinessDetailsConnector(), StubMDTPConnector(), StubIFSConnector())
      val result         = service.getAllBusinessIncomeSourcesSummaries(taxYear, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an IncomeSourcesSummary for each business" in {
      val expectedResult = Right(List(aBusinessIncomeSourcesSummaryResponse))
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val service = new BusinessServiceImpl(stubIFSBusinessDetailsConnector, StubMDTPConnector(), StubIFSConnector())
      val result  = service.getAllBusinessIncomeSourcesSummaries(taxYear, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = error
      )
      val service = new BusinessServiceImpl(stubIFSBusinessDetailsConnector, StubMDTPConnector(), StubIFSConnector())
      val result  = service.getAllBusinessIncomeSourcesSummaries(taxYear, nino).value.futureValue
      assert(result === error)
    }
  }

  "getBusinessIncomeSourcesSummary" should {
    "return an IncomeSourcesSummary for a business" in {
      val expectedResult = Right(aBusinessIncomeSourcesSummaryResponse)
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val service = new BusinessServiceImpl(stubIFSBusinessDetailsConnector, StubMDTPConnector(), StubIFSConnector())
      val result  = service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = error
      )
      val service = new BusinessServiceImpl(stubIFSBusinessDetailsConnector, StubMDTPConnector(), StubIFSConnector())
      val result  = service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value.futureValue
      assert(result === error)
    }
  }

  "getNetBusinessProfitValues" should {
    "return NetBusinessProfitValues for a business" in {
      val expectedResult = Right(aNetBusinessProfitValues)
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val stubIFSConnector = StubIFSConnector(
        getPeriodicSummaryDetailResult = Future.successful(api1786DeductionsSuccessResponse.asRight),
        getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None))
      )
      val service = new BusinessServiceImpl(stubIFSBusinessDetailsConnector, StubMDTPConnector(), stubIFSConnector)
      service.getNetBusinessProfitValues(journeyCtxWithNino).value.map { result =>
        assert(result === expectedResult)
      }
    }
  }
}
