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
import mocks.connectors.{MockBusinessDetailsConnector, MockIFSBusinessDetailsConnector, MockIFSConnector}
import mocks.connectors.{MockBusinessDetailsConnector, MockIFSBusinessDetailsConnector, MockIFSConnector, MockIncomeSourcesConnector}
import models.common.{BusinessId, Mtditid, Nino}
import models.connector.api_1171._
import models.connector.api_1786.{FinancialsType, IncomeTypeTestData}
import models.connector.api_1803
import models.domain.BusinessTestData
import models.error.DownstreamError.SingleDownstreamError
import models.error.ServiceError.BusinessNotFoundError
import models.error.{DownstreamErrorBody, ServiceError}
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import stubs.connectors.StubIFSConnector.api1786DeductionsSuccessResponse
import stubs.connectors.StubMDTPConnector
import data.CommonTestData
import models.common.BusinessId
import models.connector.businessDetailsConnector.{BusinessDetailsSuccessResponseSchema, ResponseType}
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessServiceSpec extends AnyWordSpecLike with Matchers with BeforeAndAfterEach with DefaultAwaitTimeout with CommonTestData {

  val mockAppConfig: AppConfig = mock[AppConfig]

  val mdtpConnector: StubMDTPConnector = StubMDTPConnector()

  val taxpayer: ResponseType =
    businessDetailsSuccessResponse.taxPayerDisplayResponse.copy(businessData = None)

  val emptyBusinessDetailsResponse: BusinessDetailsSuccessResponseSchema =
    businessDetailsSuccessResponse.copy(taxPayerDisplayResponse = taxpayer)

  val testService =
    new BusinessService(
      MockIFSBusinessDetailsConnector.mockInstance,
      mdtpConnector,
      MockBusinessDetailsConnector.mockInstance,
      MockIFSConnector.mockInstance,
      MockIncomeSourcesConnector.mockInstance,
      mockAppConfig
    )

  private val error = SingleDownstreamError(400, DownstreamErrorBody.SingleDownstreamErrorBody.serverError)

  "getBusinesses" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, testMtdId, testNino)(Right(None))

        val result = await(testService.getBusinesses(testMtdId, testNino).value)
        result shouldBe Right(Nil)
      }

      "return a list of businesses" in {
        val businesses = SuccessResponseSchemaTestData.mkBusinessDetailsHipExample(
          testNino,
          testMtdId,
          List(
            BusinessDataDetailsTestData.mkExample(testBusinessId),
            BusinessDataDetailsTestData.mkExample(testBusinessId2)
          )
        )

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, testMtdId, testNino)(Right(Some(businesses)))

        val result = await(testService.getBusinesses(testMtdId, testNino).value)

        result shouldBe Right(List(
          BusinessTestData.mkExample(testBusinessId),
          BusinessTestData.mkExample(testBusinessId2)
        ))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

        val result = await(testService.getBusinesses(mtditid, nino).value)
        result shouldBe Right(Nil)
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

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(businesses))

        val result = await(testService.getBusinesses(mtditid, nino).value)

        result shouldBe Right(List(
          BusinessTestData.mkExample(BusinessId("id1")),
          BusinessTestData.mkExample(BusinessId("id2"))
        ))
      }
    }
  }

  "getBusiness" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return Not Found if no business exist when hipMigration1171Enabled is true" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(Some(businessId), mtditid, nino)(Right(None))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Left(ServiceError.BusinessNotFoundError(businessId))
      }

      "return a business when hipMigration1171Enabled is true" in {
        val businesses = SuccessResponseSchemaTestData.mkBusinessDetailsHipExample(
          nino,
          mtditid,
          List(
            BusinessDataDetailsTestData.mkExample(businessId),
            BusinessDataDetailsTestData.mkExample(BusinessId("id2"))
          )
        )

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(Some(businessId), mtditid, nino)(Right(Some(businesses)))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Right(BusinessTestData.mkExample(businessId))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return Not Found if no business exist" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Left(BusinessNotFoundError(businessId)))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Left(ServiceError.BusinessNotFoundError(businessId))
      }

      "return a business" in {
        val businesses = SuccessResponseSchemaTestData.mkExample(
          nino,
          mtditid,
          List(
            BusinessDataDetailsTestData.mkExample(businessId),
            BusinessDataDetailsTestData.mkExample(BusinessId("id2"))
          )
        )

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(businesses))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Right(BusinessTestData.mkExample(businessId))
      }
    }
  }

  "getUserBusinessIds" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list when hipMigration1171Enabled is true" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, mtditid, nino)(Right(None))

        val result = await(testService.getUserBusinessIds(mtditid, nino).value)

        result shouldBe Right(Nil)
      }

      "return a list of businesses when hipMigration1171Enabled is true" in {
        val businesses = SuccessResponseSchemaTestData.mkBusinessDetailsHipExample(
          nino,
          mtditid,
          List(
            BusinessDataDetailsTestData.mkExample(BusinessId("id1")),
            BusinessDataDetailsTestData.mkExample(BusinessId("id2"))
          )
        )

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, mtditid, nino)(Right(Some(businesses)))

        val result = await(testService.getUserBusinessIds(mtditid, nino).value)

        result shouldBe Right(List(
          BusinessId("id1"),
          BusinessId("id2")
        ))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

        val result = await(testService.getUserBusinessIds(mtditid, nino).value)

        result shouldBe Right(Nil)
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

        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(businesses))

        val result = await(testService.getUserBusinessIds(mtditid, nino).value)

        result shouldBe Right(List(
          BusinessId("id1"),
          BusinessId("id2")
        ))
      }
    }
  }

  "getUserDateOfBirth" should {
    "return a user's date of birth as a LocalDate" in {
      val expectedResult = Right(aUserDateOfBirth)
      val service = new BusinessService(
        MockIFSBusinessDetailsConnector.mockInstance,
        StubMDTPConnector(),
        MockBusinessDetailsConnector.mockInstance,
        MockIFSConnector.mockInstance,
        MockIncomeSourcesConnector.mockInstance,
        mockAppConfig
      )

      val result = await(service.getUserDateOfBirth(nino).value)

      result shouldBe expectedResult
    }

    "return an error from downstream" in {
      val service = new BusinessService(
        MockIFSBusinessDetailsConnector.mockInstance,
        StubMDTPConnector(getCitizenDetailsRes = error.asLeft),
        MockBusinessDetailsConnector.mockInstance,
        MockIFSConnector.mockInstance,
        MockIncomeSourcesConnector.mockInstance,
        mockAppConfig
      )

      val result = await(service.getUserDateOfBirth(nino).value)

      result shouldBe error.asLeft
    }
  }

  "getAllBusinessIncomeSourcesSummaries" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list if a user has no businesses" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, mtditid, nino)(Right(None))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(Nil)
      }

      "return an IncomeSourcesSummary for each business" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, mtditid, nino)(Right(Some(businessDetailsHipSuccessResponse)))
        MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(List(aBusinessIncomeSourcesSummaryResponse))
      }

      "return an error from downstream" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(true)
        MockBusinessDetailsConnector.getBusinessDetails(None, mtditid, nino)(Left(BusinessNotFoundError(businessId)))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Left(BusinessNotFoundError(businessId))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list if a user has no businesses" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(Nil)
      }

      "return an IncomeSourcesSummary for each business" in {
        when(mockAppConfig.hipMigration1171Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getBusinesses(nino)(Right(aGetBusinessDataResponse))
        MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(List(aBusinessIncomeSourcesSummaryResponse))
      }
    }
  }

  "getBusinessIncomeSourcesSummary" should {
    "return an IncomeSourcesSummary for a business" in {
      MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

      val result = await(testService.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value)

      result shouldBe Right(aBusinessIncomeSourcesSummaryResponse)
    }

    "return an error from downstream" in {
      MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Left(BusinessNotFoundError(businessId)))

      val result = await(testService.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value)

      result shouldBe Left(BusinessNotFoundError(businessId))
    }
  }

  "getNetBusinessProfitOrLossValues" should {
    "return NetBusinessProfitValues for a business" in {
      val expectedSuccessResult: NetBusinessProfitOrLossValues = NetBusinessProfitOrLossValues(
        turnover = IncomeTypeTestData.sample.turnover.getOrElse(0),
        incomeNotCountedAsTurnover = IncomeTypeTestData.sample.other.getOrElse(0),
        totalExpenses = aBusinessIncomeSourcesSummaryResponse.totalExpenses,
        netProfit = aBusinessIncomeSourcesSummaryResponse.netProfit,
        netLoss = aBusinessIncomeSourcesSummaryResponse.netLoss,
        balancingCharge = 0,
        goodsAndServicesForOwnUse = 0,
        disallowableExpenses = 0,
        totalAdditions = aBusinessIncomeSourcesSummaryResponse.totalAdditions.getOrElse(0),
        capitalAllowances = 0,
        turnoverNotTaxableAsBusinessProfit = 0,
        totalDeductions = aBusinessIncomeSourcesSummaryResponse.totalDeductions.getOrElse(0),
        outstandingBusinessIncome = 0
      )

      MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
      MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
        returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Some(IncomeTypeTestData.sample))))
      )
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))

      val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value)

      result shouldBe Right(expectedSuccessResult)
    }

    "return an error from downstream" when {
      "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
        MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Left(error))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(
          testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value
        )

        result shouldBe Left(error)
      }

      "IFSConnector .getPeriodicSummaryDetail returns an error" in {
        MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
        MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(Left(error))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(
          testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value
        )

        result shouldBe Left(error)
      }

      "IFSConnector .getAnnualSummaries returns an error" in {
        MockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
        MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
          returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Some(IncomeTypeTestData.sample))))
        )
        MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Left(error))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(
          testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value
        )

        result shouldBe Left(error)
      }
    }
  }

  "hasOtherIncomeSources" when {
    "the hipMigration2085 feature switch is enabled" must {
      "return true if have more then one income sources" in {
        when(mockAppConfig.hipMigration2085Enabled).thenReturn(true)
        MockIncomeSourcesConnector.getIncomeSources(nino)(Right(listOfIncomeSources))
        MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
          returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Option(IncomeTypeTestData.sample))))
        )
        MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))

        val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

        result shouldBe Right(true)
      }

      "return an error from downstream" when {
        "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
          when(mockAppConfig.hipMigration2085Enabled).thenReturn(true)
          MockIncomeSourcesConnector.getIncomeSources(nino)(Left(error))

          val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

          result shouldBe Left(error)
        }
      }
    }

    "the hipMigration2085 feature switch is disabled" must {
      "return true if have more then one income sources" in {
        when(mockAppConfig.hipMigration2085Enabled).thenReturn(false)
        MockIFSBusinessDetailsConnector.getListOfIncomeSources(taxYear, nino)(Right(listOfIncomeSources))
        MockIFSConnector.getPeriodicSummaryDetail(journeyCtxWithNino)(
          returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Option(IncomeTypeTestData.sample))))
        )
        MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))

        val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

        result shouldBe Right(true)
      }

      "return an error from downstream" when {
        "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
          when(mockAppConfig.hipMigration2085Enabled).thenReturn(false)
          MockIFSBusinessDetailsConnector.getListOfIncomeSources(taxYear, nino)(Left(error))

          val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

          result shouldBe Left(error)
        }
      }
    }

  }

}
