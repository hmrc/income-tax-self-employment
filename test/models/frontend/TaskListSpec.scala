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

import builders.BusinessDataBuilder
import cats.implicits._
import models.common.JourneyName.TradeDetails
import models.common.JourneyStatus.InProgress
import models.common._
import models.database.JourneyAnswers
import models.domain.{JourneyNameAndStatus, TradesJourneyStatuses}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import utils.BaseSpec._

import java.time.Instant

class TaskListSpec extends AnyWordSpecLike {
  val now: Instant = Instant.now()

  "fromJourneyAnswers" should {
    "return an empty task list when there are no answers" in {
      assert(TaskList.fromJourneyAnswers(Nil, Nil) === TaskList.empty)
    }

    "return all businesses with non statuses for a TradeDetails status" in {
      val businessId1 = BusinessId("SJPR05893938418")
      val businessId2 = BusinessId("KKKG12126914990")

      val answers = List(
        JourneyAnswers(
          mtditid,
          businessId1,
          currTaxYear,
          JourneyName.TradeDetails,
          JourneyStatus.InProgress,
          Json.obj(),
          now,
          now,
          now
        )
      )

      val businesses = List(
        BusinessDataBuilder.aBusiness.copy(businessId = businessId1.value),
        BusinessDataBuilder.aBusiness.copy(businessId = businessId2.value)
      )
      val result = TaskList.fromJourneyAnswers(answers, businesses)

      assert(
        result === TaskList(
          List(
            TradesJourneyStatuses(
              businessId1,
              BusinessDataBuilder.aBusiness.tradingName.map(TradingName(_)),
              TypeOfBusiness(BusinessDataBuilder.aBusiness.typeOfBusiness),
              AccountingType(BusinessDataBuilder.aBusiness.accountingType.getOrElse("")),
              List(JourneyNameAndStatus(TradeDetails, InProgress))
            ),
            TradesJourneyStatuses(
              businessId2,
              BusinessDataBuilder.aBusiness.tradingName.map(TradingName(_)),
              TypeOfBusiness(BusinessDataBuilder.aBusiness.typeOfBusiness),
              AccountingType(BusinessDataBuilder.aBusiness.accountingType.getOrElse("")),
              Nil
            )
          ),
          None
        ))
    }
  }
}
