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

package services

import cats.data.EitherT
import cats.implicits._
import connectors.IFSConnector.Api1803Response
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestBody
import models.connector.{ApiResponse, api_1803}
import models.database.{DatabaseAnswers, JourneyAnswers}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.FrontendAnswers
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Reads
import utils.EitherTOps._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

package object journeyAnswers {

  def getPersistedAnswers[A: Reads](row: Option[JourneyAnswers])(implicit ec: ExecutionContext, ct: ClassTag[A]): ApiResultT[Option[A]] =
    row.traverse(getPersistedAnswers[A])

  def getPersistedAnswers[A: Reads](row: JourneyAnswers)(implicit ec: ExecutionContext, ct: ClassTag[A]): ApiResultT[A] =
    EitherT.fromEither[Future](row.validatedAs[A]).leftAs[ServiceError]

  def handleAnnualSummariesForResubmission[A <: DatabaseAnswers](
      maybeAnnualSummaries: Api1803Response,
      answers: FrontendAnswers[A]): ApiResponse[Option[CreateAmendSEAnnualSubmissionRequestBody]] =
    for {
      maybeCurrent <- handleOptionalAnnualSummaries(maybeAnnualSummaries)
      existingData          = maybeCurrent.getOrElse(api_1803.SuccessResponseSchema.empty)
      updatedSubmissionBody = createUpdatedAnnualSummariesRequestBody(existingData, answers)
    } yield updatedSubmissionBody

  private def handleOptionalAnnualSummaries(response: Api1803Response): ApiResponse[Option[api_1803.SuccessResponseSchema]] =
    response match {
      case Right(data)                              => Right(Some(data))
      case Left(error) if error.status == NOT_FOUND => Right(None)
      case Left(error)                              => Left(error)
    }

  private def createUpdatedAnnualSummariesRequestBody[A <: DatabaseAnswers](
      existingData: api_1803.SuccessResponseSchema,
      answers: FrontendAnswers[A]): Option[CreateAmendSEAnnualSubmissionRequestBody] = {
    val requestBody              = existingData.toRequestBody
    val updatedAnnualAdjustments = answers.toDownStreamAnnualAdjustments(requestBody.annualAdjustments).returnNoneIfEmpty
    val updatedAnnualAllowances  = answers.toDownStreamAnnualAllowances(requestBody.annualAllowances).returnNoneIfEmpty
    CreateAmendSEAnnualSubmissionRequestBody.mkRequest(updatedAnnualAdjustments, updatedAnnualAllowances, requestBody.annualNonFinancials)
  }
}
