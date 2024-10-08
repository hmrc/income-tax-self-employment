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

package models.frontend.capitalAllowances.writingDownAllowance

import data.api1802.AnnualAllowancesData
import models.database.capitalAllowances.WritingDownAllowanceDb
import org.scalatest.OptionValues._
import org.scalatest.wordspec.AnyWordSpecLike

class WritingDownAllowanceAnswersSpec extends AnyWordSpecLike {
  val answers = new WritingDownAllowanceAnswers(
    Some(true),
    Some(1),
    Some(true),
    Some(2),
    Some(true),
    Some(3)
  )

  "toDbModel" should {
    "convert to WritingDownAllowanceDb" in {
      val result = answers.toDbModel.value
      assert(result === WritingDownAllowanceDb(Some(true), Some(true), Some(true)))
    }
  }

  "toWritingDownDownstream" should {
    "convert to AnnualAllowances without previous answers" in {
      val result = answers.toDownStreamAnnualAllowances(None)
      assert(
        result ===
          models.connector.api_1802.request.AnnualAllowances.empty.copy(
            capitalAllowanceSpecialRatePool = Some(1),
            capitalAllowanceMainPool = Some(2),
            capitalAllowanceSingleAssetPool = Some(3)
          )
      )
    }

    "convert to AnnualAllowances with previous answers" in {
      val previous = AnnualAllowancesData.example

      val result = answers.toDownStreamAnnualAllowances(Some(previous))
      assert(
        result ===
          previous.copy(
            capitalAllowanceSpecialRatePool = Some(1),
            capitalAllowanceMainPool = Some(2),
            capitalAllowanceSingleAssetPool = Some(3)
          )
      )
    }

  }
}
