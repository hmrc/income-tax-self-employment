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

import connectors.HeaderCarrierSyntax.HeaderCarrierOps
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HeaderCarrier.Config

import java.net.URL

object ApiConnector {

  // TODO I'm not sure what is happening here. Review the logic below: SASS-6247
  def apiHeaderCarrier(headerCarrierConfig: Config,
                       url: String,
                       hc: HeaderCarrier,
                       isTestMode: Boolean,
                       headers: (String, String)*): HeaderCarrier = {
    val isInternalHost = headerCarrierConfig.internalHostPatterns.exists(_.pattern.matcher(new URL(url).getHost).matches())

    if (isInternalHost) {
      val updatedHeaders = if (isTestMode) {
        headers ++ hc.otherHeaders.filter(_._1 == "ITSA_TEST_SCENARIO")
      } else {
        headers
      }
      hc.withExtraHeaders(updatedHeaders: _*)
    } else {
      val updatedHeaders = headers ++ hc.toExplicitHeaders
      hc.withExtraHeaders(updatedHeaders: _*)
    }
  }
}
