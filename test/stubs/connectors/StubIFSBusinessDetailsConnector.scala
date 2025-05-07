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

package stubs.connectors

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import connectors.IFS.IFSBusinessDetailsConnector
import IFSBusinessDetailsConnector._
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.api_1500.CreateBroughtForwardLossRequestData
import models.connector.api_1501._
import models.connector.{api_1500, api_1501, api_1502, api_1870, api_1871, api_2085, businessDetailsConnector}
import models.domain.ApiResultT
import stubs.connectors.StubIFSConnector._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubIFSBusinessDetailsConnector(
    getBusinessesResult: Api1171Response = api1171EmptyResponse.asRight,
    getBusinessIncomeSourcesSummaryResult: Api1871Response = api1871EmptyResponse.asRight,
    createBroughtForwardLossResult: Api1500Response = api1500EmptyResponse.asRight,
    updateBroughtForwardLossResult: Api1501Response = api1501EmptyResponse.asRight,
    getBroughtForwardLossResult: Api1502Response = api1502EmptyResponse.asRight,
    listBroughtForwardLossesResult: Api1870Response = api1870EmptyResponse.asRight,
    listOfIncomeSources: Api2085Response = api2085EmptyResponse.asRight
) extends IFSBusinessDetailsConnector {
  var updatedBroughtForwardLossData: Option[UpdateBroughtForwardLossRequestBody] = None

  def getBusinesses(
      nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[businessDetailsConnector.BusinessDetailsSuccessResponseSchema] =
    EitherT.fromEither[Future](getBusinessesResult)

  def getBusinessIncomeSourcesSummary(taxYear: TaxYear, nino: Nino, businessId: BusinessId)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1871.BusinessIncomeSourcesSummaryResponse] =
    EitherT.fromEither[Future](getBusinessIncomeSourcesSummaryResult)

  def createBroughtForwardLoss(
      data: CreateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1500.SuccessResponseSchema] = {
    if (createBroughtForwardLossResult.isRight) updatedBroughtForwardLossData = Some(UpdateBroughtForwardLossRequestBody(data.body.lossAmount))
    EitherT.fromEither[Future](createBroughtForwardLossResult)
  }

  def updateBroughtForwardLoss(
      data: UpdateBroughtForwardLossRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[api_1501.SuccessResponseSchema] = {
    if (updateBroughtForwardLossResult.isRight) updatedBroughtForwardLossData = Some(data.body)
    EitherT.fromEither[Future](updateBroughtForwardLossResult)
  }

  def getBroughtForwardLoss(nino: Nino, lossId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1502.SuccessResponseSchema] =
    EitherT.fromEither[Future](getBroughtForwardLossResult)

  def listBroughtForwardLosses(nino: Nino, taxYear: TaxYear)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_1870.SuccessResponseSchema] =
    EitherT.fromEither[Future](listBroughtForwardLossesResult)

  def getListOfIncomeSources(taxYear: TaxYear, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[api_2085.ListOfIncomeSources] =
    EitherT.fromEither[Future](listOfIncomeSources)

}
