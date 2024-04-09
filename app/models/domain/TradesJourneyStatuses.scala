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

package models.domain

import models.common.{AccountingType, BusinessId, TradingName}
import play.api.libs.json._

/** @param isPrepop
  *   \- isPrepop is true when the data were modified via software and will not be possible to modify using our application, it's false if a standard
  *   journey possible to be modified by our app
  */
case class TradesJourneyStatuses(businessId: BusinessId,
                                 tradingName: Option[TradingName],
                                 accountingType: AccountingType,
                                 journeyStatuses: List[JourneyNameAndStatus])
x
object TradesJourneyStatuses {
  implicit val format: OFormat[TradesJourneyStatuses] = Json.format[TradesJourneyStatuses]
}
