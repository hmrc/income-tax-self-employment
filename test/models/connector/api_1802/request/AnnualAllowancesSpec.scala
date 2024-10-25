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

package models.connector.api_1802.request

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, JsSuccess, Json}

class AnnualAllowancesSpec extends AnyWordSpec with Matchers {

  val testBuildingAllowance: BuildingAllowance = BuildingAllowance(1.0, Some(FirstYear("1/1/2024", 4.0)), Building(Some("name"), Some("24"), "ABC"))

  val testOtherAnnualAllowances: AnnualAllowances = AnnualAllowances(
    annualInvestmentAllowance = Some(1),
    capitalAllowanceMainPool = Some(2),
    capitalAllowanceSpecialRatePool = Some(3),
    zeroEmissionGoodsVehicleAllowance = Some(4),
    businessPremisesRenovationAllowance = Some(5),
    enhanceCapitalAllowance = Some(6),
    allowanceOnSales = Some(7),
    capitalAllowanceSingleAssetPool = Some(8),
    structuredBuildingAllowance = Some(List(testBuildingAllowance)),
    enhancedStructuredBuildingAllowance = Some(List(testBuildingAllowance)),
    zeroEmissionsCarAllowance = Some(11),
    tradingIncomeAllowance = None
  )

  val testOtherAnnualAllowancesJson: JsObject = Json.obj(
    "annualInvestmentAllowance"           -> 1,
    "capitalAllowanceMainPool"            -> 2,
    "capitalAllowanceSpecialRatePool"     -> 3,
    "zeroEmissionGoodsVehicleAllowance"   -> 4,
    "businessPremisesRenovationAllowance" -> 5,
    "enhanceCapitalAllowance"             -> 6,
    "allowanceOnSales"                    -> 7,
    "capitalAllowanceSingleAssetPool"     -> 8,
    "structuredBuildingAllowance"         -> Json.arr(Json.toJson(testBuildingAllowance)),
    "enhancedStructuredBuildingAllowance" -> Json.arr(Json.toJson(testBuildingAllowance)),
    "zeroEmissionsCarAllowance"           -> 11
  )

  val testTradingIncomeAllowance: AnnualAllowances = AnnualAllowances(
    annualInvestmentAllowance = None,
    capitalAllowanceMainPool = None,
    capitalAllowanceSpecialRatePool = None,
    zeroEmissionGoodsVehicleAllowance = None,
    businessPremisesRenovationAllowance = None,
    enhanceCapitalAllowance = None,
    allowanceOnSales = None,
    capitalAllowanceSingleAssetPool = None,
    structuredBuildingAllowance = None,
    enhancedStructuredBuildingAllowance = None,
    zeroEmissionsCarAllowance = None,
    tradingIncomeAllowance = Some(12)
  )

  val testTradingIncomeAllowanceJson: JsObject = Json.obj("tradingIncomeAllowance" -> 12)

  "Reads" must {
    "successfully parse Json to an AnnualAllowances model when" when {
      "tradingIncomeAllowance is the only field present" in {
        val result = Json.fromJson(testTradingIncomeAllowanceJson)(AnnualAllowances.format)

        result mustBe JsSuccess(testTradingIncomeAllowance)
      }
      "everything except tradingIncomeAllowance is present" in {
        val result = Json.fromJson(testOtherAnnualAllowancesJson)(AnnualAllowances.format)

        result mustBe JsSuccess(testOtherAnnualAllowances)
      }
      "when all fields are present (not a valid business case)" in {
        val result = Json.fromJson(testOtherAnnualAllowancesJson ++ Json.obj("tradingIncomeAllowance" -> "12"))(AnnualAllowances.format)

        result mustBe JsSuccess(testOtherAnnualAllowances.copy(tradingIncomeAllowance = Some(12)))
      }
    }
  }

  "Writes" must {
    "successfully write Json for an AnnualAllowances model when" when {
      "tradingIncomeAllowance is the only field present" in {
        val result = Json.toJson(testTradingIncomeAllowance)

        result mustBe testTradingIncomeAllowanceJson
      }
      "everything except tradingIncomeAllowance is present" in {
        val result = Json.toJson(testOtherAnnualAllowances)

        result mustBe testOtherAnnualAllowancesJson
      }
    }

    /* This is to ensure compliance with the API spec for API# 1802, which only allows
       either "tradingIncomeAllowance" to be present, or other allowances, but not both. */
    "Only write the Json for the trading income allowance answer" when {
      "when all fields, including tradingIncomeAllowance, are present" in {
        val result = Json.toJson(testOtherAnnualAllowances.copy(tradingIncomeAllowance = Some(12)))

        result mustBe testTradingIncomeAllowanceJson
      }
    }
  }

}
