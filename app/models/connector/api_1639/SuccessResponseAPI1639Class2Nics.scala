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
  * Voluntary contributions.
  */
case class SuccessResponseAPI1639Class2Nics(
  class2VoluntaryContributions: Option[Boolean]
)

object SuccessResponseAPI1639Class2Nics {
  implicit lazy val successResponseAPI1639Class2NicsJsonFormat: Format[SuccessResponseAPI1639Class2Nics] = Json.format[SuccessResponseAPI1639Class2Nics]
}

