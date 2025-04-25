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

package builders

import models.common._
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.api_2085.{IncomeSource, ListOfIncomeSources}
import models.connector.businessDetailsConnector.BusinessDataDetails
import models.connector.citizen_details.SuccessResponseSchema
import models.connector.{businessDetailsConnector, citizen_details}
import models.domain.Business.mkBusiness
import models.domain.{Business, JourneyNameAndStatus, TradesJourneyStatuses}
import models.frontend.adjustments.NetBusinessProfitOrLossValues
import play.api.libs.json.Json

import java.time.LocalDate

object BusinessDataBuilder {

  lazy val aUserDateOfBirth: LocalDate                      = LocalDate.of(1997, 7, 30)
  lazy val citizenDetailsDateOfBirth: String                = "30071997"
  lazy val getCitizenDetailsResponse: SuccessResponseSchema = Json.parse(getCitizenDetailsResponseStr).as[citizen_details.SuccessResponseSchema]

  lazy val aBusinessIncomeSourcesSummaryResponse: BusinessIncomeSourcesSummaryResponse = BusinessIncomeSourcesSummaryResponse(
    aBusinessId.value,
    totalIncome = 200,
    totalExpenses = 200,
    netProfit = 200,
    netLoss = 200,
    totalAdditions = Option(BigDecimal(200)),
    totalDeductions = Option(BigDecimal(200)),
    accountingAdjustments = Option(BigDecimal(200)),
    taxableProfit = 200,
    taxableLoss = 200
  )

  lazy val aNetBusinessProfitValues: NetBusinessProfitOrLossValues = NetBusinessProfitOrLossValues(
    turnover = 100,
    incomeNotCountedAsTurnover = 20,
    totalExpenses = 50,
    netProfit = 400,
    netLoss = 0,
    balancingCharge = 10,
    goodsAndServicesForOwnUse = 50,
    disallowableExpenses = 10,
    totalAdditions = 60,
    capitalAllowances = 0,
    turnoverNotTaxableAsBusinessProfit = 50,
    totalDeductions = 70,
    outstandingBusinessIncome = 66
  )

  lazy val aGetBusinessDataResponse: businessDetailsConnector.SuccessResponseSchema =
    Json.parse(aGetBusinessDataResponseStr).as[businessDetailsConnector.SuccessResponseSchema]
  lazy val aBusinesses: List[Business] =
    aBusinessData.map(_.map(a => mkBusiness(a, aGetBusinessDataResponse.taxPayerDisplayResponse.yearOfMigration))).getOrElse(Nil)

  lazy val aBusinessData: Option[List[BusinessDataDetails]] = aGetBusinessDataResponse.taxPayerDisplayResponse.businessData
  lazy val aBusiness: Business                              = aBusinesses.head
  lazy val aBusinessId: BusinessId                          = BusinessId(aBusiness.businessId)

  lazy val aTradesJourneyStatusesSeq: List[TradesJourneyStatuses] = List(
    TradesJourneyStatuses(
      aBusinessId,
      aBusiness.tradingName.map(TradingName(_)),
      TypeOfBusiness(aBusiness.typeOfBusiness),
      AccountingType(aBusiness.accountingType.getOrElse("")),
      List(
        JourneyNameAndStatus(JourneyName.Income, JourneyStatus.Completed),
        JourneyNameAndStatus(JourneyName.ExpensesTailoring, JourneyStatus.Completed)
      )
    )
  )

  lazy val listOfIncomeSources: ListOfIncomeSources =
    ListOfIncomeSources(
      List(
        IncomeSource(BusinessId("FHL000000000100"), LocalDate.parse("2024-04-06"), LocalDate.parse("2025-04-05"), AccountingType("CASH")),
        IncomeSource(BusinessId("FHL000000000101"), LocalDate.parse("2024-04-10"), LocalDate.parse("2025-04-14"), AccountingType("CASH"))
      ))

  lazy val aGetBusinessDataResponseStr: String =
    """
      |{
      |  "processingDate": "2023-07-05T09:16:58.655Z",
      |  "taxPayerDisplayResponse": {
      |    "safeId": "EK3074559847852",
      |    "nino": "FI290077A",
      |    "mtdId": "NIUT24195581820",
      |    "yearOfMigration": "2022",
      |    "propertyIncome": true,
      |    "businessData": [
      |      {
      |        "incomeSourceId": "SJPR05893938418",
      |        "incomeSource": "string",
      |        "accountingPeriodStartDate": "2023-02-29",
      |        "accountingPeriodEndDate": "2024-02-29",
      |        "tradingName": "string",
      |        "businessAddressDetails": {
      |          "addressLine1": "string",
      |          "addressLine2": "string",
      |          "addressLine3": "string",
      |          "addressLine4": "string",
      |          "postalCode": "string",
      |          "countryCode": "GB"
      |        },
      |        "businessContactDetails": {
      |          "telephone": "string",
      |          "mobileNo": "string",
      |          "faxNo": "string",
      |          "email": "user@example.com"
      |        },
      |        "tradingStartDate": "2023-04-06",
      |        "cashOrAccruals": true,
      |        "seasonal": true,
      |        "cessationDate": "2024-04-05",
      |        "paperLess": true,
      |        "incomeSourceStartDate": "2020-08-13",
      |        "firstAccountingPeriodStartDate": "2019-09-30",
      |        "firstAccountingPeriodEndDate": "2020-02-29",
      |        "latencyDetails": {
      |          "latencyEndDate": "2020-02-27",
      |          "taxYear1": "2019",
      |          "latencyIndicator1": "A",
      |          "taxYear2": "2020",
      |          "latencyIndicator2": "A"
      |        }
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin

  lazy val getCitizenDetailsResponseStr: String =
    s"""{
     |   "name": {
     |      "current": {
     |         "firstName": "Mike",
     |         "lastName": "Wazowski"
     |      },
     |      "previous": []
     |   },
     |   "ids": {
     |      "nino": "AA055075C"
     |   },
     |   "dateOfBirth": "30071997"
     |}""".stripMargin
}
