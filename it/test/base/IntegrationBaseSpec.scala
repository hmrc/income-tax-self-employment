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

package base

import com.github.tomakehurst.wiremock.client.WireMock
import config.AppConfig
import helpers.{JourneyAnswersHelper, WiremockHelper}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.DefaultAwaitTimeout
import repositories.MongoJourneyAnswersRepository
import testdata.CommonTestData
import uk.gov.hmrc.http.HeaderCarrier.Config
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, SessionId}

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec
    extends PlaySpec
    with GuiceOneServerPerSuite
    with ScalaFutures
    with DefaultAwaitTimeout
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with CommonTestData
    with WiremockHelper
    with JourneyAnswersHelper {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  protected lazy val ws: WSClient          = app.injector.instanceOf[WSClient]
  val mongo: MongoJourneyAnswersRepository = app.injector.instanceOf[MongoJourneyAnswersRepository]
  lazy val appConfig: AppConfig            = app.injector.instanceOf[AppConfig]
  lazy val httpClientV2: HttpClientV2      = app.injector.instanceOf[HttpClientV2]

  val headerCarrierConfig: Config = Config()

  lazy val connectedServices: Seq[String] =
    Seq(
      "auth",
      "integration-framework",
      "hip-integration-framework",
      "integration-framework-api1171",
      "citizen-details"
    )

  def servicesToUrlConfig: Seq[(String, String)] = connectedServices
    .flatMap(service => Seq(s"microservice.services.$service.host" -> s"localhost", s"microservice.services.$service.port" -> wireMockPort.toString))

  def apiTokens: Seq[(String, String)] = Seq("1867")
    .map(api => s"microservice.services.integration-framework.authorisation-token.$api" -> testApiToken)

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      ("auditing.consumer.baseUri.port" -> wireMockPort) +:
        servicesToUrlConfig ++:
        apiTokens: _*
    )
    .build()

  protected def buildClient(uri: String): WSRequest =
    ws.url(s"http://localhost:$port/income-tax-self-employment${uri.replace("income-tax-self-employment", "")}")
      .withHttpHeaders(
        HeaderNames.COOKIE        -> "test",
        HeaderNames.AUTHORIZATION -> testAuthToken,
        "mtditid"                 -> testMtdItId.toString
      )
      .withFollowRedirects(false)

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    WireMock.reset()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    DbHelper.teardown
  }

}
