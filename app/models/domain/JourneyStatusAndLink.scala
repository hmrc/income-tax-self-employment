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

import models.common.JourneyStatus.NotStarted
import models.common.{BusinessId, JourneyName, JourneyStatus, TaxYear}
import play.api.libs.json.{Json, OFormat}

final case class JourneyStatusAndLink(name: JourneyName, status: JourneyStatus, href: String)

object JourneyStatusAndLink {
  implicit val format: OFormat[JourneyStatusAndLink] = Json.format[JourneyStatusAndLink]

  def getStatusAndLinksFromJourneys(taxYear: TaxYear, businessId: BusinessId, savedJourneys: List[JourneyNameAndStatus]): List[JourneyStatusAndLink] =
    JourneyName.values.map { journey => // TODO should it be getting all journeys, or just the ones that are selected in tailoring?
      val maybeJourney: Option[JourneyNameAndStatus] = savedJourneys.find(_.name == journey)
      maybeJourney match {
        case Some(nameAndStatus) => // TODO do we need logic to determine 'InProgress' like we do in Pensions?
          JourneyStatusAndLink(nameAndStatus.name, nameAndStatus.journeyStatus, nameAndStatus.name.getHref(taxYear, businessId, toCYA = true))
        case None =>
          JourneyStatusAndLink(journey, NotStarted, journey.getHref(taxYear, businessId)) // TODO what if status should be cannot start yet?
      }
    }.toList
}
