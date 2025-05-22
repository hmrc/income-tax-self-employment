/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors.data

import base.IntegrationBaseSpec
import models.connector.api_1171._
import play.api.libs.json.Json

trait Api1171Test extends IntegrationBaseSpec {

  val downstreamUrl = s"/registration/business-details/nino/$testNino"

  val successResponseRaw: String =
    """
      |{
      |  "processingDate": "2023-07-05T09:16:58",
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

  val successResponse: SuccessResponseSchema = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
