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

package models.connector.api_1639

import play.api.libs.json._

/**
  * Represents the Swagger definition for successResponseAPI1639.
  */
case class SuccessResponseAPI1639(
  taxAvoidance: Option[List[SuccessResponseAPI1639TaxAvoidanceInner]],
  class2Nics: Option[SuccessResponseAPI1639Class2Nics]
)

object SuccessResponseAPI1639 {
  implicit lazy val successResponseAPI1639JsonFormat: Format[SuccessResponseAPI1639] = Json.format[SuccessResponseAPI1639]
}

