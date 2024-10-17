/*
 * Copyright 2024 HM Revenue & Customs
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

import models.common.{Enumerable, WithName}
sealed trait WhichYearIsLossReported {
  val apiTaxYear: String
}

object WhichYearIsLossReported extends Enumerable.Implicits {

  case object Year2018to2019 extends WithName("2018to2019") with WhichYearIsLossReported {
    val apiTaxYear = "2018-19"
  }
  case object Year2019to2020 extends WithName("2019to2020") with WhichYearIsLossReported {
    val apiTaxYear = "2019-20"
  }
  case object Year2020to2021 extends WithName("2020to2021") with WhichYearIsLossReported {
    val apiTaxYear = "2020-21"
  }
  case object Year2021to2022 extends WithName("2021to2022") with WhichYearIsLossReported {
    val apiTaxYear = "2021-22"
  }
  case object Year2022to2023 extends WithName("2022to2023") with WhichYearIsLossReported {
    val apiTaxYear = "2022-23"
  }

  val values: Seq[WhichYearIsLossReported] = Seq(
    Year2018to2019,
    Year2019to2020,
    Year2020to2021,
    Year2021to2022,
    Year2022to2023
  )

  implicit val enumerable: Enumerable[WhichYearIsLossReported] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
