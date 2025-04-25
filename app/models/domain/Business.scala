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

package models.domain

import models.connector.businessDetailsConnector.BusinessDataDetails
import models.domain.Business.{AccountingPeriod, LatencyDetails}
import play.api.libs.json.{Json, OFormat}

case class Business(
    businessId: String,
    typeOfBusiness: String,
    tradingName: Option[String],
    yearOfMigration: Option[String],
    accountingPeriods: Seq[AccountingPeriod],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails],
    accountingType: Option[String],
    commencementDate: Option[String],
    cessationDate: Option[String],
    businessAddressLineOne: Option[String],
    businessAddressLineTwo: Option[String],
    businessAddressLineThree: Option[String],
    businessAddressLineFour: Option[String],
    businessAddressPostcode: Option[String],
    businessAddressCountryCode: Option[String]
)

object Business {

  implicit val businessFormat: OFormat[Business] = Json.format[Business]

  case class AccountingPeriod(start: String, end: String)
  object AccountingPeriod {
    implicit val accountingPeriodFormat: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
  }

  case class LatencyDetails(latencyEndDate: String, taxYear1: String, latencyIndicator1: String, taxYear2: String, latencyIndicator2: String)
  object LatencyDetails {
    implicit val latencyDetailsFormat: OFormat[LatencyDetails] = Json.format[LatencyDetails]
  }

  private val typeOfBusiness                         = "self-employment"
  private val latencyIndicatorType: String => String = latencyIndicator => if (latencyIndicator == "Q") "Quarterly" else "Annual"

  def mkBusiness(details: BusinessDataDetails, yearOfMigration: Option[String]): Business =
    Business(
      businessId = details.incomeSourceId,
      typeOfBusiness,
      details.tradingName,
      yearOfMigration,
      accountingPeriods = Seq(
        AccountingPeriod(
          details.accountingPeriodStartDate,
          details.accountingPeriodEndDate
        )),
      details.firstAccountingPeriodStartDate,
      details.firstAccountingPeriodEndDate,
      details.latencyDetails.map(ld =>
        LatencyDetails(
          ld.latencyEndDate,
          ld.taxYear1,
          latencyIndicatorType(ld.latencyIndicator1.toString),
          ld.taxYear2,
          latencyIndicatorType(ld.latencyIndicator2.toString)
        )),
      accountingType = details.cashOrAccruals.map(if (_) "ACCRUAL" else "CASH"),
      commencementDate = details.tradingStartDate,
      details.cessationDate,
      businessAddressLineOne = details.businessAddressDetails.map(_.addressLine1),
      businessAddressLineTwo = details.businessAddressDetails.flatMap(_.addressLine2),
      businessAddressLineThree = details.businessAddressDetails.flatMap(_.addressLine3),
      businessAddressLineFour = details.businessAddressDetails.flatMap(_.addressLine4),
      businessAddressPostcode = details.businessAddressDetails.map(_.postalCode),
      businessAddressCountryCode = details.businessAddressDetails.map(_.countryCode)
    )
}
