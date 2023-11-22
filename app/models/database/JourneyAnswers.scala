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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time._

case class JourneyAnswers(id: String, data: JsObject = Json.obj(), lastUpdated: Instant = Instant.now)

object JourneyAnswers {
  val reads: Reads[JourneyAnswers] =
    ((JsPath \ "_id").read[String] and
      (JsPath \ "data").read[JsObject] and
      (JsPath \ "lastUpdated").read(MongoJavatimeFormats.instantFormat))(JourneyAnswers.apply _)

  val writes: OWrites[JourneyAnswers] =
    ((JsPath \ "_id").write[String] and
      (JsPath \ "data").write[JsObject] and
      (JsPath \ "lastUpdated").write(MongoJavatimeFormats.instantFormat))(unlift(JourneyAnswers.unapply))

  implicit val formats: OFormat[JourneyAnswers] = OFormat(reads, writes)

}
