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

package models.common

import enumeratum._
import org.mongodb.scala.bson.{BsonString, BsonValue}

sealed abstract class JourneyStatus(override val entryName: String) extends EnumEntry

object JourneyStatus extends Enum[JourneyStatus] with utils.PlayJsonEnum[JourneyStatus] {

  val values: IndexedSeq[JourneyStatus] = findValues

  case object InProgress extends JourneyStatus("in-progress")
  case object Completed  extends JourneyStatus("completed")

  def toBson(status: JourneyStatus): BsonValue = new BsonString(status.entryName)

  def fromBson(bson: BsonValue): JourneyStatus = withName(bson.asString().getValue)
}
