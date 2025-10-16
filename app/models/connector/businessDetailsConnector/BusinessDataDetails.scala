/*
 * Copyright 2025 HM Revenue & Customs
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

package models.connector.businessDetailsConnector

import play.api.libs.json._
import play.api.libs.functional.syntax._


/** Represents the Swagger definition for business_data_details.
  * @param incomeSourceId
  *   incomeSourceId
  * @param cashOrAccruals
  *   Cash Or Accruals, true for Accruals, false for Cash
  * @param seasonal
  *   When true indicates is a seasonal business. For example, ski material rental
  * @param paperLess
  *   PaperLess
  */
case class BusinessDataDetails(
    incomeSourceId: String,
    incomeSource: Option[String],
    accountingPeriodStartDate: String,
    accountingPeriodEndDate: String,
    tradingName: Option[String],
    businessAddressDetails: Option[BusinessDataDetailsBusinessAddressDetails],
    businessContactDetails: Option[ContactDetailsType],
    tradingStartDate: Option[String],
    cashOrAccruals: Option[Boolean],
    seasonal: Option[Boolean],
    cessationDate: Option[String],
    paperLess: Option[Boolean],
    incomeSourceStartDate: Option[String],
    firstAccountingPeriodStartDate: Option[String],
    firstAccountingPeriodEndDate: Option[String],
    latencyDetails: Option[LatencyDetails]
)

object BusinessDataDetails {
  implicit val businessDataDetailsJsonFormat: Format[BusinessDataDetails] = Json.format[BusinessDataDetails]

  val hipReads: Reads[BusinessDataDetails] = (
    (JsPath \ "incomeSourceId").read[String] and
      (JsPath \ "incomeSource").readNullable[String] and
      (JsPath \ "accPeriodSDate").read[String] and
      (JsPath \ "accPeriodEDate").read[String] and
      (JsPath \ "tradingName").readNullable[String] and
      (JsPath \ "businessAddressDetails").readNullable[BusinessDataDetailsBusinessAddressDetails] and
      (JsPath \ "businessContactDetails").readNullable[ContactDetailsType] and
      (JsPath \ "tradingSDate").readNullable[String] and
      (JsPath \ "cashOrAccrualsFlag").readNullable[Boolean] and
      (JsPath \ "seasonalFlag").readNullable[Boolean] and
      (JsPath \ "cessationDate").readNullable[String] and
      (JsPath \ "paperLessFlag").readNullable[Boolean] and
      (JsPath \ "incomeSourceStartDate").readNullable[String] and
      (JsPath \ "firstAccountingPeriodStartDate").readNullable[String] and
      (JsPath \ "firstAccountingPeriodEndDate").readNullable[String] and
      (JsPath \ "latencyDetails").readNullable[LatencyDetails]
    )(BusinessDataDetails.apply _)

  val hipFormat: Format[BusinessDataDetails] = Format(hipReads, businessDataDetailsJsonFormat.writes(_))

}
