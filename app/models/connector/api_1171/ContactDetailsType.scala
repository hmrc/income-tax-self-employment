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

package models.connector.api_1171

import play.api.libs.json._

/** Represents the Swagger definition for contact_details_type.
  * @param email
  *   email id
  */
case class ContactDetailsType(
    telephone: Option[String],
    mobileNo: Option[String],
    faxNo: Option[String],
    email: Option[String]
)

object ContactDetailsType {
  implicit lazy val contactDetailsTypeJsonFormat: Format[ContactDetailsType] = Json.format[ContactDetailsType]
}
