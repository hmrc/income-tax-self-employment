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
import models.common.JourneyName.VehicleDetails
import models.database.expenses.travel._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.await
import services.answers.CollectionSection

class CollectionAnswerControllerISpec extends IntegrationBaseSpec with AuthStub {

  def url(journey: String, idx: Int): String =
    s"/answers/users/$testNino/businesses/$testBusinessId/years/${testTaxYear.endYear}/journeys/$journey/${idx.toString}"

  val testSection: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("test"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(FlatRate),
    costsOutsideFlatRate = Some(BigDecimal("100.00"))
  )

  val testSection2: VehicleDetailsDb = testSection.copy(description = Some("test2"))
  val testSection3: VehicleDetailsDb = testSection.copy(description = Some("test3"))

  "GET /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section/:idx" when {
    "data exists" should {
      "Return OK with the data for the correct index" in {
        stubAuthorisedIndividual()
        stubAudits()

        DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection)))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).get())

        response.status mustBe OK
        response.json mustBe Json.toJson(testSection)
        DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe Some(CollectionSection(Seq(testSection)))
      }

      "Return OK with the data for the correct index when there are multiple indices" in {
        stubAuthorisedIndividual()
        stubAudits()

        DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection, testSection2, testSection3)))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 2)).get())

        response.status mustBe OK
        response.json mustBe Json.toJson(testSection2)
        DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe Some(CollectionSection(Seq(testSection, testSection2, testSection3)))
      }

      "return BAD_REQUEST if the index is out of bounds" in {
        stubAuthorisedIndividual()
        stubAudits()

        DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection)))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 0)).get())

        response.status mustBe BAD_REQUEST
      }
    }

    "no data exists" should {
      "Return NOT_FOUND" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).get())

        response.status mustBe NOT_FOUND
        DbHelper.getJson(VehicleDetails) mustBe None
      }
    }

    "data exists, but it's corrupted" should {
      "Return INTERNAL_SERVER_ERROR" in {
        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(VehicleDetails, Json.obj("values" -> Json.arr(Json.obj("usedSimplifiedExpenses" -> "true"))))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).get())

        response.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "an invalid section has been requested" should {
      "return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url("invalid-section", idx = 1)).get())

        response.status mustBe BAD_REQUEST
        DbHelper.getJson(VehicleDetails) mustBe None
      }
    }
  }

  "PUT /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section/:idx" when {
    "the JSON content is valid" should {
      val original = testSection.copy(description = Some("original"))

      Seq((1, testSection), (2, testSection2), (3, testSection3)).foreach { case (idx, update) =>
        s"Replace index $idx of 3" in {
          stubAuthorisedIndividual()
          stubAudits()
          DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(original, original, original)))

          val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx)).put(Json.toJson(update)))

          response.status mustBe OK
          response.json mustBe Json.toJson(update)

          val idxFromZero = idx - 1
          DbHelper.getJson(VehicleDetails).flatMap(json => (json \ "values" \ idxFromZero).validate[VehicleDetailsDb].asOpt) mustBe Some(update)
        }
      }
    }

    "the JSON content is invalid" should {
      "not create the index and return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).put(Json.obj("usedSimplifiedExpenses" -> "invalid")))

        response.status mustBe BAD_REQUEST
        DbHelper.getJson(VehicleDetails) mustBe None
      }
    }

    "not modify an existing index and return BAD_REQUEST" in {
      stubAuthorisedIndividual()
      stubAudits()

      DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection)))

      val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).put(Json.obj("usedSimplifiedExpenses" -> "invalid")))

      response.status mustBe BAD_REQUEST
      DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe Some(CollectionSection(Seq(testSection)))
    }

    "the body isn't valid JSON" should {
      "return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).put("Not JSON"))

        response.status mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST if the index is out of bounds" in {
      stubAuthorisedIndividual()
      stubAudits()

      val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, 0)).put(Json.toJson(testSection)))

      response.status mustBe BAD_REQUEST
    }
  }

  "DELETE /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section/:idx" when {
    "data exists for the given index" should {
      "delete the entire section if the last index is deleted and return NO_CONTENT" in {
        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection)))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).delete())

        response.status mustBe NO_CONTENT
        DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe None
      }

      "delete the correct index when there are multiple indices and return NO_CONTENT" in {
        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(VehicleDetails, CollectionSection(Seq(testSection, testSection2)))

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).delete())

        response.status mustBe NO_CONTENT
        DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe Some(CollectionSection(Seq(testSection2)))
      }
    }

    "data doesn't exist for the given section" should {
      "just return NO_CONTENT" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, idx = 1)).delete())

        response.status mustBe NO_CONTENT
        DbHelper.get[CollectionSection[VehicleDetailsDb]](VehicleDetails) mustBe None
      }
    }

    "return BAD_REQUEST if the index is out of bounds" in {
      stubAuthorisedIndividual()
      stubAudits()

      val response: WSResponse = await(buildClient(url(VehicleDetails.entryName, 0)).delete())

      response.status mustBe BAD_REQUEST
    }
  }

}
