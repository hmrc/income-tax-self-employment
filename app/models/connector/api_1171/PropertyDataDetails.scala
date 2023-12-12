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

package models.connector.api_1171

import play.api.libs.json._

/** Represents the Swagger definition for property_data_details.
  * @param incomeSourceType
  *   Possible values: 02-uk-property, 03-foreign-property
  * @param incomeSourceId
  *   incomeSourceId
  * @param cashOrAccruals
  *   Cash Or Accruals, true for Accruals,false for Cash
  * @param email
  *   email id
  * @param paperLess
  *   PaperLess
  */
case class PropertyDataDetails(
    incomeSourceType: Option[PropertyDataDetails.IncomeSourceType.Value],
    incomeSourceId: String,
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingStartDate: Option[String],
    cashOrAccruals: Option[Boolean],
    numPropRented: Option[String],
    numPropRentedUK: Option[String],
    numPropRentedEEA: Option[String],
    numPropRentedNONEEA: Option[String],
    email: Option[String],
    cessationDate: Option[String],
    paperLess: Option[Boolean],
    incomeSourceStartDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails]
)

object PropertyDataDetails {
  implicit lazy val propertyDataDetailsJsonFormat: Format[PropertyDataDetails] = Json.format[PropertyDataDetails]

  // noinspection TypeAnnotation
  object IncomeSourceType extends Enumeration {
    val UkProperty      = Value("uk-property")
    val ForeignProperty = Value("foreign-property")

    type IncomeSourceType = Value
    implicit lazy val IncomeSourceTypeJsonFormat: Format[Value] = Format(Reads.enumNameReads(this), Writes.enumNameWrites[this.type])
  }
}
