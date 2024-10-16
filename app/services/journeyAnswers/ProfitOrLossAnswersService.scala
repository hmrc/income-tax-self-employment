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
import models.connector.api_1870.LossData
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
      _      <- createUpdateOrDeleteAnnualSummaries(ctx, answers)
      _      <- createUpdateOrDeleteBroughtForwardLoss(ctx, answers)
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield result

  private def createUpdateOrDeleteAnnualSummaries(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- ifsConnector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[ProfitOrLossDb](maybeAnnualSummaries, answers)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(ifsConnector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  def getLossByBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[LossData]] = {
    val losses = ifsBusinessDetailsConnector.listBroughtForwardLosses(ctx.nino, ctx.taxYear)
    losses.transform {
      case Right(list) =>
        Right(list.losses.find(_.businessId == ctx.businessId.value))
      case Left(error) if error.status == NOT_FOUND =>
        Right(None)
      case Left(otherError) =>
        Left(otherError)
    }
  }

  def createUpdateOrDeleteBroughtForwardLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] =
    for {
      maybeLoss <- getLossByBusinessId(ctx)
      result    <- handleBroughtForwardLoss(ctx, maybeLoss, answers)
    } yield result

  private def handleBroughtForwardLoss(ctx: JourneyContextWithNino, maybeLoss: Option[LossData], answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    maybeLoss match {
      case Some(lossData) => handleBroughtForwardLossWithExistingLoss(ctx, lossData, answers)
      case None           => handleBroughtForwardLossNoExistingLoss(ctx, answers)
    }

  private def handleBroughtForwardLossWithExistingLoss(ctx: JourneyContextWithNino, lossData: LossData, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    (answers.unusedLossAmount, answers.whichYearIsLossReported) match {
      case (Some(amount), Some(whichYear)) =>
        if (whichYear.apiTaxYear == lossData.taxYearBroughtForwardFrom) {
          ifsBusinessDetailsConnector
            .updateBroughtForwardLoss(ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossData(ctx, lossData.lossId, amount))
            .map(_ => ())
        } else {
          ifsBusinessDetailsConnector
            .updateBroughtForwardLossYear(
              ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossYearData(ctx, lossData.lossId, amount, whichYear.apiTaxYear))
            .map(_ => ())
        }
      case _ => ifsBusinessDetailsConnector.deleteBroughtForwardLoss(ctx.nino, lossData.lossId)
    }

  private def handleBroughtForwardLossNoExistingLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    (answers.unusedLossAmount, answers.whichYearIsLossReported) match {
      // No previous data, data to be submitted -> create
      case (Some(amount), Some(year)) =>
        ifsBusinessDetailsConnector
          .createBroughtForwardLoss(
            ProfitOrLossJourneyAnswers
              .toCreateBroughtForwardLossData(ctx, amount, year))
          .map(_ => ())
      // No previous data, no data to submit -> do nothing
      case _ => EitherT.rightT[Future, ServiceError](())
    }
}
