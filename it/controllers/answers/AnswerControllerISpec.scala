package controllers.answers

import base.IntegrationBaseSpec
import helpers.AuthStub
import models.common.JourneyName.TravelExpenses
import models.database.expenses.travel.{OwnVehicles, TravelExpensesDb}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK}
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.await

class AnswerControllerISpec extends IntegrationBaseSpec with AuthStub {

  val url: String => String = journey => s"/answers/users/$testNino/businesses/$testBusinessId/years/${testTaxYear.endYear}/journeys/$journey"

  "GET /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section" when {
    "data exists" should {
      "Return OK with the named section" in {
        val testSection = TravelExpensesDb(
          expensesToClaim = Some(Seq(OwnVehicles)),
          allowablePublicTransportExpenses = Some(100),
          disallowablePublicTransportExpenses = Some(50))

        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(TravelExpenses, testSection)

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).get())

        response.status mustBe OK
        response.json mustBe Json.toJson(testSection)
        DbHelper.get[TravelExpensesDb](TravelExpenses) mustBe Some(testSection)
      }
    }

    "no data exists" should {
      "Return NOT_FOUND" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).get())

        response.status mustBe NOT_FOUND
        DbHelper.getJson(TravelExpenses) mustBe None
      }
    }

    "data exists, but it's corrupted" should {
      "Return NOT_FOUND" in {
        val testSection = TravelExpensesDb(
          expensesToClaim = Some(Seq(OwnVehicles)),
          allowablePublicTransportExpenses = Some(100),
          disallowablePublicTransportExpenses = Some(50))

        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(TravelExpenses, Json.obj("expensesToClaim" -> "invalid"))

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).get())

        response.status mustBe INTERNAL_SERVER_ERROR
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
      "Replace the named section with the given JSON" in {
        val testSection: JsValue = Json.toJson(
          TravelExpensesDb(
            expensesToClaim = Some(Seq()),
            allowablePublicTransportExpenses = Some(100),
            disallowablePublicTransportExpenses = Some(50)))

        val expectedUpdate: JsObject = testSection.as[JsObject] - "expensesToClaim" ++ Json.obj("expensesToClaim" -> Json.arr("OwnVehicles"))

        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(TravelExpenses, testSection)

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).put(expectedUpdate))

        response.status mustBe OK
        response.json mustBe Json.toJson(expectedUpdate)
        DbHelper.getJson(TravelExpenses) mustBe Some(expectedUpdate)
      }
    }

    "the JSON content is invalid" should {
      "return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).put(Json.obj("expensesToClaim" -> "invalid")))

        response.status mustBe BAD_REQUEST
      }
    }

    "the body isn't valid JSON" should {
      "return BAD_REQUEST" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).put("Not JSON"))

        response.status mustBe BAD_REQUEST
      }
    }
  }

  "DELETE /answers/users/:nino/businesses/:business/years/:taxYear/sections/:section" when {
    "data exists for the given section" should {
      "delete the data and return NO_CONTENT" in {
        val testSection = TravelExpensesDb(
          expensesToClaim = Some(Seq(OwnVehicles)),
          allowablePublicTransportExpenses = Some(100),
          disallowablePublicTransportExpenses = Some(50))

        stubAuthorisedIndividual()
        stubAudits()
        DbHelper.insertOne(TravelExpenses, testSection)

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).delete())

        response.status mustBe NO_CONTENT
        DbHelper.getJson(TravelExpenses) mustBe None
      }
    }

    "data doesn't exist for the given section" should {
      "just return NO_CONTENT" in {
        stubAuthorisedIndividual()
        stubAudits()

        val response: WSResponse = await(buildClient(url(TravelExpenses.entryName)).delete())

        response.status mustBe NO_CONTENT
        DbHelper.getJson(TravelExpenses) mustBe None
      }
    }
  }

}
