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

package models.vendor

import models.vendor.Business.AccountingPeriod
import play.api.libs.json.{Json, OFormat}

case class Business(
  businessId: String,
  tradingName: Option[String],
  typeOfBusiness: String,
  accountingPeriods: Seq[AccountingPeriod],
  accountingType: String,
  commencementDate: String,
  cessationDate: String,
  businessAddressLineOne: String,
  businessAddressLineTwo: String,
  businessAddressLineThree: String,
  businessAddressLineFour: String,
  businessAddressPostcode: String,
  businessAddressCountryCode: String
)

object Business {
  implicit val businessFormat: OFormat[Business] = Json.format[Business]
  
  case class AccountingPeriod(start: String, end: String)
  object AccountingPeriod {
    implicit val accountingPeriodFormat: OFormat[AccountingPeriod] = Json.format[AccountingPeriod]
  }
}
