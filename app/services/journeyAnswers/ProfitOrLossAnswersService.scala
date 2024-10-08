/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.{IFSBusinessDetailsConnector, IFSConnector}
import models.common.{JourneyContextWithNino, JourneyName}
import models.connector.api_1502
import models.database.adjustments.ProfitOrLossDb
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.ProfitOrLossJourneyAnswers
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

}

@Singleton
class ProfitOrLossAnswersServiceImpl @Inject() (ifsConnector: IFSConnector,
                                                ifsBusinessDetailsConnector: IFSBusinessDetailsConnector,
                                                repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _      <- submitAnnualSummaries(ctx, answers)
      _      <- createUpdateOrDeleteBroughtForwardLoss(ctx, answers)
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield result

  private def submitAnnualSummaries(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- ifsConnector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[ProfitOrLossDb](maybeAnnualSummaries, answers)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(ifsConnector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  private def createUpdateOrDeleteBroughtForwardLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] =
    EitherT {
      for {
        existingLoss <- ifsBusinessDetailsConnector.getBroughtForwardLoss(ctx.nino, ctx.businessId).value
        result       <- handleBroughtForwardLoss(existingLoss, ctx, answers)
      } yield result
    }

  private def handleBroughtForwardLoss(existingLoss: Either[ServiceError, api_1502.SuccessResponseSchema],
                                       ctx: JourneyContextWithNino,
                                       answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    (answers.unusedLossAmount, answers.whichYearIsLossReported, existingLoss) match {
      // No previous data, data to be submitted -> create
      case (Some(amount), Some(year), Left(error)) if error.status == NOT_FOUND =>
        ifsBusinessDetailsConnector
          .createBroughtForwardLoss(
            ProfitOrLossJourneyAnswers
              .toCreateBroughtForwardLossData(ctx, amount, year))
          .value
          .map(_.map(_ => ()))
      // Previous data, data to be submitted -> update
      case (Some(amount), Some(_), Right(_)) =>
        ifsBusinessDetailsConnector
          .updateBroughtForwardLoss(
            ProfitOrLossJourneyAnswers
              .toUpdateBroughtForwardLossData(ctx, amount))
          .value
          .map(_.map(_ => ()))
      // Previous data, no data to be submitted -> delete
      case (None, None, Right(_)) => ifsBusinessDetailsConnector.deleteBroughtForwardLoss(ctx.nino, ctx.businessId).value.map(_.map(_ => ()))
      // No previous data, no data to submit -> do nothing
      case (None, None, Left(error)) if error.status == NOT_FOUND => Future.successful(Right(()))
      // API error case
      case (_, _, Left(error)) => Future.successful(Left(error))
    }
}
