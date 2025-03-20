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

package models.database.expenses.travel

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class TravelExpensesDbSpec extends AnyWordSpec with Matchers {

  val base: TravelExpensesDb = TravelExpensesDb(
    expensesToClaim = Some(Seq(OwnVehicles, LeasedVehicles, PublicTransport)),
    allowablePublicTransportExpenses = Some(BigDecimal("100.00")),
    disallowablePublicTransportExpenses = Some(BigDecimal("50.00"))
  )

  "write" should {
    "create the correct JSON" in {
      Json.toJson(base) shouldBe Json.obj(
        "expensesToClaim"                     -> Json.arr("OwnVehicles", "LeasedVehicles", "PublicTransport"),
        "allowablePublicTransportExpenses"    -> 100,
        "disallowablePublicTransportExpenses" -> 50
      )
    }
  }

  "read" should {
    "parse the correct JSON" in {
      val json = Json.obj(
        "expensesToClaim"                     -> Json.arr("OwnVehicles", "LeasedVehicles", "PublicTransport"),
        "allowablePublicTransportExpenses"    -> 100,
        "disallowablePublicTransportExpenses" -> 50.00
      )

      json.as[TravelExpensesDb] shouldBe base
    }
  }

}
