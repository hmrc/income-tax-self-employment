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

sealed trait JourneyAnswersContext {
  val taxYear: TaxYear
  val businessId: BusinessId
  val mtditid: Mtditid
  val journey: JourneyName
}

object JourneyAnswersContext {
  // Don't actually use the JourneyName. Do we need it?
  case class JourneyContextWithNino(taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, nino: Nino, journey: JourneyName)
      extends JourneyAnswersContext

  case class JourneyContext(taxYear: TaxYear, businessId: BusinessId, mtditid: Mtditid, journey: JourneyName) extends JourneyAnswersContext
}
