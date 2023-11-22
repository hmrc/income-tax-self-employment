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

package utils

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends PlaySpec with GuiceOneServerPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(extraConfig)
    .build()

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  lazy val baseUrl = s"http://localhost:$port"

  val extraConfig: Map[String, Any] =
    Map[String, Any](
      "metrics.enabled" -> false
    )

  def buildClient(path: String)(implicit app: Application): WSRequest =
    app.injector
      .instanceOf[WSClient]
      .url(s"http://localhost:$port/$path")
      .withHttpHeaders(
        "Content-Type" -> "application/json",
        "Authorization" -> "Bearer BXQ3/Treo4kQCZvVcCqKPhA7wE/2hNqCz4BjnFzEN5m6lmzrFrQI96Au2BZrW0e9WLpDsptzxUoUEaw0V1MQH6EXGq/8151X26j/qnvuZUXEsWcJ6ru7Fr+/ci2kcBf4NHKTPCIju1pIGJG5Oqihp7aDpRrleO+Ik/A5cDedlvf9KwIkeIPK/mMlBESjue4V"
      )

}
