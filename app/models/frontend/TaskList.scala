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

package models.frontend

import models.common._
import models.database.JourneyAnswers
import models.domain.{Business, JourneyNameAndStatus, TradesJourneyStatuses}
import play.api.libs.json.{Json, OFormat}

final case class TaskList(tradeDetails: Option[JourneyNameAndStatus], businesses: List[TradesJourneyStatuses])

object TaskList {
  implicit val format: OFormat[TaskList] = Json.format[TaskList]

  val empty: TaskList = TaskList(None, Nil)

  def fromJourneyAnswers(userJourneyAnswers: List[JourneyAnswers], businesses: List[Business]): TaskList = {
    val groupedByBusinessId: Map[BusinessId, Seq[JourneyAnswers]] = userJourneyAnswers.groupBy(_.businessId)
    val tradingDetailsStatus = groupedByBusinessId
      .get(BusinessId.tradeDetailsId)
      .flatMap(
        _.toList.headOption
          .map(a => JourneyNameAndStatus(a.journey, a.status)))

    val perBusinessStatuses = businesses.map { business =>
      val currentJourneys = groupedByBusinessId
        .get(BusinessId(business.businessId))
        .map(_.toList)
        .getOrElse(Nil)

      TradesJourneyStatuses(
        BusinessId(business.businessId),
        business.tradingName.map(TradingName(_)),
        AccountingType(business.accountingType.getOrElse("")),
        currentJourneys.map(j => JourneyNameAndStatus(j.journey, j.status))
      )
    }

    TaskList(tradingDetailsStatus, perBusinessStatuses)
  }
}
