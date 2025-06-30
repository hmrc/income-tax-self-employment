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
import connectors.MDTP.MDTPConnector
import models.common.Nino
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import models.connector.citizen_details.SuccessResponseSchema
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler3
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockCitizenDetailsConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockCitizenDetailsConnector = mock[MDTPConnector]

  object CitizenDetailsConnectorMock {

    def getCitizenDetails(nino: Nino)
                         (returnValue: Either[ServiceError, SuccessResponseSchema]): CallHandler3[Nino, HeaderCarrier, ExecutionContext, ApiResultT[SuccessResponseSchema]] =
      (mockCitizenDetailsConnector.getCitizenDetails(_: Nino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, *, *)
        .returning(EitherT(Future.successful(returnValue)))

  }

}
