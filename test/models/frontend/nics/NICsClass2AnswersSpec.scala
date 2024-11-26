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

package models.frontend.nics

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class NICsClass2AnswersSpec extends AnyWordSpec with Matchers {

  "NICsClass2Answers" should {

    "write to JSON correctly" in {
      val answers = NICsClass2Answers(class2NICs = true)
      val json = Json.toJson(answers)
      json.toString() shouldBe """{"class2NICs":true}"""
    }

    "read from JSON correctly" in {
      val json = Json.parse("""{"class2NICs":true}""")
      json.validate[NICsClass2Answers] shouldBe JsSuccess(NICsClass2Answers(class2NICs = true))
    }

    "handle false value correctly" in {
      val answers = NICsClass2Answers(class2NICs = false)
      val json = Json.toJson(answers)
      json.toString() shouldBe """{"class2NICs":false}"""

      val parsedJson = Json.parse("""{"class2NICs":false}""")
      parsedJson.validate[NICsClass2Answers] shouldBe JsSuccess(NICsClass2Answers(class2NICs = false))
    }
  }
}