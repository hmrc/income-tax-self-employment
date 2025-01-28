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

import models.common.{BusinessId, Mtditid, Nino, TaxYear}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.{WSClient, WSRequest}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import scala.concurrent.ExecutionContext

trait IntegrationBaseSpec extends PlaySpec with GuiceOneServerPerSuite with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val hc: HeaderCarrier    = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  protected lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  val taxYear: TaxYear       = TaxYear(2024)
  val businessId: BusinessId = BusinessId("SJPR05893938418")
  val nino: Nino             = Nino("nino")
  val mtditid: Mtditid       = Mtditid("1234567890")

  protected def buildClient(urlandUri: String): WSRequest =
    ws
      .url(s"http://localhost:$port$urlandUri")
      .withFollowRedirects(false)
}
