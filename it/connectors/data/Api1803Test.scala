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

package connectors.data

import models.common.TaxYear.asTys
import models.connector.api_1803._
import play.api.libs.json.Json
import testdata.CommonTestData

trait Api1803Test extends CommonTestData {

  val downstreamUrl = s"/income-tax/${asTys(testTaxYear)}/$testNino/self-employments/$testBusinessId/annual-summaries"

  val successResponseRaw: String = """{
                                     |  "annualAdjustments": {
                                     |    "includedNonTaxableProfits": 210,
                                     |    "basisAdjustment": 178.23,
                                     |    "overlapReliefUsed": 123.78,
                                     |    "accountingAdjustment": 678.9,
                                     |    "averagingAdjustment": 674.98,
                                     |    "outstandingBusinessIncome": 342.67,
                                     |    "balancingChargeBpra": 145.98,
                                     |    "balancingChargeOther": 457.23,
                                     |    "goodsAndServicesOwnUse": 432.9
                                     |  },
                                     |  "annualAllowances": {
                                     |    "annualInvestmentAllowance": 564.76,
                                     |    "capitalAllowanceMainPool": 456.98,
                                     |    "capitalAllowanceSpecialRatePool": 352.87,
                                     |    "zeroEmissionGoodsVehicleAllowance": 653.9,
                                     |    "businessPremisesRenovationAllowance": 452.98,
                                     |    "enhanceCapitalAllowance": 563.23,
                                     |    "allowanceOnSales": 678.9,
                                     |    "capitalAllowanceSingleAssetPool": 563.89,
                                     |    "structuredBuildingAllowance": [
                                     |      {
                                     |        "amount": 564.89,
                                     |        "firstYear": {
                                     |          "qualifyingDate": "2019-05-29",
                                     |          "qualifyingAmountExpenditure": 567.67
                                     |        },
                                     |        "building": {
                                     |          "name": "Victoria Building",
                                     |          "number": "23",
                                     |          "postCode": "TF3 5GH"
                                     |        }
                                     |      }
                                     |    ],
                                     |    "enhancedStructuredBuildingAllowance": [
                                     |      {
                                     |        "amount": 445.56,
                                     |        "firstYear": {
                                     |          "qualifyingDate": "2019-09-29",
                                     |          "qualifyingAmountExpenditure": 565.56
                                     |        },
                                     |        "building": {
                                     |          "name": "Trinity House",
                                     |          "number": "20",
                                     |          "postCode": "TF4 7HJ"
                                     |        }
                                     |      }
                                     |    ],
                                     |    "zeroEmissionsCarAllowance": 678.78
                                     |  },
                                     |  "annualNonFinancials": {
                                     |    "exemptFromPayingClass4Nics": true,
                                     |    "businessDetailsChangedRecently": true
                                     |  }
                                     |}""".stripMargin

  val successResponse = Json.parse(successResponseRaw).as[SuccessResponseSchema]
}
