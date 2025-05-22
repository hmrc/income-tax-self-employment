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

/** Represents the Swagger definition for foreignAddressType.
  * @param addressLine1
  *   Address line 1
  * @param addressLine2
  *   Address line 2
  * @param addressLine3
  *   Address line 3
  * @param addressLine4
  *   Address line 4
  * @param postalCode
  *   Postal code
  */
case class ForeignAddressType(
    addressLine1: String,
    addressLine2: Option[String],
    addressLine3: Option[String],
    addressLine4: Option[String],
    postalCode: Option[String],
    countryCode: String
)

object ForeignAddressType {
  implicit lazy val foreignAddressTypeJsonFormat: Format[ForeignAddressType] = Json.format[ForeignAddressType]
}
