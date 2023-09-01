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

package models.api

import models.api.BusinessData.{BusinessAddressDetails, TaxPayerDisplayResponse, latencyIndicatorType, typeOfBusiness}
import models.mdtp.Business
import models.mdtp.Business.{AccountingPeriod, LatencyDetails}
import play.api.libs.json.{Json, OFormat}

case class BusinessData(
  incomeSourceId: String,
  tradingName: Option[String],
  accountingPeriodStartDate: String,
  accountingPeriodEndDate: String,
  firstAccountingPeriodStartDate: Option[String],
  firstAccountingPeriodEndDate:  Option[String],
  latencyDetails: Option[LatencyDetails],
  tradingStartDate: Option[String],
  cashOrAccruals: Option[Boolean],
  cessationDate: Option[String],
  businessAddressDetails: BusinessAddressDetails
) {
  def toBusiness(taxPDR: TaxPayerDisplayResponse): Business = Business(
    businessId = incomeSourceId,
    typeOfBusiness,
    tradingName,
    taxPDR.yearOfMigration,
    accountingPeriods = Seq(AccountingPeriod(
      accountingPeriodStartDate, accountingPeriodEndDate
    )),
    firstAccountingPeriodStartDate, firstAccountingPeriodEndDate,
    latencyDetails.map(ld => ld.copy(latencyIndicator1 = latencyIndicatorType(ld.latencyIndicator1),
                                           latencyIndicator2 = latencyIndicatorType(ld.latencyIndicator2))),
    accountingType = cashOrAccruals.map(if (_) "ACCRUAL" else "CASH"),
    commencementDate = tradingStartDate,
    cessationDate,
    businessAddressLineOne = businessAddressDetails.addressLine1,
    businessAddressLineTwo = businessAddressDetails.addressLine2,
    businessAddressLineThree = businessAddressDetails.addressLine3,
    businessAddressLineFour = businessAddressDetails.addressLine4,
    businessAddressPostcode = businessAddressDetails.postalCode,
    businessAddressCountryCode = businessAddressDetails.countryCode
  )
}

object BusinessData {
  implicit val businessFormat: OFormat[BusinessData] = Json.format[BusinessData]
  
  case class BusinessAddressDetails(
    addressLine1: String,
    addressLine2: Option[String],
    addressLine3: Option[String],
    addressLine4: Option[String],
    postalCode: Option[String],
    countryCode: String
  )
  object BusinessAddressDetails {
    implicit val businessAddressDetailsFormat: OFormat[BusinessAddressDetails] = Json.format[BusinessAddressDetails]
  }
  
  case class TaxPayerDisplayResponse(
    safeId: String,
    nino: String,
    mtdId: String,
    yearOfMigration: Option[String],
    businessData: Seq[BusinessData]
  )
  object TaxPayerDisplayResponse {
    implicit val taxPayerDisplayResponseFormat: OFormat[TaxPayerDisplayResponse] = Json.format[TaxPayerDisplayResponse]
  }
  case class GetBusinessDataRequest(processingDate: String, taxPayerDisplayResponse:TaxPayerDisplayResponse)
  object GetBusinessDataRequest {
    implicit val getBusinessesDataRequestFormat: OFormat[GetBusinessDataRequest] = Json.format[GetBusinessDataRequest]
  }
  
  val latencyIndicatorType: String => String = (latencyIndicator) => if (latencyIndicator == "Q") "Quarterly" else "Annual"
  val typeOfBusiness = "self-employment"
}
