
package connectors

import base.IntegrationBaseSpec
import helpers.WiremockSpec
import models.connector.api_1867.{CarryForward, ReliefClaim, UkProperty}
import models.error.DownstreamError.GenericDownstreamError
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsString, Json}

import java.time.LocalDate

class ReliefClaimsConnectorISpec extends WiremockSpec with IntegrationBaseSpec {

  val connector: ReliefClaimsConnector = app.injector.instanceOf[ReliefClaimsConnector]

  val testTaxYear: String = "2024"
  val testMtditid: String = "mtditid"

  val url: String = s"/income-tax/$taxYear/claims-for-relief/$testMtditid"

  val selfEmploymentClaim: JsObject = Json.obj(
    "incomeSourceId" -> "XAIS12345678901",
    "reliefClaimed" -> "02",
    "reliefClaimed" -> "CF",
    "taxYearClaimedFor" -> "2024",
    "claimId" -> "1234567890",
    "submissionDate" -> "2024-01-01"
  )

  val propertyClaim: JsObject = selfEmploymentClaim + ("incomeSourceType" -> JsString("02"))

  val testBaseReliefClaim: ReliefClaim = ReliefClaim(
    incomeSourceId = "XAIS12345678901",
    incomeSourceType = None,
    reliefClaimed = CarryForward,
    taxYearClaimedFor = "2024",
    claimId = "1234567890",
    submissionDate = LocalDate.of(2024, 1, 1)
  )

  "getReliefClaims" when {
    "the API returns 200 OK" should {
      "successfully parse a Self Employment claim" in {
        val response = Json.arr(selfEmploymentClaim)

        stubGetWithResponseBody(
          url = url,
          expectedStatus = OK,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getReliefClaims(testTaxYear, testMtditid).futureValue

        result mustBe Right(List(testBaseReliefClaim))
        result.map(_.head.isSelfEmploymentClaim) mustBe Right(true)
      }

      "successfully parse a Property claim" in {
        val response = Json.arr(propertyClaim)

        stubGetWithResponseBody(
          url = url,
          expectedStatus = OK,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getReliefClaims(testTaxYear, testMtditid).futureValue

        result mustBe Right(List(testBaseReliefClaim.copy(incomeSourceType = Some(UkProperty))))
        result.map(_.head.isPropertyClaim) mustBe Right(true)
      }

      "successfully parse both a Self Employment and Property claim" in {
        val response = Json.arr(selfEmploymentClaim, propertyClaim)

        stubGetWithResponseBody(
          url = url,
          expectedStatus = OK,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getReliefClaims(testTaxYear, testMtditid).futureValue

        result mustBe Right(List(
          testBaseReliefClaim,
          testBaseReliefClaim.copy(incomeSourceType = Some(UkProperty))
        ))
        result.map(_.head.isSelfEmploymentClaim) mustBe Right(true)
        result.map(_.last.isPropertyClaim) mustBe Right(true)
      }
    }

    "the API returns 404 NOT_FOUND" should {
      "return a Left with an error message" in {
        val response: JsObject = Json.obj("failures" -> Json.arr(Json.obj("code" -> "NOT_FOUND", "reason" -> "Resource not found")))

        stubGetWithResponseBody(
          url = url,
          expectedStatus = NOT_FOUND,
          expectedResponse = Json.stringify(response)
        )

        val result = connector.getReliefClaims(testTaxYear, testMtditid).futureValue

        result mustBe Right(Nil)
      }
    }

    "the API returns 400 BAD_REQUEST, 422 UNPROCESSABLE_ENTITY or 5xx response" should {
      "return a service error" in {
        Seq(
          (BAD_REQUEST, "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear."),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", "The remote endpoint has indicated that this tax year is not supported."),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", "IF is currently experiencing problems that require live service intervention."),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
        ) foreach { case (status, code, reason) =>
          val response: JsObject = Json.obj(
            "failures" -> Json.arr(
              Json.obj(
                "code" -> code,
                "reason" -> reason
              )
            )
          )

          stubGetWithResponseBody(
            url = url,
            expectedStatus = status,
            expectedResponse = Json.stringify(response)
          )

          val result = connector.getReliefClaims(testTaxYear, testMtditid).futureValue

          result.isLeft mustBe true
          result.merge mustBe a[GenericDownstreamError]
        }
      }
    }
  }

}
