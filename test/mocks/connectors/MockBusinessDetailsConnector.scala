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
import connectors.HIP.BusinessDetailsConnector
import models.common.{BusinessId, Mtditid, Nino}
import models.connector.businessDetailsConnector.BusinessDetailsHipSuccessWrapper
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler5
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockBusinessDetailsConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockBusinessDetailsConnector: BusinessDetailsConnector = mock[BusinessDetailsConnector]

  object BusinessDetailsConnectorMock {

    def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)
                          (returnValue: Either[ServiceError, Option[BusinessDetailsHipSuccessWrapper]]): CallHandler5[Option[BusinessId], Mtditid, Nino, HeaderCarrier, ExecutionContext, ApiResultT[Option[BusinessDetailsHipSuccessWrapper]]] =
      (mockBusinessDetailsConnector.getBusinessDetails(_: Option[BusinessId], _: Mtditid, _: Nino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(businessId, mtditid, nino, *, *)
        .returning(EitherT.apply(Future.successful(returnValue)))

  }
}
