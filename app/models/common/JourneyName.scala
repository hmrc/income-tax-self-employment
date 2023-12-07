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
import play.api.mvc.PathBindable

sealed abstract class JourneyName(override val entryName: String) extends EnumEntry

object JourneyName extends Enum[JourneyName] with utils.PlayJsonEnum[JourneyName] {
  val values: IndexedSeq[JourneyName] = findValues

  case object Income                     extends JourneyName("income")
  case object ExpensesTailoring          extends JourneyName("expenses-categories")
  case object GoodsToSellOrUse           extends JourneyName("expenses-goods-to-sell-or-use")
  case object OfficeSupplies             extends JourneyName("expenses-office-supplies")
  case object RepairsAndMaintenanceCosts extends JourneyName("expenses-repairs-and-maintenance")
  case object StaffCosts                 extends JourneyName("expenses-staff-costs")
  case object Entertainment              extends JourneyName("expenses-entertainment")

  // Are we using this code? Should we be if not?
  implicit def pathBindable(implicit strBinder: PathBindable[String]): PathBindable[JourneyName] = new PathBindable[JourneyName] {

    override def bind(key: String, value: String): Either[String, JourneyName] =
      strBinder.bind(key, value).flatMap { stringValue =>
        JourneyName.withNameOption(stringValue) match {
          case Some(journeyName) => Right(journeyName)
          case None              => Left(s"$stringValue Invalid journey name")
        }
      }

    override def unbind(key: String, journeyName: JourneyName): String =
      strBinder.unbind(key, journeyName.entryName)
  }

}
