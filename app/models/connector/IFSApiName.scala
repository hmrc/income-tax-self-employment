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

package models.connector

import enumeratum._

sealed abstract class IFSApiName(override val entryName: String) extends EnumEntry

/** When you add a new API here, don't forget to add it in the application.conf and config for QA, Staging and Prod
  */
object IFSApiName extends Enum[IFSApiName] {
  val values = IndexedSeq[IFSApiName]()

  case object Api1171 extends IFSApiName("1171")
  case object Api1638 extends IFSApiName("1638")
  case object Api1786 extends IFSApiName("1786")
  case object Api1802 extends IFSApiName("1802")
  case object Api1803 extends IFSApiName("1803")
  case object Api1894 extends IFSApiName("1894")
  case object Api1895 extends IFSApiName("1895")
  case object Api1965 extends IFSApiName("1965")
}
