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

package services.answers

import data.CommonTestData
import models.common.JourneyName.{NationalInsuranceContributions, TravelExpenses}
import models.database.expenses.travel.{OwnVehicles, TravelExpensesDb}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await

import uk.gov.hmrc.http.InternalServerException

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyAnswerValidationServiceSpec extends AnyWordSpec with Matchers with CommonTestData with DefaultAwaitTimeout {

  val service = new JourneyAnswerValidationService()

  "validate" when {
    "the journey name is valid" when {
      "the data is valid for the journey" should {
        "return ValidSection" in {
          val validJson = Json.toJson(
            TravelExpensesDb(
              expensesToClaim = Some(Seq(OwnVehicles)),
              allowablePublicTransportExpenses = Some(BigDecimal("100.00")),
              disallowablePublicTransportExpenses = Some(BigDecimal("50.00"))
            ))

          val result = await(service.validate(TravelExpenses, validJson))

          result mustBe Right(ValidSection(validJson))
        }
      }

      "the data is invalid for the journey" should {
        "return InvalidSection with details of the erroneous fields" in {
          val validJson = Json.obj(
            "expensesToClaim"                     -> "invalid",
            "allowablePublicTransportExpenses"    -> "invalid",
            "disallowablePublicTransportExpenses" -> "50.00"
          )

          val result = await(service.validate(TravelExpenses, validJson))

          result mustBe Left(InvalidSection(Seq("/allowablePublicTransportExpenses", "/expensesToClaim")))
        }
      }

      "the json contains fields that aren't expected by the journey" should {
        "return a sanitized json with the unexpected fields removed" in {
          val invalidJson = Json.obj(
            "expensesToClaim"                     -> Json.arr("OwnVehicles"),
            "allowablePublicTransportExpenses"    -> 100,
            "disallowablePublicTransportExpenses" -> 50,
            "unexpectedField"                     -> "unexpected"
          )

          val validJson = invalidJson.as[JsObject] - "unexpectedField"

          val result = await(service.validate(TravelExpenses, invalidJson))

          result mustBe Right(ValidSection(validJson))
        }
      }
    }

    "the journey name isn't supported by the Answers API" should {
      "throw an InternalServerException" in {
        val json = Json.obj()

        assertThrows[InternalServerException] {
          await(service.validate(NationalInsuranceContributions, json))
        }
      }
    }
  }

}
