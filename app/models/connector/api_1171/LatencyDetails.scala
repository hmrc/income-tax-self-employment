/*
 * Copyright 2025 HM Revenue & Customs
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

package models.connector.api_1171

import play.api.libs.json._

case class LatencyDetails(
    latencyEndDate: String,
    taxYear1: String,
    latencyIndicator1: LatencyDetails.LatencyIndicator1.Value,
    taxYear2: String,
    latencyIndicator2: LatencyDetails.LatencyIndicator2.Value
)

object LatencyDetails {
  implicit lazy val latencyDetailsJsonFormat: Format[LatencyDetails] = Json.format[LatencyDetails]

  // noinspection TypeAnnotation
  object LatencyIndicator1 extends Enumeration {
    val A = Value("A")
    val Q = Value("Q")

    type LatencyIndicator1 = Value
    implicit lazy val LatencyIndicator1JsonFormat: Format[Value] = Format(Reads.enumNameReads(this), Writes.enumNameWrites[this.type])
  }

  // noinspection TypeAnnotation
  object LatencyIndicator2 extends Enumeration {
    val A = Value("A")
    val Q = Value("Q")

    type LatencyIndicator2 = Value
    implicit lazy val LatencyIndicator2JsonFormat: Format[Value] = Format(Reads.enumNameReads(this), Writes.enumNameWrites[this.type])
  }
}
