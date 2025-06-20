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

package models.frontend.adjustments

import models.frontend.adjustments.WhichYearIsLossReported.{Year2018to2019, Year2019to2020, Year2020to2021, Year2021to2022, Year2022to2023}
import org.scalatest.freespec.AnyFreeSpec
import utils.TestUtils.convertToAnyMustWrapper

class WhichYearIsLossReportedSpec extends AnyFreeSpec {

  "WhichYearIsLossReported" - {
    "convert to WhichYearIsLossReported for the valid input" in {
      WhichYearIsLossReported.convertToWhichYearIsLossReported("2018-19") mustBe Year2018to2019
      WhichYearIsLossReported.convertToWhichYearIsLossReported("2019-20") mustBe Year2019to2020
      WhichYearIsLossReported.convertToWhichYearIsLossReported("2020-21") mustBe Year2020to2021
      WhichYearIsLossReported.convertToWhichYearIsLossReported("2021-22") mustBe Year2021to2022
      WhichYearIsLossReported.convertToWhichYearIsLossReported("2022-23") mustBe Year2022to2023
    }

    "throw an exception for the invalid input" in {
      val exep: Exception = intercept[Exception](WhichYearIsLossReported.convertToWhichYearIsLossReported("invalid"))
      exep.getMessage mustBe "Unsupported year"
    }
  }
}
