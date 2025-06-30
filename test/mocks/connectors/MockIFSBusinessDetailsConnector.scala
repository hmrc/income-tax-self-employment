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
import connectors.IFS.IFSBusinessDetailsConnector
import models.common._
import models.connector.api_1500.{CreateBroughtForwardLossRequestData, SuccessResponseSchema => SuccessResponseSchema1500}
import models.connector.api_1501.{UpdateBroughtForwardLossRequestData, SuccessResponseSchema => SuccessResponseSchema1501}
import models.connector.api_1502.{SuccessResponseSchema, SuccessResponseSchema => SuccessResponseSchema1502}
import models.connector.api_1870.{SuccessResponseSchema => SuccessResponseSchema1870}
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.{api_1500, api_1501, api_1870, api_2085}
import models.connector.api_2085.ListOfIncomeSources
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.{CallHandler3, CallHandler4, CallHandler5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


trait MockIFSBusinessDetailsConnector extends TestSuite with MockFactory with OneInstancePerTest {

  val mockIFSBusinessDetailsConnector: IFSBusinessDetailsConnector = mock[IFSBusinessDetailsConnector]

  object IFSBusinessDetailsConnectorMock {

    def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)
                                       (returnValue: Either[ServiceError, BusinessIncomeSourcesSummaryResponse]): CallHandler5[TaxYear, Nino, BusinessId, HeaderCarrier, ExecutionContext, ApiResultT[BusinessIncomeSourcesSummaryResponse]] =
      (mockIFSBusinessDetailsConnector.getBusinessIncomeSourcesSummary(_: TaxYear, _: Nino, _: BusinessId)(_: HeaderCarrier, _: ExecutionContext))
        .expects(taxYear, nino, businessId, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def getBusinesses(nino: Nino)
                     (returnValue: Either[ServiceError, BusinessDetailsSuccessResponseSchema]): CallHandler3[Nino, HeaderCarrier, ExecutionContext, ApiResultT[BusinessDetailsSuccessResponseSchema]] =
      (mockIFSBusinessDetailsConnector.getBusinesses(_: Nino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def getListOfIncomeSources(taxYear: TaxYear, nino: Nino)
                              (returnValue: Either[ServiceError, api_2085.ListOfIncomeSources]): CallHandler4[TaxYear, Nino, HeaderCarrier, ExecutionContext, ApiResultT[ListOfIncomeSources]] =
      (mockIFSBusinessDetailsConnector.getListOfIncomeSources(_: TaxYear, _: Nino)(_: HeaderCarrier, _: ExecutionContext))
        .expects(taxYear, nino, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def getBroughtForwardLoss(nino: Nino, lossId: String)
                             (returnValue: Either[ServiceError, SuccessResponseSchema1502]): CallHandler4[Nino, String, HeaderCarrier, ExecutionContext, ApiResultT[SuccessResponseSchema]] =
      (mockIFSBusinessDetailsConnector.getBroughtForwardLoss(_: Nino, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, lossId, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def getListOfBroughtForwardLosses(nino: Nino, taxYear: TaxYear)
                                     (returnValue: Either[ServiceError, SuccessResponseSchema1870]): CallHandler4[Nino, TaxYear, HeaderCarrier, ExecutionContext, ApiResultT[api_1870.SuccessResponseSchema]] =
      (mockIFSBusinessDetailsConnector.listBroughtForwardLosses(_: Nino, _: TaxYear)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, taxYear, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def createBroughtForwardLoss(data: CreateBroughtForwardLossRequestData)
                                (returnValue: Either[ServiceError, SuccessResponseSchema1500]): CallHandler3[CreateBroughtForwardLossRequestData, HeaderCarrier, ExecutionContext, ApiResultT[api_1500.SuccessResponseSchema]] =
      (mockIFSBusinessDetailsConnector.createBroughtForwardLoss(_: CreateBroughtForwardLossRequestData)(_: HeaderCarrier, _: ExecutionContext))
        .expects(data, *, *)
        .returning(EitherT(Future.successful(returnValue)))

    def updateBroughtForwardLoss(data: UpdateBroughtForwardLossRequestData)
                                (returnValue: Either[ServiceError, SuccessResponseSchema1501]): CallHandler3[UpdateBroughtForwardLossRequestData, HeaderCarrier, ExecutionContext, ApiResultT[api_1501.SuccessResponseSchema]] =
      (mockIFSBusinessDetailsConnector.updateBroughtForwardLoss(_: UpdateBroughtForwardLossRequestData)(_: HeaderCarrier, _: ExecutionContext))
        .expects(data, *, *)
        .returning(EitherT(Future.successful(returnValue)))

  }

}
