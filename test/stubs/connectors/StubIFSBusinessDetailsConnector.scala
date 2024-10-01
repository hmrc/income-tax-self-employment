/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.IFSBusinessDetailsConnector
import connectors.IFSBusinessDetailsConnector._
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.api_1500.CreateBroughtForwardLossRequestData
import models.connector.api_1501.UpdateBroughtForwardLossRequestData
import models.connector.{api_1171, api_1500, api_1501, api_1502, api_1870, api_1871}
import models.domain.ApiResultT
import models.error.ServiceError
import stubs.connectors.StubIFSConnector._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubIFSBusinessDetailsConnector(
    getBusinessesResult: Api1171Response = api1171EmptyResponse.asRight,
    getBusinessIncomeSourcesSummaryResult: Api1871Response = api1871EmptyResponse.asRight,
    createBroughtForwardLossResult: Api1500Response = api1500EmptyResponse.asRight,
    updateBroughtForwardLossResult: Api1501Response = api1501EmptyResponse.asRight,
    getBroughtForwardLossResult: Api1502Response = api1502EmptyResponse.asRight,
    deleteBroughtForwardLossResult: Either[ServiceError, Unit] = Right(()),
    listBroughtForwardLossesResult: Api1870Response = api1870EmptyResponse.asRight
) extends IFSBusinessDetailsConnector {

  def getBusinesses(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1171.SuccessResponseSchema] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse] =
    EitherT.fromEither[Future](getBusinessIncomeSourcesSummaryResult)

  def createBroughtForwardLoss(
      data: CreateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1500.SuccessResponseSchema] =
    EitherT.fromEither[Future](createBroughtForwardLossResult)

  def updateBroughtForwardLoss(
      data: UpdateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1501.SuccessResponseSchema] =
    EitherT.fromEither[Future](updateBroughtForwardLossResult)

  def getBroughtForwardLoss(nino: Nino, lossId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1502.SuccessResponseSchema] =
    EitherT.fromEither[Future](getBroughtForwardLossResult)

  def deleteBroughtForwardLoss(nino: Nino, lossId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    EitherT.fromEither[Future](deleteBroughtForwardLossResult)

  def listBroughtForwardLosses(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1870.SuccessResponseSchema] =
    EitherT.fromEither[Future](listBroughtForwardLossesResult)
}
