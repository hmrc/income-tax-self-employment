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

import play.api.mvc.PathBindable

final case class TaxYear(value: Int) extends AnyVal

object TaxYear {

  implicit def pathBindable(implicit intBinder: PathBindable[Int]): PathBindable[TaxYear] = new PathBindable[TaxYear] {

    override def bind(key: String, value: String): Either[String, TaxYear] =
      intBinder.bind(key, value).map(TaxYear.apply)

    override def unbind(key: String, taxYear: TaxYear): String =
      intBinder.unbind(key, taxYear.value)

  }
}
