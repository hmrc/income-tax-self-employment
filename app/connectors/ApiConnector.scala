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

  def apiHeaderCarrier(headerCarrierConfig: Config,
                       url: String,
                       hc: HeaderCarrier,
                       testScenarios: List[String],
                       headers: (String, String)*): HeaderCarrier = {
    def maybeWithTestScenarios(headers: Seq[(String, String)]): Seq[(String, String)] =
      if (testScenarios.nonEmpty) {
        val updatedHeaders = headers ++ hc.otherHeaders.filter(_._1 == "ITSA_TEST_SCENARIO")
        updatedHeaders ++ testScenarios.map(value => "ITSA_TEST_SCENARIO" -> value)
      } else {
        headers
      }

    val isInternalHost = headerCarrierConfig.internalHostPatterns.exists(_.pattern.matcher(new URL(url).getHost).matches())
    val updatedHeaders = if (isInternalHost) headers else headers ++ hc.toExplicitHeaders

    hc.withExtraHeaders(maybeWithTestScenarios(updatedHeaders): _*)
  }
}
