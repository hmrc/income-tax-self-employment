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

package models.connector.api_1505

import play.api.libs.json.{Format, Json}

// TODO SASS-10335 make necessary changes to these models. Split them into separate files if needed like other API folders do
case class CreateLossClaimRequestBody(incomeSourceId: String, reliefClaimed: String, taxYear: Int)
object CreateLossClaimRequestBody {
  implicit lazy val format: Format[CreateLossClaimRequestBody] = Json.format[CreateLossClaimRequestBody]
}

case class CreateLossClaimResponseBody(claimId: String)
object CreateLossClaimResponseBody {
  implicit lazy val format: Format[CreateLossClaimResponseBody] = Json.format[CreateLossClaimResponseBody]
}
