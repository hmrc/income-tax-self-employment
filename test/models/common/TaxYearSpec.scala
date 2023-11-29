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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.BaseSpec

class TaxYearSpec extends BaseSpec {

  private val year = TaxYear(2024)

  "getting start and end dates" must {
    "get April 5th and 6th" in {
      TaxYear.startDate(year) shouldBe "2023-04-06"
      TaxYear.endDate(year) shouldBe "2024-04-05"
    }
  }
  "get a TYS (YY-YY) format" in {
    TaxYear.asTys(year) shouldBe "23-24"
  }

}
