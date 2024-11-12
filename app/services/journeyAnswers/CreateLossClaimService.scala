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
import models.connector.api_1505.{RequestSchemaAPI1505, SuccessResponseAPI1505}
import models.domain.ApiResultT
import models.error.{DownstreamError, ServiceError}
import play.api.http.Status._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateLossClaimService @Inject() (ifsConnector: IFSConnector) {

  def mapDownstreamErrors(serviceError: ServiceError): DownstreamError = {
    serviceError.status match {
      case 400 => serviceError.errorMessage match {
        case "INVALID_TAXABLE_ENTITY_ID"  => DownstreamError.GenericDownstreamError(BAD_REQUEST, "Submission has not passed validation. Invalid parameter taxableEntityId.")
        case "INVALID_PAYLOAD"            => DownstreamError.GenericDownstreamError(BAD_REQUEST, "Submission has not passed validation. Invalid payload.")
        case "INVALID_CORRELATIONID"      => DownstreamError.GenericDownstreamError(BAD_REQUEST, "Submission has not passed validation. Invalid Header parameter CorrelationId.")
        case _ => DownstreamError.GenericDownstreamError(BAD_REQUEST, "Bad request.")
      }
      case 409 => DownstreamError.GenericDownstreamError(CONFLICT, "The remote endpoint has indicated that the claim for relief already exists.")
      case 422 => serviceError.errorMessage match {
        case "ACCOUNTING_PERIOD_NOT_ENDED" => DownstreamError.GenericDownstreamError(UNPROCESSABLE_ENTITY, "The remote endpoint has indicated that <Message received from backend ITSD>.")
        case "INVALID_CLAIM_TYPE"          => DownstreamError.GenericDownstreamError(UNPROCESSABLE_ENTITY, "The remote endpoint has indicated that the claim type not valid for income source.")
        case "NO_ACCOUNTING_PERIOD"        => DownstreamError.GenericDownstreamError(UNPROCESSABLE_ENTITY, "The remote endpoint has indicated that no accounting period for the year of the claim.")
        case "TAX_YEAR_NOT_SUPPORTED"      => DownstreamError.GenericDownstreamError(UNPROCESSABLE_ENTITY, "The remote endpoint has indicated that the brought forward losses and loss claims are not supported for the specified tax year.")
        case _ => DownstreamError.GenericDownstreamError(UNPROCESSABLE_ENTITY, "Unprocessable entity.")
      }
      case 404 => DownstreamError.GenericDownstreamError(NOT_FOUND, "The remote endpoint has indicated that the income source cannot be found.")
      case 500 => DownstreamError.GenericDownstreamError(INTERNAL_SERVER_ERROR, "IF is currently experiencing problems that require live service intervention.")
      case 503 => DownstreamError.GenericDownstreamError(SERVICE_UNAVAILABLE, "Dependent systems are currently not responding.")
      case _ => DownstreamError.GenericDownstreamError(INTERNAL_SERVER_ERROR, "An unexpected error occurred.")
    }
  }


  def createLossClaimType(ctx: JourneyContextWithNino, request: RequestSchemaAPI1505)(implicit
                                                                                      hc: HeaderCarrier,
                                                                                      ec: ExecutionContext): ApiResultT[Option[SuccessResponseAPI1505]] = {

    EitherT {
      ifsConnector
        .createLossClaim(ctx, request)
        .value
        .map {
          case Right(successResponse) => Right(Some(successResponse))
          case Left(error) => Left(mapDownstreamErrors(error))
        }
    }
  }

}
