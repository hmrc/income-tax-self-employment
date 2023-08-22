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

import models.api.BusinessData.{BusinessAddressDetails, BusinessContactDetails}
import models.vendor.Business
import models.vendor.Business.AccountingPeriod
import play.api.libs.json.{Json, OFormat}

case class BusinessData(
  incomeSourceId: String,
  tradingName: String,
  accountingPeriodStartDate: String,
  accountingPeriodEndDate: String,
  cashOrAccruals: String,
  tradingStartDate: String,
  seasonal: Boolean,
  cessationDate: String,
  cessationReason: String,
  paperLess: Boolean,
  businessAddressDetails: BusinessAddressDetails,
  businessContactDetails: BusinessContactDetails
) {
  def toBusiness: Business = Business(
    businessId = incomeSourceId,
    Some(tradingName),
    typeOfBusiness = "self-employment",
    accountingPeriods = Seq(AccountingPeriod(
      accountingPeriodStartDate, accountingPeriodEndDate
    )),
    accountingType = cashOrAccruals,
    commencementDate = tradingStartDate,
    cessationDate = cessationDate,
    businessAddressLineOne = businessAddressDetails.addressLine1,
    businessAddressLineTwo = businessAddressDetails.addressLine1,
    businessAddressLineThree = businessAddressDetails.addressLine1,
    businessAddressLineFour = businessAddressDetails.addressLine1,
    businessAddressPostcode = businessAddressDetails.postalCode,
    businessAddressCountryCode = businessAddressDetails.countryCode
  )
}

object BusinessData {
  implicit val businessFormat: OFormat[BusinessData] = Json.format[BusinessData]
  
  case class BusinessAddressDetails(
    addressLine1: String,
    addressLine2: String,
    addressLine3: String,
    addressLine4: String,
    postalCode: String,
    countryCode: String
  )
  object BusinessAddressDetails {
    implicit val businessAddressDetailsFormat: OFormat[BusinessAddressDetails] = Json.format[BusinessAddressDetails]
  }
  
  case class BusinessContactDetails(
    phoneNumber: String,
    mobileNumber: String,
    faxNumber: String,
    emailAddress: String
  )
  object BusinessContactDetails {
    implicit val businessContactDetailsFormats: OFormat[BusinessContactDetails] = Json.format[BusinessContactDetails]
  }
  
  case class GetBusinessDataRequest(
    safeId: String,
    nino: String,
    mtdbsa: String,
    propertyIncome: Boolean,
    businessData: Seq[BusinessData]
  )
  object GetBusinessDataRequest {
    implicit val getBusinessesDataRequestFormat: OFormat[GetBusinessDataRequest] = Json.format[GetBusinessDataRequest]
  }
}
