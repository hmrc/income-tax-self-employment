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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, RequestId}
import uk.gov.hmrc.http.HeaderCarrier.Config
import cats.implicits._

class ApiConnectorSpec extends AnyWordSpecLike with Matchers {

  "apiHeaderCarrier" should {
    val config = Config(
      List("localhost".r),
      Nil,
      None
    )
    val originalHc = HeaderCarrier()
      .copy(requestId = RequestId("originalRequestId").some)
      .withExtraHeaders(HeaderNames.xRequestId -> "extraRequestId")

    "add extra headers for internal host" in {
      val result = ApiConnector.apiHeaderCarrier(config, "http://localhost", originalHc, Nil, HeaderNames.xRequestId -> "updatedRequestId")
      result.extraHeaders.toList should contain(HeaderNames.xRequestId -> "updatedRequestId")
    }

    "add explicit headers for non-internal host" in {
      val result = ApiConnector.apiHeaderCarrier(config, "http://ifs", originalHc, Nil, HeaderNames.xRequestId -> "overridenRequestId")
      result.extraHeaders.toList should contain(HeaderNames.xRequestId -> "overridenRequestId")
    }
  }
}
