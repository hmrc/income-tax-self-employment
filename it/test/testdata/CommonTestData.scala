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

package testdata

import models.common._
import models.connector.api_2085.{IncomeSource, ListOfIncomeSources}
import models.connector.businessDetailsConnector.{BusinessDataDetails, BusinessDetailsHipSuccessWrapper, BusinessDetailsSuccessResponseSchema, ResponseType}
import utils.BaseSpec.taxYear

import java.time.LocalDate

trait CommonTestData extends IntegrationTimeData {

  val testTaxYear2425: String = TaxYear.asTys(taxYear)
  val testTaxYear2024: TaxYear    = TaxYear(2024)
  val testTaxYear2025: TaxYear    = TaxYear(2025)
  val testMtdItId: Mtditid        = Mtditid("555555555")
  val testBusinessId: BusinessId  = BusinessId("SJPR05893938418")
  val testBusinessId2: BusinessId = BusinessId("SJPR05893938419")
  val testNino: Nino              = Nino("AA123123A")
  val testClaimId: String         = "12345"
  val testTradingName             = "Test trading name"
  val testTradingName2            = "Test trading name 2"

  val testTaxYear: TaxYear  = TaxYear(2024)
  val testAuthToken: String = "Bearer 123"
  val testApiToken          = "testToken"
  val testCorrelationId     = "X-123"

  val testContextWithNino: JourneyContextWithNino = JourneyContextWithNino(testTaxYear2024, testBusinessId, testMtdItId, testNino)

  val testBusinessDetails1: BusinessDataDetails = BusinessDataDetails(
    incomeSourceId = testBusinessId.value,
    accountingPeriodStartDate = "2024-04-06",
    accountingPeriodEndDate = "2025-04-05",
    cessationDate = None,
    incomeSource = None,
    tradingName = Some(testTradingName),
    businessAddressDetails = None,
    businessContactDetails = None,
    tradingStartDate = None,
    cashOrAccruals = None,
    seasonal = None,
    paperLess = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    incomeSourceStartDate = None,
    latencyDetails = None
  )

  val testBusinessDetails2: BusinessDataDetails = BusinessDataDetails(
    incomeSourceId = testBusinessId2.value,
    accountingPeriodStartDate = "2024-04-06",
    accountingPeriodEndDate = "2025-04-05",
    cessationDate = None,
    incomeSource = None,
    tradingName = Some(testTradingName2),
    businessAddressDetails = None,
    businessContactDetails = None,
    tradingStartDate = None,
    cashOrAccruals = None,
    seasonal = None,
    paperLess = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    incomeSourceStartDate = None,
    latencyDetails = None
  )

  val test1171Response: BusinessDetailsSuccessResponseSchema = BusinessDetailsSuccessResponseSchema(
    processingDate = "2023-10-01T12:00:00Z",
    taxPayerDisplayResponse = ResponseType(
      safeId = "1",
      nino = testNino.value,
      mtdId = testMtdItId.value,
      yearOfMigration = Some("2024"),
      propertyIncome = false,
      businessData = Some(
        List(
          testBusinessDetails1,
          testBusinessDetails2
        ))
    )
  )

  val test1171HipResponseJson: String =
    s"""
      |{
      |  "success": {
      |    "processingDate": "2023-10-01T12:00:00Z",
      |    "taxPayerDisplayResponse": {
      |      "safeId": "1",
      |      "nino": "$testNino",
      |      "mtdId": "$testMtdItId",
      |      "yearOfMigration": "2024",
      |      "propertyIncomeFlag": false,
      |      "businessData": [
      |       {
      |          "incomeSourceId": "${testBusinessId.value}",
      |          "incomeSource": "string",
      |          "accPeriodSDate": "2024-04-06",
      |          "accPeriodEDate": "2025-04-05",
      |          "tradingName": "$testTradingName"
      |        },
      |        {
      |          "incomeSourceId": "${testBusinessId2.value}",
      |          "incomeSource": "string",
      |          "accPeriodSDate": "2024-04-06",
      |          "accPeriodEDate": "2025-04-05",
      |          "tradingName": "$testTradingName"
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin

  val test1171HipResponse: BusinessDetailsHipSuccessWrapper = BusinessDetailsHipSuccessWrapper(
    BusinessDetailsSuccessResponseSchema(
      processingDate = "2023-10-01T12:00:00Z",
      taxPayerDisplayResponse = ResponseType(
        safeId = "1",
        nino = testNino.value,
        mtdId = testMtdItId.value,
        yearOfMigration = Some("2024"),
        propertyIncome = false,
        businessData = Some(
          List(
            testBusinessDetails1,
            testBusinessDetails2
          ))
    ))
  )

  val testIncomeSource1: IncomeSource = IncomeSource(
    incomeSourceId = testBusinessId,
    accountingPeriodStartDate = LocalDate.of(2022, 4, 6),
    accountingPeriodEndDate = LocalDate.of(2023, 4, 5),
    accountingType = AccountingType("CASH")
  )

  val testIncomeSource2: IncomeSource = testIncomeSource1.copy(incomeSourceId = testBusinessId2)

  val testListOfIncomeSources: ListOfIncomeSources = ListOfIncomeSources(
    selfEmployments = List(
      testIncomeSource1,
      testIncomeSource2
    )
  )

}
