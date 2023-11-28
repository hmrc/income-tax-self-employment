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

package models.frontend.journeys.expenses.goodsToSellOrUse

import models.frontend.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers.{reads, writes}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class GoodsToSellOrUseJourneyAnswersSpec extends AnyWordSpec {

  private val incomingJson = Json.obj("goodsToSellOrUseAmount" -> 100.00, "disallowableGoodsToSellOrUseAmount" -> 100.00)
  private val answers      = GoodsToSellOrUseJourneyAnswers(100.00, Some(100.00))

  "given valid json" must {
    "deserialize it" in {
      Json.fromJson(incomingJson) shouldBe JsSuccess(answers)
    }
  }
  "serialize json" in {
    val expectedJson = Json.parse(s"""
         |{
         |    "deductions": {
         |      "costOfGoods": {
         |        "amount": 100.00,
         |        "disallowableAmount": 100.00
         |      }
         |    }
         |}
         |""".stripMargin)

    Json.toJson(answers) shouldBe expectedJson
  }

}
