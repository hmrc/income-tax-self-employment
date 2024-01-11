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

import play.api.libs.json._

/** Represents the Swagger definition for annualAllowancesType.
  * @param electricChargePointAllowance
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  * @param zeroEmissionsCarAllowance
  *   Defines a monetary value (to 2 decimal places), between 0 and 99999999999.99
  */
case class AnnualAllowancesType(
    annualInvestmentAllowance: Option[BigDecimal],
    capitalAllowanceMainPool: Option[BigDecimal],
    capitalAllowanceSpecialRatePool: Option[BigDecimal],
    zeroEmissionGoodsVehicleAllowance: Option[BigDecimal],
    businessPremisesRenovationAllowance: Option[BigDecimal],
    enhanceCapitalAllowance: Option[BigDecimal],
    allowanceOnSales: Option[BigDecimal],
    capitalAllowanceSingleAssetPool: Option[BigDecimal],
    electricChargePointAllowance: Option[BigDecimal],
    structuredBuildingAllowance: Option[List[StructuredBuildingAllowanceTypeInner]],
    enhancedStructuredBuildingAllowance: Option[List[StructuredBuildingAllowanceTypeInner]],
    zeroEmissionsCarAllowance: Option[BigDecimal],
    tradingIncomeAllowance: Option[BigDecimal]
)

object AnnualAllowancesType {
  implicit lazy val annualAllowancesTypeJsonFormat: Format[AnnualAllowancesType] = Json.format[AnnualAllowancesType]
}
