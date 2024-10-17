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

package models.connector.api_1803

import models.connector.api_1802.request.{AnnualAllowances, Building, BuildingAllowance, FirstYear}
import org.scalatest.wordspec.AnyWordSpecLike

class AnnualAllowancesTypeSpec extends AnyWordSpecLike {
  "toApi1802AnnualAllowance" should {
    "convert annual allowance type to API#1802 object" in {
      val annual = AnnualAllowancesType(
        annualInvestmentAllowance = Some(1),
        capitalAllowanceMainPool = Some(2),
        capitalAllowanceSpecialRatePool = Some(3),
        zeroEmissionGoodsVehicleAllowance = Some(4),
        businessPremisesRenovationAllowance = Some(5),
        enhanceCapitalAllowance = Some(6),
        allowanceOnSales = Some(7),
        capitalAllowanceSingleAssetPool = Some(8),
        structuredBuildingAllowance = Some(
          List(
            StructuredBuildingAllowanceTypeInner(
              1.0,
              Some(StructuredBuildingAllowanceTypeInnerFirstYear("1/1/2024", 4.0)),
              StructuredBuildingAllowanceTypeInnerBuilding(Some("name"), Some("24"), "ABC")))),
        enhancedStructuredBuildingAllowance = Some(
          List(
            StructuredBuildingAllowanceTypeInner(
              3.0,
              Some(StructuredBuildingAllowanceTypeInnerFirstYear("2/2/2024", 3.0)),
              StructuredBuildingAllowanceTypeInnerBuilding(Some("name"), Some("24"), "ABC")))),
        zeroEmissionsCarAllowance = Some(10),
        tradingIncomeAllowance = Some(11)
      )
      val result = annual.toApi1802AnnualAllowance
      assert(
        result === AnnualAllowances(
          annualInvestmentAllowance = Some(1),
          capitalAllowanceMainPool = Some(2),
          capitalAllowanceSpecialRatePool = Some(3),
          zeroEmissionGoodsVehicleAllowance = Some(4),
          businessPremisesRenovationAllowance = Some(5),
          enhanceCapitalAllowance = Some(6),
          allowanceOnSales = Some(7),
          capitalAllowanceSingleAssetPool = Some(8),
          structuredBuildingAllowance =
            Some(List(BuildingAllowance(1.0, Some(FirstYear("1/1/2024", 4.0)), Building(Some("name"), Some("24"), "ABC")))),
          enhancedStructuredBuildingAllowance =
            Some(List(BuildingAllowance(3.0, Some(FirstYear("2/2/2024", 3.0)), Building(Some("name"), Some("24"), "ABC")))),
          zeroEmissionsCarAllowance = Some(10),
          tradingIncomeAllowance = Some(11)
        ))

    }
  }
}
