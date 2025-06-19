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

package controllers.answers

import base.IntegrationBaseSpec
import helpers.AuthStub
import models.common.JourneyName.TravelExpenses
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.await
import testdata.AnswerApiTestData

class AnswerControllerISpec extends IntegrationBaseSpec with AuthStub with AnswerApiTestData {

  val url: String => String = journey => s"/answers/users/$testNino/businesses/$testBusinessId/years/${testTaxYear.endYear}/journeys/$journey"

  "GET /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section" when {
    "data exists" should {
      validScenarios.foreach { case (journey, journeyJson) =>
        s"Return OK with the named section for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()
          DbHelper.insertOne(journey, journeyJson)

          val response: WSResponse = await(buildClient(url(journey.entryName)).get())

          response.status mustBe OK
          response.json mustBe Json.toJson(journeyJson)
          DbHelper.getJson(journey) mustBe Some(journeyJson)
        }
      }
    }

    "no data exists" should {
      validScenarios.foreach { case (journey, _) =>
        s"Return NOT_FOUND for journey $journey" in {
          stubAuthorisedIndividual()
          stubAudits()

          val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).get())

          response.status mustBe NOT_FOUND
          DbHelper.getJson(TravelExpenses) mustBe None
        }
      }
    }

    "data exists, but it's corrupted" should {
      invalidScenarios.foreach { case (journey, journeyJson) =>
        s"Return INTERNAL_SERVER_ERROR for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()
          DbHelper.insertOne(journey, journeyJson)

          val response: WSResponse = await(buildClient(url(journey.entryName)).get())

          response.status mustBe INTERNAL_SERVER_ERROR
        }
      }
    }

    "an invalid section has been requested" should {
      "return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url("invalid-section")).get())

        response.status mustBe BAD_REQUEST
        DbHelper.getJson(TravelExpenses) mustBe None
      }
    }
  }

  "PUT /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section" when {
    "the JSON content is valid" should {
      replaceScenarios.foreach { case (journey, (testSection, expectedUpdate)) =>
        s"Replace the named section with the given JSON for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()
          DbHelper.insertOne(journey, testSection)

          val response: WSResponse = await(buildClient(url(journey.entryName)).put(expectedUpdate))

          response.status mustBe OK
          response.json mustBe Json.toJson(expectedUpdate)
          DbHelper.getJson(journey) mustBe Some(expectedUpdate)
        }
      }
    }

    "the JSON content is invalid" should {
      invalidScenarios.foreach { case (journey, journeyJson) =>
        s"return BAD_REQUEST for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()

          val response: WSResponse = await(buildClient(url(journey.entryName)).put(journeyJson))

          response.status mustBe BAD_REQUEST
        }
      }
    }

    "the body isn't valid JSON" should {
      validScenarios.foreach { case (journey, _) =>
        s"return BAD_REQUEST for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()

          val response: WSResponse = await(buildClient(url(journey.entryName)).put("Not JSON"))

          response.status mustBe BAD_REQUEST
        }
      }
    }
  }

  "DELETE /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section" when {
    "data exists for the given section" should {
      validScenarios.foreach { case (journey, journeyJson) =>
        s"delete the data and return NO_CONTENT for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()
          DbHelper.insertOne(journey, journeyJson)

          val response: WSResponse = await(buildClient(url(journey.entryName)).delete())

          response.status mustBe NO_CONTENT
          DbHelper.getJson(journey) mustBe None
        }
      }
    }

    "data doesn't exist for the given section" should {
      validScenarios.foreach { case (journey, _) =>
        s"return NO_CONTENT for $journey" in {
          stubAuthorisedIndividual()
          stubAudits()

          val response: WSResponse = await(buildClient(url(journey.entryName)).delete())

          response.status mustBe NO_CONTENT
          DbHelper.getJson(journey) mustBe None
        }
      }
    }
  }

}
