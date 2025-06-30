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

package mocks.connectors

import cats.data.EitherT
import connectors.HIP.BroughtForwardLossConnector
import models.common.{Nino, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockBroughtForwardLossConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockBroughtForwardLossConnector: BroughtForwardLossConnector = mock[BroughtForwardLossConnector]

  private type MockType = CallHandler5[Nino, TaxYear, String, HeaderCarrier, ExecutionContext, ApiResultT[Unit]]

  object BroughtForwardLossConnectorMock {

    def deleteBroughtForwardLosses(nino: Nino, taxYear: TaxYear , lossId: String)
                                  (returnValue: Either[ServiceError, Unit]): MockType =
      (mockBroughtForwardLossConnector.deleteBroughtForwardLoss(_: Nino, _: TaxYear, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, taxYear, lossId, *, *)
        .returning(EitherT(Future.successful(returnValue)))

  }

}
