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

package connectors

import bulders.BusinessDataBuilder.aGetBusinessDataRequestStr
import com.github.tomakehurst.wiremock.http.HttpHeader
import config.AppConfig
import connectors.BusinessConnector.IdType.{MtdId, Nino}
import connectors.BusinessConnector.businessUriPath
import helpers.WiremockSpec
import models.api.BusinessData.GetBusinessDataRequest
import models.error.APIErrorBody.APIError.{data404, ifsServer500, mtdId400, nino400, service503}
import models.error.APIErrorBody.{APIError, APIStatusError}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class GetBusinessConnectorISpec extends WiremockSpec {

  lazy val connector: BusinessConnector = app.injector.instanceOf[BusinessConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(businessApiHost: String): AppConfig = new AppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val ifsBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
  }

  val (nino, mtdId) = ("123456789", "1234567890123456")
  val (apiNinoUrl, apiMtdIdUrl) = (businessUriPath(Nino, nino), businessUriPath(MtdId, mtdId))

  val headersSentToIfs = Seq(
    new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  ".getBusinesses" should {

    for ((idType, idNumber, ifsUrl) <- Seq((Nino, nino, apiNinoUrl), (MtdId, mtdId, apiMtdIdUrl))) {

      s"include internal headers - $ifsUrl" when {
        val internalHost = "localhost"
        val externalHost = "127.0.0.1"
        val expectedResponseBody = aGetBusinessDataRequestStr

        for ((intExtHost, intExt) <- Seq((internalHost, "Internal"), (externalHost, "External"))) {

          s"the host for API is '$intExt'" in {
            val expectedResult = Json.parse(expectedResponseBody).as[GetBusinessDataRequest]

            stubGetWithResponseBody(ifsUrl, OK, expectedResponseBody, headersSentToIfs)
            auditStubs()

            implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
            val result = await((new BusinessConnector(httpClient, appConfig(intExtHost))).getBusinesses(idType, idNumber)(hc))
            result mustBe Right(expectedResult)
          }
        }
      }

      for ((errorStatus, apiError) <- Seq(
        (NOT_FOUND, data404), (BAD_REQUEST, if (idType == Nino) nino400 else mtdId400),
        (INTERNAL_SERVER_ERROR, ifsServer500), (SERVICE_UNAVAILABLE, service503))) {
        val errorResponseBody = Json.obj("code" -> apiError.code, "reason" -> apiError.reason, "errorType" -> "DOWNSTREAM_ERROR_CODE")

        s"return a $errorStatus  - $ifsUrl" in {
          stubGetWithResponseBody(ifsUrl, errorStatus, errorResponseBody.toString(), headersSentToIfs)
          auditStubs()

          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val result = await(connector.getBusinesses(idType, idNumber)(hc))
          result mustBe Left(APIStatusError(errorStatus, apiError))
        }
      }

      "return a parsing error" when {
        val (invalidIdType, invalidReason) = ("PARSING_ERROR", "Error parsing response from API")
        
        
        s"the HeaderCarrier is insufficient - $ifsUrl" in {
          val errorStatus = 404
          val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason, "errorType" -> "DOWNSTREAM_ERROR_CODE")
          stubGetWithResponseBody(ifsUrl, errorStatus, errorResponseBody.toString(), headersSentToIfs)
          auditStubs()
          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result = await(connector.getBusinesses(idType, idNumber)(hc))
          result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
        }
        
        s"the json fails to validate a non GetBusinessDataRequest json - $ifsUrl" in {
          val errorStatus = 500
          val nonGetBusinessDataRequestBody = Json.obj("field" -> "Non GetBusinessDataRequest json")
          stubGetWithResponseBody(ifsUrl, OK, nonGetBusinessDataRequestBody.toString(), headersSentToIfs)
          auditStubs()
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val result = await(connector.getBusinesses(idType, idNumber)(hc))
          result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
        }
      }
    }
  }
}
