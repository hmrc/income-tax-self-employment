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

package bulders

import models.common._
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.citizen_details.SuccessResponseSchema
import models.connector.{api_1171, citizen_details}
import models.domain.Business.mkBusiness
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
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
    totalAdditions = Some(200),
    totalDeductions = Some(200),
    accountingAdjustments = Some(200),
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

  lazy val aGetBusinessDataResponse = Json.parse(aGetBusinessDataResponseStr).as[api_1171.SuccessResponseSchema]
  lazy val aBusinessData            = aGetBusinessDataResponse.taxPayerDisplayResponse.businessData
  lazy val aBusinesses = aBusinessData.map(_.map(a => mkBusiness(a, aGetBusinessDataResponse.taxPayerDisplayResponse.yearOfMigration))).getOrElse(Nil)
  lazy val aBusiness   = aBusinesses.head
  lazy val aBusinessId = BusinessId(aBusiness.businessId)
  lazy val aTradesJourneyStatusesSeq = List(
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

  // Note our models use a subset of all the data pulled back by the API which is included here
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
      |    ],
      |    "propertyData": [
      |      {
      |        "incomeSourceType": "uk-property",
      |        "incomeSourceId": "KKKG12126914990",
      |        "accountingPeriodStartDate": "2021-03-11",
      |        "accountingPeriodEndDate": "2022-04-10",
      |        "tradingStartDate": "2022-02-29",
      |        "cashOrAccruals": true,
      |        "numPropRented": "7",
      |        "numPropRentedUK": "42",
      |        "numPropRentedEEA": "922",
      |        "numPropRentedNONEEA": "732",
      |        "email": "user@example.com",
      |        "cessationDate": "2022-02-29",
      |        "paperLess": true,
      |        "incomeSourceStartDate": "2022-02-29",
      |        "firstAccountingPeriodStartDate": "2021-02-29",
      |        "firstAccountingPeriodEndDate": "2022-11-30",
      |        "latencyDetails": {
      |          "latencyEndDate": "2021-11-29",
      |          "taxYear1": "2021",
      |          "latencyIndicator1": "A",
      |          "taxYear2": "2022",
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
