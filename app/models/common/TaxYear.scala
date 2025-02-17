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

import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

import java.time.LocalDate

final case class TaxYear(endYear: Int) extends AnyVal {
  override def toString: String = endYear.toString

  def toYYYY_YY: String = s"${endYear - 1}-${endYear.toString.takeRight(2)}"

  def fromAnnualPeriod: String = s"${endYear - 1}-04-06"

  def toAnnualPeriod: String = s"$endYear-04-05"
}

object TaxYear {

  private val taxYearStartDay = 6
  private val taxYearEndDay   = 5
  private val taxYearMonth    = 4

  def startDate(taxYear: TaxYear): String = LocalDate.of(taxYear.endYear - 1, taxYearMonth, taxYearStartDay).toString

  def endDate(taxYear: TaxYear): String = LocalDate.of(taxYear.endYear, taxYearMonth, taxYearEndDay).toString

  /* Gets a representation of a taxYear in a YY-YY format (from a YYYY format).
   */
  def asTys(taxYear: TaxYear): String = {
    val end   = taxYear.endYear - 2000
    val start = end - 1
    s"$start-$end"
  }

  def asTy(taxYearYYYY_YY: String): TaxYear = {
    val taxYear = taxYearYYYY_YY.take(4).toInt + 1
    TaxYear(taxYear)
  }

  implicit def pathBindable(implicit intBinder: PathBindable[Int]): PathBindable[TaxYear] = new PathBindable[TaxYear] {

    override def bind(key: String, value: String): Either[String, TaxYear] =
      intBinder.bind(key, value).map(TaxYear.apply)

    override def unbind(key: String, taxYear: TaxYear): String =
      intBinder.unbind(key, taxYear.endYear)

  }

  implicit val format: Format[TaxYear] = Json.valueFormat[TaxYear]

}
