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

package helpers

import com.codahale.metrics.SharedMetricRegistries
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import config.AppConfig
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HttpClient

trait WiremockSpec
    extends PlaySpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with GuiceOneServerPerSuite
    with FutureAwaits
    with DefaultAwaitTimeout
    with WiremockStubHelpers
    with AuthStub {
  self: PlaySpec =>

  val wireMockPort = 11111

  val wireMockServer: WireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  lazy val connectedServices: Seq[String] =
    Seq("auth", "integration-framework", "hip-integration-framework", "integration-framework-api1171", "citizen-details")

  protected lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  protected val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  def servicesToUrlConfig: Seq[(String, String)] = connectedServices
    .flatMap(service => Seq(s"microservice.services.$service.host" -> s"localhost", s"microservice.services.$service.port" -> wireMockPort.toString))

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      ("auditing.consumer.baseUri.port" -> wireMockPort) +:
        servicesToUrlConfig: _*
    )
    .build()

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    SharedMetricRegistries.clear()
    WireMock.configureFor("localhost", wireMockPort)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset()
  }

}
