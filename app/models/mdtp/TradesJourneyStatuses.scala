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

package models.mdtp

import models.mdtp.TradesJourneyStatuses.JourneyStatus
import play.api.libs.json._

case class TradesJourneyStatuses(businessId: String,
                                 tradingName: Option[String],
                                 journeyStatuses: Seq[JourneyStatus]
                                )

object TradesJourneyStatuses {
  implicit val format: OFormat[TradesJourneyStatuses] = Json.format[TradesJourneyStatuses]

  def apply(tradesJourneyStatuses: (String, Option[String], Seq[(String, Boolean)])): TradesJourneyStatuses = {
    TradesJourneyStatuses(
      businessId = tradesJourneyStatuses._1,
      tradingName = tradesJourneyStatuses._2,
      journeyStatuses = tradesJourneyStatuses._3.map(tgs => JourneyStatus(tgs._1, tgs._2))
    )
  }

  case class JourneyStatus(journey: String, completedState: Boolean)

  object JourneyStatus {
    implicit val format: OFormat[JourneyStatus] = Json.format[JourneyStatus]
  }
}

