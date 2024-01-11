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

import play.api.libs.json.{Json, OFormat}

// The `tradingIncomeAllowance` cannot be present with other allowances. It has a min value of 0, max of 1000 (2 dp).
case class AnnualAllowances(annualInvestmentAllowance: Option[BigDecimal],
                            capitalAllowanceMainPool: Option[BigDecimal],
                            capitalAllowanceSpecialRatePool: Option[BigDecimal],
                            zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
                            businessPremisesRenovationAllowance: Option[BigDecimal],
                            enhanceCapitalAllowance: Option[BigDecimal],
                            allowanceOnSales: Option[BigDecimal],
                            capitalAllowanceSingleAssetPool: Option[BigDecimal],
                            electricChargePointAllowance: Option[BigDecimal],
                            structuredBuildingAllowance: Option[List[BuildingAllowance]],
                            enhancedStructuredBuildingAllowance: Option[List[BuildingAllowance]],
                            zeroEmissionsCarAllowance: Option[BigDecimal],
                            tradingIncomeAllowance: Option[BigDecimal])

object AnnualAllowances {
  implicit val format: OFormat[AnnualAllowances] = Json.format[AnnualAllowances]

  val empty: AnnualAllowances = AnnualAllowances(None, None, None, None, None, None, None, None, None, None, None, None, None)
}
