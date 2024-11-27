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

package models.database.income

import models.database.JourneyState
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class JourneyStateSpec extends AnyWordSpec with Matchers {

  "JourneyState" should {

    "write to JSON correctly" in {
      val journeyStateData = JourneyState.JourneyStateData(
        businessId = "business123",
        journey = "journeyType",
        taxYear = 2024,
        completedState = true
      )
      val journeyState = JourneyState(
        id = "test-id",
        journeyStateData = journeyStateData,
        lastUpdated = LocalDate.of(2024, 11, 26)
      )
      val json = Json.toJson(journeyState)

      val expectedJson = Json.parse(
        """{
          |"_id": "test-id",
          |"journeyStateData": {
          |  "businessId": "business123",
          |  "journey": "journeyType",
          |  "taxYear": 2024,
          |  "completedState": true
          |},
          |"lastUpdated": {
          |  "$date": {
          |    "$numberLong": "1732579200000"
          |  }
          |}
          |}""".stripMargin
      )

      json shouldBe expectedJson
    }

    "read from JSON correctly" in {
      val json = Json.parse(
        """{
          |"_id": "test-id",
          |"journeyStateData": {
          |  "businessId": "business123",
          |  "journey": "journeyType",
          |  "taxYear": 2024,
          |  "completedState": true
          |},
          |"lastUpdated": {
          |  "$date": {
          |    "$numberLong": "1732579200000"
          |  }
          |}
          |}""".stripMargin
      )

      val expectedJourneyState = JourneyState(
        id = "test-id",
        journeyStateData = JourneyState.JourneyStateData(
          businessId = "business123",
          journey = "journeyType",
          taxYear = 2024,
          completedState = true
        ),
        lastUpdated = LocalDate.of(2024, 11, 26)
      )
      json.validate[JourneyState] shouldBe JsSuccess(expectedJourneyState)
    }

    "handle default values correctly" in {
      val journeyStateData = JourneyState.JourneyStateData(
        businessId = "business123",
        journey = "journeyType",
        taxYear = 2024,
        completedState = true
      )

      val journeyState = JourneyState(
        journeyStateData = journeyStateData
      )

      journeyState.id should not be empty
      journeyState.lastUpdated shouldBe LocalDate.now()
    }
  }

  "JourneyStateData" should {

    "write to JSON correctly" in {
      val journeyStateData = JourneyState.JourneyStateData(
        businessId = "business123",
        journey = "journeyType",
        taxYear = 2024,
        completedState = true
      )

      val json = Json.toJson(journeyStateData)

      json.toString() shouldBe """{"businessId":"business123","journey":"journeyType","taxYear":2024,"completedState":true}"""
    }

    "read from JSON correctly" in {
      val json = Json.parse("""{"businessId":"business123","journey":"journeyType","taxYear":2024,"completedState":true}""")
      val expectedJourneyStateData = JourneyState.JourneyStateData(
        businessId = "business123",
        journey = "journeyType",
        taxYear = 2024,
        completedState = true
      )

      json.validate[JourneyState.JourneyStateData] shouldBe JsSuccess(expectedJourneyStateData)
    }
  }
}
