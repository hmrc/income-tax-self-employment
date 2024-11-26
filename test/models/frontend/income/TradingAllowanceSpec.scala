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

package models.frontend.income

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.JsString

class TradingAllowanceSpec extends AnyWordSpec with Matchers {

  "TradingAllowance" should {

    "contain the correct values" in {
      TradingAllowance.values should contain allOf (
        TradingAllowance.UseTradingAllowance,
        TradingAllowance.DeclareExpenses
      )
    }

    "read from JSON correctly" in {
      JsString("useTradingAllowance").as[TradingAllowance] shouldBe TradingAllowance.UseTradingAllowance
      JsString("declareExpenses").as[TradingAllowance] shouldBe TradingAllowance.DeclareExpenses
    }

    "provide the correct enumerable instance" in {
      TradingAllowance.enumerable.withName("useTradingAllowance") shouldBe Some(TradingAllowance.UseTradingAllowance)
      TradingAllowance.enumerable.withName("declareExpenses") shouldBe Some(TradingAllowance.DeclareExpenses)
      TradingAllowance.enumerable.withName("invalid") shouldBe None
    }
  }
}