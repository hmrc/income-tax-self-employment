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

package models.connector

import enumeratum._

sealed trait ApiName {
  def entryName: String
}

sealed abstract class IFSApiName(override val entryName: String) extends EnumEntry with ApiName

/** When you add a new API here, don't forget to add it in the application.conf and config for QA, Staging and Prod
  */
object IFSApiName extends Enum[IFSApiName] {
  val values: IndexedSeq[IFSApiName] = IndexedSeq[IFSApiName]()

  final case object Api1171 extends IFSApiName("1171")
  final case object Api1500 extends IFSApiName("1500")
  final case object Api1501 extends IFSApiName("1501")
  final case object Api1502 extends IFSApiName("1502")
  final case object Api1504 extends IFSApiName("1504")
  final case object Api1505 extends IFSApiName("1505")
  final case object Api1507 extends IFSApiName("1507")
  final case object Api1509 extends IFSApiName("1509")
  final case object Api1870 extends IFSApiName("1870")
  final case object Api1638 extends IFSApiName("1638")
  final case object Api1639 extends IFSApiName("1639")
  final case object Api1640 extends IFSApiName("1640")
  final case object Api1786 extends IFSApiName("1786")
  final case object Api1787 extends IFSApiName("1787")
  final case object Api1802 extends IFSApiName("1802")
  final case object Api1803 extends IFSApiName("1803")
  final case object Api1867 extends IFSApiName("1867")
  final case object Api1871 extends IFSApiName("1871")
  final case object Api1894 extends IFSApiName("1894")
  final case object Api1895 extends IFSApiName("1895")
  final case object Api1965 extends IFSApiName("1965")
  final case object Api2085 extends IFSApiName("2085")
}

sealed abstract class HipApiName(override val entryName: String) extends EnumEntry with ApiName

/** When you add a new API here, don't forget to add it in the application.conf and config for QA, Staging and Prod
  */
object HipApiName extends Enum[HipApiName] {
  val values: IndexedSeq[HipApiName] = IndexedSeq[HipApiName]()

  final case object Api1504 extends HipApiName("1504")
  final case object Api1505 extends HipApiName("1505")
  final case object Api1171 extends HipApiName("1171")
  final case object Api5190 extends HipApiName("5190")
}

sealed abstract class MDTPApiName(override val entryName: String) extends EnumEntry with ApiName

object MDTPApiName extends Enum[MDTPApiName] {
  val values: IndexedSeq[MDTPApiName] = IndexedSeq[MDTPApiName]()

  case object CitizenDetails extends MDTPApiName("citizen-details")
}
