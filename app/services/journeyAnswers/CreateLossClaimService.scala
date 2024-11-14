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

package services.journeyAnswers

import cats.data.EitherT
import connectors.IFSConnector
import models.common.JourneyContextWithNino
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.domain.ApiResultT
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateLossClaimService @Inject() (ifsConnector: IFSConnector) {

  def createLossClaimType(ctx: JourneyContextWithNino, request: CreateLossClaimRequestBody)(implicit
                                                                                            hc: HeaderCarrier,
                                                                                            ec: ExecutionContext): ApiResultT[Option[CreateLossClaimSuccessResponse]] =
    EitherT {
      ifsConnector
        .createLossClaim(ctx, request)
        .value
        .map {
          case Right(successResponse) => Right(Some(successResponse))
          case Left(error: SingleDownstreamErrorBody) => Left(mapDownstreamErrors(error))
        }
    }

  private val errorMap: Map[SingleDownstreamErrorBody, ServiceError] = Map(
    SingleDownstreamErrorBody.invalidTaxableEntityId   -> ServiceError.FormatNinoError,
    SingleDownstreamErrorBody.invalidPayload           -> ServiceError.InternalServerError,
    SingleDownstreamErrorBody.invalidCorrelationId     -> ServiceError.InternalServerError,
    SingleDownstreamErrorBody.duplicate                -> ServiceError.RuleDuplicateSubmissionError,
    SingleDownstreamErrorBody.accountingPeriodNotEnded -> ServiceError.RuleAccountingPeriodNotEndedError,
    SingleDownstreamErrorBody.invalidClaimType         -> ServiceError.RuleTypeOfClaimInvalidError,
    SingleDownstreamErrorBody.noAccountingPeriod       -> ServiceError.RuleNoAccountingPeriodError,
    SingleDownstreamErrorBody.taxYearNotSupported      -> ServiceError.RuleTaxYearNotSupportedError,
    SingleDownstreamErrorBody.incomeSourceNotFound     -> ServiceError.MatchingResourceNotFoundError,
    SingleDownstreamErrorBody.serverError              -> ServiceError.InternalServerError,
    SingleDownstreamErrorBody.serviceUnavailable       -> ServiceError.InternalServerError
  )

  private def mapDownstreamErrors(downstreamError: SingleDownstreamErrorBody): ServiceError = {
    errorMap.getOrElse(downstreamError, ServiceError.ServiceUnavailableError("Unexpected error occurred."))
  }

}