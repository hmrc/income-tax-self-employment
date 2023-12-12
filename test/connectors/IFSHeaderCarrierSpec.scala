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

import models.connector.IFSApiName
import models.connector.IntegrationContext.IFSHeaderCarrier
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.http.HeaderCarrier.Config
import utils.TestUtils

class IFSHeaderCarrierSpec extends AnyWordSpecLike with Matchers {
  private val ec = scala.concurrent.ExecutionContext.Implicits.global

  "IFSHeaderCarrier" should {
    val internalHost        = "http://localhost"
    val hc                  = HeaderCarrier()
    val mockApiConfig       = TestUtils.mockAppConfig
    val headerCarrierConfig = Config()

    "enrich header carrier with bearer and environment" in {
      val context = IFSHeaderCarrier(headerCarrierConfig, mockApiConfig, IFSApiName.Api1965, internalHost)(hc, ec)
      val result  = context.hc

      result.authorization shouldBe Some(Authorization(s"Bearer secret"))
      result.extraHeaders should contain("Environment" -> "test")
    }
  }
}
