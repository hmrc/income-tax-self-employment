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

package models.connector.api_1894.request

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpecLike

class FinancialsTypeSpec extends AnyWordSpecLike with TypeCheckedTripleEquals {

  "toApi1895" should {
    "convert the model to API 1895" in {
      val data          = FinancialsTypeTestData.sample
      val expectedModel = models.connector.api_1895.request.AmendSEPeriodSummaryRequestBodyTestData.sample
      assert(
        data.toApi1895(Some(123.45)) === expectedModel
          .copy(incomes = expectedModel.incomes.map(_.copy(taxTakenOffTradingIncome = Some(123.45)))))
    }
  }

  "updateDeductions" should {
    "update the deductions" in {
      val data = FinancialsTypeTestData.sample
      assert(data.updateDeductions(DeductionsTestData.sample) === data.copy(deductions = Some(DeductionsTestData.sample)))
    }

    "return None for deductions if updating with an empty deductions" in {
      assert(FinancialsType.empty.updateDeductions(Deductions.empty).deductions === None)
    }
  }
}
