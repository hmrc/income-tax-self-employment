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

package models.commonTaskList

import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class TaskListData(mtdItId: String, taxYear: Int, data: JsObject = Json.obj(), lastUpdated: Instant = Instant.now)

object TaskListData {

  val reads: Reads[TaskListData] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "mtdItId").read[String] and
        (__ \ "taxYear").read[Int].filter(_.toString.matches("^20\\d{2}$")) and
        (__ \ "data").read[JsObject] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    )(TaskListData.apply _)
  }

  val writes: OWrites[TaskListData] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "mtdItId").write[String] and
        (__ \ "taxYear").write[Int] and
        (__ \ "data").write[JsObject] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
    )(unlift(TaskListData.unapply))
  }

  implicit val format: OFormat[TaskListData] = OFormat(reads, writes)
}
