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

package models.connector.api_1894

import play.api.libs.json._

/** Represents the Swagger definition for requestBody.
  * @param from
  *   Defines a date in the format yyyy-mm-dd
  * @param to
  *   Defines a date in the format yyyy-mm-dd
  */
case class RequestBody(
    from: String,
    to: String,
    financials: Option[FinancialsType]
)

object RequestBody {
  implicit lazy val requestBodyJsonFormat: Format[RequestBody] = Json.format[RequestBody]
}
