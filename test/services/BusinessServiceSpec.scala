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
import config.FeatureSwitchConfig
import data.CommonTestData
import data.IFSConnectorTestData.{api1786DeductionsSuccessResponse, citizenDetailsResponse}
import mocks.MockAppConfig
import mocks.connectors._
import models.common.BusinessId
import models.connector.api_1171._
import models.connector.api_1786.{FinancialsType, IncomeTypeTestData}
import models.connector.api_1803
import models.connector.businessDetailsConnector.{BusinessDetailsSuccessResponseSchema, ResponseType}
import models.domain.BusinessTestData
import models.error.DownstreamError.SingleDownstreamError
import models.error.ServiceError.BusinessNotFoundError
import models.error.{DownstreamErrorBody, ServiceError}
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import utils.BaseSpec._

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessServiceSpec extends AnyWordSpecLike
  with Matchers
  with BeforeAndAfterEach
  with DefaultAwaitTimeout
  with CommonTestData
  with MockIFSBusinessDetailsConnector
  with MockCitizenDetailsConnector
  with MockBusinessDetailsConnector
  with MockIFSConnector
  with MockIncomeSourcesConnector
  with MockAppConfig {

  val taxpayer: ResponseType =
    businessDetailsSuccessResponse.taxPayerDisplayResponse.copy(businessData = None)

  val emptyBusinessDetailsResponse: BusinessDetailsSuccessResponseSchema =
    businessDetailsSuccessResponse.copy(taxPayerDisplayResponse = taxpayer)

  val mockConfig = mock[FeatureSwitchConfig]

  lazy val testService =
    new BusinessService(
      mockIFSBusinessDetailsConnector,
      mockCitizenDetailsConnector,
      mockBusinessDetailsConnector,
      mockIFSConnector,
      mockIncomeSourcesConnector,
      mockConfig
    )

  private val error = SingleDownstreamError(400, DownstreamErrorBody.SingleDownstreamErrorBody.serverError)

  "getBusinesses" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, testMtdId, testNino)(Right(None))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, testMtdId, testNino)(Right(Some(businesses)))

        val result = await(testService.getBusinesses(testMtdId, testNino).value)

        result shouldBe Right(List(
          BusinessTestData.mkExample(testBusinessId),
          BusinessTestData.mkExample(testBusinessId2)
        ))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(businesses))

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
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(Some(businessId), mtditid, nino)(Right(None))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(Some(businessId), mtditid, nino)(Right(Some(businesses)))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Right(BusinessTestData.mkExample(businessId))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return Not Found if no business exist" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Left(BusinessNotFoundError(businessId)))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(businesses))

        val result = await(testService.getBusiness(businessId, mtditid, nino).value)

        result shouldBe Right(BusinessTestData.mkExample(businessId))
      }
    }
  }

  "getUserBusinessIds" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list when hipMigration1171Enabled is true" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, mtditid, nino)(Right(None))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, mtditid, nino)(Right(Some(businesses)))

        val result = await(testService.getUserBusinessIds(mtditid, nino).value)

        result shouldBe Right(List(
          BusinessId("id1"),
          BusinessId("id2")
        ))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

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

        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(businesses))

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
      CitizenDetailsConnectorMock.getCitizenDetails(nino)(citizenDetailsResponse.asRight)

      val result = await(testService.getUserDateOfBirth(nino).value)

      result shouldBe expectedResult
    }

    "return an error from downstream" in {
      CitizenDetailsConnectorMock.getCitizenDetails(nino)(error.asLeft)

      val result = await(testService.getUserDateOfBirth(nino).value)

      result shouldBe error.asLeft
    }
  }

  "getAllBusinessIncomeSourcesSummaries" when {
    "the hipMigration1171Enabled feature switch is enabled" should {
      "return an empty list if a user has no businesses" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, mtditid, nino)(Right(None))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(Nil)
      }

      "return an IncomeSourcesSummary for each business" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, mtditid, nino)(Right(Some(businessDetailsHipSuccessResponse)))
        IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(List(aBusinessIncomeSourcesSummaryResponse))
      }

      "return an error from downstream" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(true)
        BusinessDetailsConnectorMock.getBusinessDetails(None, mtditid, nino)(Left(BusinessNotFoundError(businessId)))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Left(BusinessNotFoundError(businessId))
      }
    }

    "the hipMigration1171Enabled feature switch is disabled" should {
      "return an empty list if a user has no businesses" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(emptyBusinessDetailsResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(Nil)
      }

      "return an IncomeSourcesSummary for each business" in {
        (() => mockConfig.hipMigration1171Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getBusinesses(nino)(Right(aGetBusinessDataResponse))
        IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

        val result = await(testService.getAllBusinessIncomeSourcesSummaries(taxYear, mtditid, nino).value)

        result shouldBe Right(List(aBusinessIncomeSourcesSummaryResponse))
      }
    }
  }

  "getBusinessIncomeSourcesSummary" should {
    "return an IncomeSourcesSummary for a business" in {
      IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))

      val result = await(testService.getBusinessIncomeSourcesSummary(taxYear, nino, businessId).value)

      result shouldBe Right(aBusinessIncomeSourcesSummaryResponse)
    }

    "return an error from downstream" in {
      IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(taxYear, nino, businessId)(Left(BusinessNotFoundError(businessId)))

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

      IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
      IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(
        returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Some(IncomeTypeTestData.sample))))
      )
      IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))

      val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value)

      result shouldBe Right(expectedSuccessResult)
    }

    "return an error from downstream" when {
      "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
        IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Left(error))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(
          testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value
        )

        result shouldBe Left(error)
      }

      "IFSConnector .getPeriodicSummaryDetail returns an error" in {
        IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(Left(error))

        val result: Either[ServiceError, NetBusinessProfitOrLossValues] = await(
          testService.getNetBusinessProfitOrLossValues(journeyCtxWithNino).value
        )

        result shouldBe Left(error)
      }

      "IFSConnector .getAnnualSummaries returns an error" in {
        IFSBusinessDetailsConnectorMock.getBusinessIncomeSourcesSummary(currTaxYear, nino, businessId)(Right(aBusinessIncomeSourcesSummaryResponse))
        IFSConnectorMock.getPeriodicSummaryDetail(journeyCtxWithNino)(
          returnValue = Right(api1786DeductionsSuccessResponse.copy(financials = FinancialsType(None, Some(IncomeTypeTestData.sample))))
        )
        IFSConnectorMock.getAnnualSummaries(journeyCtxWithNino)(Left(error))

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
        (() => mockConfig.hipMigration2085Enabled).expects().returning(true)
        IncomeSourcesConnectorMock.getIncomeSources(nino)(Right(listOfIncomeSources))
        val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

        result shouldBe Right(true)
      }

      "return an error from downstream" when {
        "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
          (() => mockConfig.hipMigration2085Enabled).expects().returning(true)
          IncomeSourcesConnectorMock.getIncomeSources(nino)(Left(error))

          val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

          result shouldBe Left(error)
        }
      }
    }

    "the hipMigration2085 feature switch is disabled" must {
      "return true if have more then one income sources" in {
        (() => mockConfig.hipMigration2085Enabled).expects().returning(false)
        IFSBusinessDetailsConnectorMock.getListOfIncomeSources(taxYear, nino)(Right(listOfIncomeSources))

        val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

        result shouldBe Right(true)
      }

      "return an error from downstream" when {
        "IFSBusinessDetailsConnector .getBusinessIncomeSourcesSummary returns an error" in {
          (() => mockConfig.hipMigration2085Enabled).expects().returning(false)
          IFSBusinessDetailsConnectorMock.getListOfIncomeSources(taxYear, nino)(Left(error))

          val result: Either[ServiceError, Boolean] = await(testService.hasOtherIncomeSources(taxYear, nino).value)

          result shouldBe Left(error)
        }
      }
    }

  }

}
