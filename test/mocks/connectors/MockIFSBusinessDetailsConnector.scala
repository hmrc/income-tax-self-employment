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
import org.scalatestplus.mockito.MockitoSugar.mock
import connectors.IFS.IFSBusinessDetailsConnector
import models.common._
import models.error.ServiceError
import models.domain.ApiResultT
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.{api_1870, api_2085}
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import org.mockito.stubbing.ScalaOngoingStubbing
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.when
import stubs.connectors.StubIFSConnector.api1870SuccessResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


object MockIFSBusinessDetailsConnector {

  val mockInstance: IFSBusinessDetailsConnector = mock[IFSBusinessDetailsConnector]

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)
                                     (returnValue: Either[ServiceError, BusinessIncomeSourcesSummaryResponse]): ScalaOngoingStubbing[ApiResultT[BusinessIncomeSourcesSummaryResponse]] =
    when(mockInstance.getBusinessIncomeSourcesSummary(eqTo(taxYear), eqTo(nino), eqTo(businessId))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT(Future.successful(returnValue)))

  def getBusinesses(nino: Nino)
                   (returnValue: Either[ServiceError, BusinessDetailsSuccessResponseSchema]): ScalaOngoingStubbing[ApiResultT[BusinessDetailsSuccessResponseSchema]] =
    when(mockInstance.getBusinesses(eqTo(nino))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT(Future.successful(returnValue)))

  def getListOfIncomeSources(taxYear: TaxYear, nino: Nino)
                            (returnValue: Either[ServiceError, api_2085.ListOfIncomeSources]): ScalaOngoingStubbing[ApiResultT[api_2085.ListOfIncomeSources]] =
    when(mockInstance.getListOfIncomeSources(eqTo(taxYear), eqTo(nino))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(EitherT(Future.successful(returnValue)))

}
