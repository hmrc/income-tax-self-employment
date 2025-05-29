
package controllers

import base.IntegrationBaseSpec
import connectors.data.Api1171Test
import helpers.AuthStub
import models.common.{BusinessId, Nino}
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.await

class BusinessDetailsControllerISpec extends IntegrationBaseSpec with AuthStub with Api1171Test {

  private def getListUrl(nino: Nino) = s"/individuals/business/details/$nino/list"

  private def getUrl(nino: Nino, businessId: BusinessId) = s"/individuals/business/details/$nino/$businessId"

  "GET /individuals/business/details/:nino/list" should {
    "Return a list of businesses from the HIP Business Details API" in {
      stubAuthorisedIndividual()
      stubGetWithResponseBody(
        url = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?mtdReference=$testMtdItId&nino=$testNino",
        expectedStatus = OK,
        expectedResponse = api1171HipResponseJson
      )

      val res = await(buildClient(getListUrl(testNino)).get())

      res.status mustBe OK
      res.body mustBe Json.stringify(Json.toJson(api1171HipResponse.success.toBusinesses))
    }

    "Return NOT FOUND when no businesses are found" in {
      stubAuthorisedIndividual()
      stubGetWithoutResponseBody(
        url = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?mtdReference=$testMtdItId&nino=$testNino",
        expectedStatus = NOT_FOUND,
      )

      val res = await(buildClient(getListUrl(testNino)).get())

      res.status mustBe NOT_FOUND
    }
  }

  "GET /individuals/business/details/:nino/:businessId" should {
    "Return a business from the HIP Business Details API" in {
      stubAuthorisedIndividual()
      stubGetWithResponseBody(
        url = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?incomeSourceId=$testBusinessId&mtdReference=$testMtdItId&nino=$testNino",
        expectedStatus = OK,
        expectedResponse = api1171HipResponseJson
      )

      val res = await(buildClient(getUrl(testNino, testBusinessId)).get())

      res.status mustBe OK
      res.body mustBe Json.stringify(Json.toJson(api1171HipResponse.success.toBusinesses.head))
    }

    "Return NO CONTENT when no business was found" in {
      stubAuthorisedIndividual()
      stubGetWithoutResponseBody(
        url = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?incomeSourceId=$testBusinessId&mtdReference=$testMtdItId&nino=$testNino",
        expectedStatus = NOT_FOUND,
      )

      val res = await(buildClient(getUrl(testNino, testBusinessId)).get())

      res.status mustBe NOT_FOUND
    }
  }

}
