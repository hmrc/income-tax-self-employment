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

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.AppConfig
import connectors.BusinessConnector.{IdType, MtdId, Nino, businessUriPath}
import connectors.GetBusinessConnectorISpec.expectedResponseBody
import connectors.httpParsers.GetBusinessesHttpParser.GetBusinessesResponse
import helpers.WiremockSpec
import models.api.BusinessData.GetBusinessDataRequest
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
    override val desBaseUrl: String = s"http://$businessApiHost:$wireMockPort"
  }

  val (nino, mtdId) = ("123456789", "1234567890123456")
  val (desNinoUrl, desMtdIdUrl) = (businessUriPath(Nino, nino), businessUriPath(MtdId, mtdId))

  val headersSentToDes = Seq(
    new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  ".getBusinesses" should {

    for ((idType, idNumber, desUrl) <- Seq((Nino, nino, desNinoUrl), (MtdId, mtdId, desMtdIdUrl))) {

      s"include internal headers - $desUrl" when {
        val internalHost = "localhost"
        val externalHost = "127.0.0.1"

        for ((intExtHost, intExt) <- Seq((internalHost, "Internal"), (externalHost, "External"))) {

          s"the host for DES is '$intExt'" in {
            val expectedResult = Json.parse(expectedResponseBody).as[GetBusinessDataRequest]
            val result = connect(idType, idNumber,
              HeaderCarrier(sessionId = Some(SessionId("sessionIdValue"))),
              new BusinessConnector(httpClient, appConfig(intExtHost)))(desUrl, OK, expectedResponseBody, headersSentToDes
            )
            result mustBe Right(Some(expectedResult))
          }
        }
      }

      s"return a Right None when NOT_FOUND - $desUrl" in {
        val result = connect(idType, idNumber)(desUrl, NOT_FOUND, "")
        result mustBe Right(None)
      }

      Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { errorStatus =>
        val (invalidIdType, invalidReason) = {
          val invalidParam = "Submission has not passed validation. Invalid parameter"
          if (idType == Nino) ("INVALID_NINO", s"$invalidParam NINO") else ("INVALID_MTDID", s"$invalidParam MTDID")
        }
        val errorResponseBody = Json.obj("code" -> invalidIdType, "reason" -> invalidReason)
        
        s"return a $errorStatus  - $desUrl" in {
          val result = connect(idType, idNumber)(desUrl, errorStatus, errorResponseBody.toString())
          result mustBe Left(APIStatusError(errorStatus, APIError(invalidIdType, invalidReason)))
        }
      }
    }
    
    def connect(idType: IdType, idNumber: String, headC: HeaderCarrier = HeaderCarrier(), busConnector: BusinessConnector = connector)
                   (connectUrl: String, responseStatus: Int, responseBody: String, requestHeaders: Seq[HttpHeader] = Seq.empty): GetBusinessesResponse = {
      stubGetWithResponseBody(connectUrl, responseStatus, responseBody, requestHeaders)
      auditStubs()

      implicit val hc: HeaderCarrier = headC
      val result = await(busConnector.getBusinesses(idType, idNumber)(hc))
      result
    }
    
  }
}

object GetBusinessConnectorISpec {

  val expectedResponseBody: String =
    """
      |{
      |  "safeId": "XE00001234567890",
      |  "nino": "AA123456A",
      |  "mtdbsa": "123456789012345",
      |  "propertyIncome": true,
      |  "businessData": [
      |    {
      |      "incomeSourceId": "XAIS12345678910",
      |      "accountingPeriodStartDate": "2019-01-01",
      |      "accountingPeriodEndDate": "2019-12-31",
      |      "tradingName": "RCDTS",
      |      "businessAddressDetails": {
      |        "addressLine1": "100 SuttonStreet",
      |        "addressLine2": "Wokingham",
      |        "addressLine3": "Surrey",
      |        "addressLine4": "London",
      |        "postalCode": "DH14EJ",
      |        "countryCode": "GB"
      |      },
      |      "businessContactDetails": {
      |        "phoneNumber": "01332752856",
      |        "mobileNumber": "07782565326",
      |        "faxNumber": "01332754256",
      |        "emailAddress": "stephen@manncorpone.co.uk"
      |      },
      |      "tradingStartDate": "2001-01-01",
      |      "cashOrAccruals": "cash",
      |      "seasonal": true,
      |      "cessationDate": "2001-01-01",
      |      "cessationReason": "002",
      |      "paperLess": true
      |    }
      |  ]
      |}
      |""".stripMargin
  
}
