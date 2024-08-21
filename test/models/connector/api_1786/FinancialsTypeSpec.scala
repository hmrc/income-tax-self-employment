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

package models.connector.api_1786

import models.connector.api_1894.request.{Deductions, DeductionsTestData, FinancialsTypeTestData}
import org.scalatest.wordspec.AnyWordSpecLike
import models.connector.api_1895
import models.connector.api_1894

class FinancialsTypeSpec extends AnyWordSpecLike {
  private val data = FinancialsTypeTestData.sample

  "updateDeductions" should {
    "return no deductions if empty" in {
      assert(data.updateDeductions(Deductions.empty) === data.copy(deductions = None))
    }

    "return updated deductions" in {
      assert(api_1894.request.FinancialsType.empty.updateDeductions(DeductionsTestData.sample) === data.copy(incomes = None))
    }
  }

  "toApi1895" should {
    "convert the model to API 1895" in {
      assert(
        data.toApi1895(Some(100.12)) === api_1895.request.AmendSEPeriodSummaryRequestBody(
          Some(api_1895.request.Incomes(Some(1.0), Some(2.0), Some(100.12))),
          Some(api_1895.request.DeductionsTestData.sample)
        ))
    }
  }
}
