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

package models.database

import models.database.JourneyState.JourneyStateData
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDate
import java.util.UUID

// TODO Merge with answer collection
case class JourneyState(
    id: String = UUID.randomUUID().toString,
    journeyStateData: JourneyStateData,
    lastUpdated: LocalDate = LocalDate.now()
)

object JourneyState {

  case class JourneyStateData(
      businessId: String,
      journey: String,
      taxYear: Int,
      completedState: Boolean
  )
  object JourneyStateData {
    implicit val journeyStateDataFormat: OFormat[JourneyStateData] = Json.format[JourneyStateData]
  }

  implicit val mongoJourneyStateFormat: OFormat[JourneyState] = {
    import play.api.libs.functional.syntax._

    val reads: Reads[JourneyState] =
      (
        (__ \ "_id").read[String] and
          (__ \ "journeyStateData").read[JourneyStateData] and
          (__ \ "lastUpdated").read(MongoJavatimeFormats.localDateFormat)
      )(JourneyState.apply _)

    val writes: OWrites[JourneyState] =
      (
        (__ \ "_id").write[String] and
          (__ \ "journeyStateData").write[JourneyStateData] and
          (__ \ "lastUpdated").write(MongoJavatimeFormats.localDateFormat)
      )(unlift(JourneyState.unapply))

    OFormat(reads, writes)
  }
}
