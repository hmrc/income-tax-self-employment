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

package models.frontend.expenses.officeSupplies

import play.api.libs.json._

case class OfficeSuppliesJourneyAnswers(officeSuppliesAmount: BigDecimal, officeSuppliesDisallowableAmount: Option[BigDecimal])

object OfficeSuppliesJourneyAnswers {
  implicit val reads: Reads[OfficeSuppliesJourneyAnswers] = Json.reads[OfficeSuppliesJourneyAnswers]

  implicit val writes: OWrites[OfficeSuppliesJourneyAnswers] = Json.writes[OfficeSuppliesJourneyAnswers]

  implicit val formats: OFormat[OfficeSuppliesJourneyAnswers] = OFormat(reads, writes)
}
