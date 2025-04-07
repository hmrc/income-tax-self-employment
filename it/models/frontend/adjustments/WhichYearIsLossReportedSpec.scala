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
