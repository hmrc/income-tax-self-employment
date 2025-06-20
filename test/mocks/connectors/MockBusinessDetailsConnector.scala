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
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object MockBusinessDetailsConnector {

  val mockInstance: BusinessDetailsConnector = mock[BusinessDetailsConnector]

  def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)
                        (returnValue: Either[ServiceError, Option[BusinessDetailsHipSuccessWrapper]]): ScalaOngoingStubbing[ApiResultT[Option[BusinessDetailsHipSuccessWrapper]]] =
    when(mockInstance.getBusinessDetails(eqTo(businessId), eqTo(mtditid), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT.apply(Future.successful(returnValue)))

}
