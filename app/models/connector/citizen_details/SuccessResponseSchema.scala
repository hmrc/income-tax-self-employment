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

package models.connector.citizen_details

import play.api.libs.json._

import java.time.LocalDate

case class SuccessResponseSchema(name: LegalNames, ids: Ids, dateOfBirth: String) {

  def parseDoBToLocalDate: LocalDate = {
    val (year, month, day) = (dateOfBirth.substring(4, 8), dateOfBirth.substring(2, 4), dateOfBirth.substring(0, 2))
    LocalDate.parse(s"$year-$month-$day")
  }

}

object SuccessResponseSchema {
  implicit lazy val successResponseSchemaJsonFormat: Format[SuccessResponseSchema] = Json.format[SuccessResponseSchema]
}

case class LegalNames(current: Name, previous: List[Name])
object LegalNames {
  implicit lazy val format: Format[LegalNames] = Json.format[LegalNames]
}
case class Name(firstName: String, lastName: String)
object Name {
  implicit lazy val format: Format[Name] = Json.format[Name]
}
case class Ids(nino: String)
object Ids {
  implicit lazy val format: Format[Ids] = Json.format[Ids]
}
