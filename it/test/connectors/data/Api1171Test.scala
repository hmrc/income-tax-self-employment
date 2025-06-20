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

package connectors.data

import models.connector.businessDetailsConnector.{BusinessDetailsHipSuccessWrapper, BusinessDetailsSuccessResponseSchema}
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1171Test extends CommonTestData {

  val downstreamUrl: String = s"/registration/business-details/nino/$testNino"

  val api1171HipResponseJson: String =
    """
      |{
      |  "success": {
      |    "processingDate": "2023-07-05T09:16:58",
      |    "taxPayerDisplayResponse": {
      |      "safeId": "EK3074559847852",
      |      "nino": "FI290077A",
      |      "mtdId": "NIUT24195581820",
      |      "yearOfMigration": "2022",
      |      "propertyIncomeFlag": true,
      |      "businessData": [
      |       {
      |          "incomeSourceId": "SJPR05893938418",
      |          "incomeSource": "string",
      |          "accPeriodSDate": "2023-02-29",
      |          "accPeriodEDate": "2024-02-29",
      |          "tradingName": "string",
      |          "businessAddressDetails": {
      |            "addressLine1": "string",
      |            "addressLine2": "string",
      |            "addressLine3": "string",
      |            "addressLine4": "string",
      |            "postalCode": "string",
      |            "countryCode": "GB"
      |          },
      |          "businessContactDetails": {
      |            "telephone": "string",
      |            "mobileNo": "string",
      |            "faxNo": "string",
      |            "email": "user@example.com"
      |          },
      |          "tradingSDate": "2023-04-06",
      |          "cashOrAccrualsFlag": true,
      |          "seasonalFlag": true,
      |          "cessationDate": "2024-04-05",
      |          "paperLessFlag": true,
      |          "incomeSourceStartDate": "2020-08-13",
      |          "firstAccountingPeriodStartDate": "2019-09-30",
      |          "firstAccountingPeriodEndDate": "2020-02-29",
      |          "latencyDetails": {
      |            "latencyEndDate": "2020-02-27",
      |            "taxYear1": "2019",
      |            "latencyIndicator1": "A",
      |            "taxYear2": "2020",
      |            "latencyIndicator2": "A"
      |          }
      |        }
      |      ]
      |    }
      |  }
      |}
      |""".stripMargin

  val api1171HipResponse: BusinessDetailsHipSuccessWrapper = Json.parse(api1171HipResponseJson).as[BusinessDetailsHipSuccessWrapper]

  val api1171IfsResponseJson: String =
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
      |     {
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

  val api1171IfsResponse: BusinessDetailsSuccessResponseSchema = Json.parse(api1171IfsResponseJson).as[BusinessDetailsSuccessResponseSchema]

}
