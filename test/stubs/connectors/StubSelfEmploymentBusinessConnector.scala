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

package stubs.connectors

import cats.implicits.catsSyntaxEitherId
import connectors.SelfEmploymentBusinessConnector
import connectors.httpParsers.CreateSEPeriodSummaryHttpParser.Api1894Response
import models.connector.api_1894.request.CreateSEPeriodSummaryRequestData
import models.connector.api_1894.response.CreateSEPeriodSummaryResponse
import stubs.connectors.StubSelfEmploymentBusinessConnector.emptyApi1894Response
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubSelfEmploymentBusinessConnector(
    createSEPeriodSummaryResult: Future[Api1894Response] = Future.successful(emptyApi1894Response.asRight)
) extends SelfEmploymentBusinessConnector {

  def createSEPeriodSummary(
      requestData: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] =
    createSEPeriodSummaryResult
}

object StubSelfEmploymentBusinessConnector {
  val emptyApi1894Response: CreateSEPeriodSummaryResponse = CreateSEPeriodSummaryResponse("id")
}
