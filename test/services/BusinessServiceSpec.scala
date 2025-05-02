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

package services

import builders.BusinessDataBuilder._
import cats.implicits.catsSyntaxEitherId
import config.AppConfig
import mocks.connectors.MockBusinessDetailsConnector
import models.common.BusinessId
import models.connector.api_1171._
import models.connector.api_1786.{FinancialsType, IncomeTypeTestData}
import models.connector.api_1803
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.domain.BusinessTestData
import models.error.DownstreamError.SingleDownstreamError
import models.error.{DownstreamErrorBody, ServiceError}
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import stubs.connectors.StubIFSConnector.{api1171EmptyResponse, api1786DeductionsSuccessResponse}
import stubs.connectors.{StubBusinessDetailsConnector, StubIFSBusinessDetailsConnector, StubIFSConnector, StubMDTPConnector}
import uk.gov.hmrc.http.HttpClient
import utils.BaseSpec._
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessServiceSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach {

  val mockAppConfig: AppConfig   = mock[AppConfig]
  val mockHttpClient: HttpClient = mock[HttpClient]

  val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val mdtpConnector: StubMDTPConnector                             = StubMDTPConnector()
  val ifsConnector: StubIFSConnector                               = StubIFSConnector()
  val hipBusinessDetailsConnector: StubBusinessDetailsConnector    = StubBusinessDetailsConnector(mockHttpClient, mockAppConfig)

  val testService =
    new BusinessServiceImpl(
      ifsBusinessDetailsConnector,
      mdtpConnector,
      hipBusinessDetailsConnector,
      ifsConnector,
      mockAppConfig
    )

  private val error = SingleDownstreamError(400, DownstreamErrorBody.SingleDownstreamErrorBody.serverError)

  val stubBusinessDetailsConnector: StubBusinessDetailsConnector = StubBusinessDetailsConnector(
    mockHttpClient,
    mockAppConfig,
    getBusinessDetailsRes = aGetBusinessDataResponse.asRight
  )

  "getBusinesses" should {
    "return an empty list" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val result = testService.getBusinesses(businessId, mtditid, nino).value.futureValue.value
      assert(result === Nil)
    }

    "return an empty when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val result = testService.getBusinesses(businessId, mtditid, nino).value.futureValue.value
      assert(result === Nil)

    }

    "return a list of businesses" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

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
        StubBusinessDetailsConnector(httpClient = mockHttpClient, appConfig = mockAppConfig, getBusinessDetailsRes = Right(businesses)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getBusinesses(businessId, mtditid, nino).value.futureValue.value

      val expectedBusiness = List(
        BusinessTestData.mkExample(BusinessId("id1")),
        BusinessTestData.mkExample(BusinessId("id2"))
      )

      assert(result === expectedBusiness)
    }

    "return a list of businesses when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

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
        StubBusinessDetailsConnector(httpClient = mockHttpClient, appConfig = mockAppConfig, getBusinessDetailsRes = Right(businesses)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getBusinesses(businessId, mtditid, nino).value.futureValue.value

      val expectedBusiness = List(
        BusinessTestData.mkExample(BusinessId("id1")),
        BusinessTestData.mkExample(BusinessId("id2"))
      )

      assert(result === expectedBusiness)
    }

  }

  "getBusiness" should {
    "return Not Found if no business exist" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val id     = BusinessId("id")
      val result = testService.getBusiness(id, mtditid, nino).value.futureValue.left.value

      assert(result === ServiceError.BusinessNotFoundError(id))
    }

    "return Not Found if no business exist when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val id     = BusinessId("id")
      val result = testService.getBusiness(id, mtditid, nino).value.futureValue.left.value

      assert(result === ServiceError.BusinessNotFoundError(id))
    }

    "return a business" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val business = SuccessResponseSchemaTestData.mkExample(nino, mtditid, List(BusinessDataDetailsTestData.mkExample(businessId)))

      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(getBusinessesResult = Right(business)),
        StubMDTPConnector(),
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, getBusinessDetailsRes = Right(business)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getBusiness(businessId, mtditid, nino).value.futureValue.value
      assert(result === BusinessTestData.mkExample(businessId))
    }

    "return a business when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val business = SuccessResponseSchemaTestData.mkExample(nino, mtditid, List(BusinessDataDetailsTestData.mkExample(businessId)))

      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(getBusinessesResult = Right(business)),
        StubMDTPConnector(),
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, getBusinessDetailsRes = Right(business)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getBusiness(businessId, mtditid, nino).value.futureValue.value
      assert(result === BusinessTestData.mkExample(businessId))
    }
  }

  "getUserBusinessIds" should {
    "return an empty list" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val result = testService.getUserBusinessIds(businessId, mtditid, nino).value.futureValue.value
      assert(result === Nil)
    }

    "return an empty list when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val result = testService.getUserBusinessIds(businessId, mtditid, nino).value.futureValue.value
      assert(result === Nil)
    }

    "return a list of businesses" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

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
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, getBusinessDetailsRes = Right(businesses)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getUserBusinessIds(businessId, mtditid, nino).value.futureValue.value

      val expectedBusiness = List(
        BusinessId("id1"),
        BusinessId("id2")
      )

      assert(result === expectedBusiness)
    }

    "return a list of businesses when hipMigration1171Enabled is true" in {

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
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, getBusinessDetailsRes = Right(businesses)),
        StubIFSConnector(),
        mockAppConfig
      )

      val result = service.getUserBusinessIds(businessId, mtditid, nino).value.futureValue.value

      val expectedBusiness = List(
        BusinessId("id1"),
        BusinessId("id2")
      )

      assert(result === expectedBusiness)
    }
  }

  "getUserDateOfBirth" should {
    "return a user's date of birth as a LocalDate" in {
      val expectedResult = Right(aUserDateOfBirth)
      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(),
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getUserDateOfBirth(nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val service =
        new BusinessServiceImpl(
          StubIFSBusinessDetailsConnector(),
          StubMDTPConnector(getCitizenDetailsRes = error.asLeft),
          stubBusinessDetailsConnector,
          StubIFSConnector(),
          mockAppConfig
        )
      val result = service.getUserDateOfBirth(nino).value.futureValue
      assert(result === error.asLeft)
    }
  }

  "getAllBusinessIncomeSourcesSummaries" should {
    "return an empty list if a user has no businesses" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val expectedResult = Right(List.empty[BusinessIncomeSourcesSummaryResponse])
      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(),
        StubMDTPConnector(),
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, api1171EmptyResponse.asRight),
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an empty list if a user has no businesses when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val expectedResult = Right(List.empty[BusinessIncomeSourcesSummaryResponse])
      val service = new BusinessServiceImpl(
        StubIFSBusinessDetailsConnector(),
        StubMDTPConnector(),
        StubBusinessDetailsConnector(mockHttpClient, mockAppConfig, api1171EmptyResponse.asRight),
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an IncomeSourcesSummary for each business" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val expectedResult = Right(List(aBusinessIncomeSourcesSummaryResponse))
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an IncomeSourcesSummary for each business when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val expectedResult = Right(List(aBusinessIncomeSourcesSummaryResponse))
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)

      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = error.asLeft
      )
      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === error.asLeft)
    }

    "return an error from downstream when hipMigration1171Enabled is true" in {
      when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
      MockBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)(businessDetailsSuccessResponse)

      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = error.asLeft
      )
      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getAllBusinessIncomeSourcesSummaries(taxYear, businessId, mtditid, nino).value.futureValue
      assert(result === error.asLeft)
    }
  }

  "getBusinessIncomeSourcesSummary" should {
    "return an IncomeSourcesSummary for a business" in {
      val expectedResult = Right(aBusinessIncomeSourcesSummaryResponse)
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value.futureValue
      assert(result === expectedResult)
    }

    "return an error from downstream" in {
      val stubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessesResult = aGetBusinessDataResponse.asRight,
        getBusinessIncomeSourcesSummaryResult = error.asLeft
      )

      val service = new BusinessServiceImpl(
        stubIFSBusinessDetailsConnector,
        StubMDTPConnector(),
        stubBusinessDetailsConnector,
        StubIFSConnector(),
        mockAppConfig
      )
      val result = service.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value.futureValue
      assert(result === error.asLeft)
    }
  }

  "getNetBusinessProfitOrLossValues" should {
    "return NetBusinessProfitValues for a business" in new Test {
      override def stubIFSBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        getBusinessIncomeSourcesSummaryResult = aBusinessIncomeSourcesSummaryResponse.asRight
      )
      override def stubIFSConnector: StubIFSConnector = StubIFSConnector(
        getPeriodicSummaryDetailResult =
          Future.successful(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Some(IncomeTypeTestData.sample))).asRight),
        getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None))
      )
      val result: Either[ServiceError, NetBusinessProfitOrLossValues] = service.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value.futureValue

      assert(result === expectedSuccessResult.asRight)
    }

    "return an error from downstream" when {
      "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in new Test {
        override def stubIFSBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(getBusinessIncomeSourcesSummaryResult = error.asLeft)

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] =
          service.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value.futureValue

        assert(result === error.asLeft)
      }
      "IFSConnector .getPeriodicSummaryDetail returns an error" in new Test {
        override def stubIFSConnector: StubIFSConnector = StubIFSConnector(getPeriodicSummaryDetailResult = Future(error.asLeft))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] =
          service.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value.futureValue

        assert(result === error.asLeft)
      }
      "IFSConnector .getAnnualSummaries returns an error" in new Test {
        override def stubIFSConnector: StubIFSConnector = StubIFSConnector(getAnnualSummariesResult = error.asLeft)

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] =
          service.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value.futureValue

        assert(result === error.asLeft)
      }
    }
  }

  "hasOtherIncomeSources" should {
    "return true if have more then one income sources" in new Test {
      override def stubIFSBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
        listOfIncomeSources = listOfIncomeSources.asRight
      )
      override def stubIFSConnector: StubIFSConnector = StubIFSConnector(
        getPeriodicSummaryDetailResult =
          Future.successful(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Option(IncomeTypeTestData.sample))).asRight),
        getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None))
      )
      val result: Either[ServiceError, Boolean] = service.hasOtherIncomeSources(taxYear, nino).value.futureValue

      assert(result === Right(true))
    }

    "return an error from downstream" when {
      "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in new Test {
        override def stubIFSBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
          StubIFSBusinessDetailsConnector(listOfIncomeSources = error.asLeft)

        val result: Either[ServiceError, Boolean] = service.hasOtherIncomeSources(taxYear, nino).value.futureValue

        assert(result === error.asLeft)
      }
    }
  }

  trait Test {

    val expectedSuccessResult: NetBusinessProfitOrLossValues = NetBusinessProfitOrLossValues(
      IncomeTypeTestData.sample.turnover.getOrElse(0),
      IncomeTypeTestData.sample.other.getOrElse(0),
      aBusinessIncomeSourcesSummaryResponse.totalExpenses,
      aBusinessIncomeSourcesSummaryResponse.netProfit,
      aBusinessIncomeSourcesSummaryResponse.netLoss,
      0,
      0,
      0,
      aBusinessIncomeSourcesSummaryResponse.totalAdditions.getOrElse(0),
      0,
      0,
      aBusinessIncomeSourcesSummaryResponse.totalDeductions.getOrElse(0),
      0
    )

    def stubIFSBusinessDetailsConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
    def stubIFSConnector: StubIFSConnector                               = StubIFSConnector()
    def stubHipBusinessDetailsConnector: StubBusinessDetailsConnector    = StubBusinessDetailsConnector(mockHttpClient, mockAppConfig)

    def service: BusinessServiceImpl = new BusinessServiceImpl(
      stubIFSBusinessDetailsConnector,
      StubMDTPConnector(),
      stubHipBusinessDetailsConnector,
      stubIFSConnector,
      mockAppConfig
    )
  }
}
