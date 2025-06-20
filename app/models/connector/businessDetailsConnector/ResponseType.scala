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

import models.common.BusinessId
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ResponseType(
    safeId: String,
    nino: String,
    mtdId: String,
    yearOfMigration: Option[String],
    propertyIncome: Boolean,
    businessData: Option[List[BusinessDataDetails]]
) {
  def getMaybeSingleBusinessId: Option[BusinessId] = businessData.flatMap { businessList =>
    businessList match {
      case singleBusiness :: Nil => Some(BusinessId(singleBusiness.incomeSourceId))
      case _                     => None
    }

  }
}

object ResponseType {
  implicit val responseTypeJsonFormat: Format[ResponseType] = Json.format[ResponseType]

  val hipReads: Reads[ResponseType] = (
    (JsPath \ "safeId").read[String] and
    (JsPath \ "nino").read[String] and
    (JsPath \ "mtdId").read[String] and
    (JsPath \ "yearOfMigration").readNullable[String] and
    (JsPath \ "propertyIncomeFlag").read[Boolean] and
    (JsPath \ "businessData").readNullable[List[BusinessDataDetails]](Reads.list(BusinessDataDetails.hipFormat))
  )(ResponseType.apply _)

  val hipFormat: Format[ResponseType] = Format(hipReads, responseTypeJsonFormat.writes)

}
