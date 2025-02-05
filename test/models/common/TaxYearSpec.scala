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

import org.scalatest.wordspec.AnyWordSpecLike

class TaxYearSpec extends AnyWordSpecLike {
  private val year = TaxYear(2024)

  "getting start and end dates" should {
    "get April 5th and 6th" in {
      assert(TaxYear.startDate(year) === "2023-04-06")
      assert(TaxYear.endDate(year) === "2024-04-05")
    }
  }

  "get a TYS (YY-YY) format" in {
    assert(TaxYear.asTys(year) === "23-24")
  }

  "get a TaxYear (YY-YY) format" in {
    assert(TaxYear.asTy("2023-24") === TaxYear(2024))
  }

  "toYYYY_YY" should {
    "get a YYYY-YY format" in {
      assert(TaxYear(2024).toYYYY_YY === "2023-24")
      assert(TaxYear(1999).toYYYY_YY === "1998-99")
    }
  }

}
