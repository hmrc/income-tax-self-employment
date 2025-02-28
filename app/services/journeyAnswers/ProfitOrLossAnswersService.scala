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

package services.journeyAnswers

import cats.data.EitherT
import connectors.{HipConnector, IFSBusinessDetailsConnector, IFSConnector}
import models.common.{JourneyContextWithNino, JourneyName, Nino, TaxYear}
import models.connector.api_1500.CreateBroughtForwardLossRequestData
import models.connector.api_1501.UpdateBroughtForwardLossYear
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
  def getProfitOrLoss(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ProfitOrLossJourneyAnswers]]

}

@Singleton
class ProfitOrLossAnswersServiceImpl @Inject() (ifsConnector: IFSConnector,
                                                ifsBusinessDetailsConnector: IFSBusinessDetailsConnector,
                                                hipConnector: HipConnector,
                                                reliefClaimsService: ReliefClaimsService,
                                                repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _      <- handleAnnualSummaries(ctx, answers)
      _      <- storeReliefClaimAnswers(ctx, answers)
      _      <- storeBroughtForwardLossAnswers(ctx, answers)
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield result

  def getProfitOrLoss(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ProfitOrLossJourneyAnswers]] =
    for {
      optProfitOrLoss <- getDbAnswers(ctx)
    } yield optProfitOrLoss

  private def handleAnnualSummaries(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- ifsConnector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[ProfitOrLossDb](maybeAnnualSummaries, answers)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(ifsConnector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  def storeReliefClaimAnswers(ctx: JourneyContextWithNino, submittedAnswers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      reliefClaims <- reliefClaimsService.getAllReliefClaims(ctx)
      optLossClaimAnswer = submittedAnswers.whatDoYouWantToDoWithLoss
      result <- (reliefClaims, optLossClaimAnswer) match {
        case (Nil, Some(lossClaimAnswer)) if lossClaimAnswer.nonEmpty =>
          reliefClaimsService.createReliefClaims(ctx, lossClaimAnswer)
        case (claims, Some(lossClaimAnswer)) if claims.nonEmpty && lossClaimAnswer.nonEmpty =>
          reliefClaimsService.updateReliefClaims(ctx, claims, lossClaimAnswer)
        case (claims, None | Some(Nil)) if claims.nonEmpty =>
          reliefClaimsService.deleteReliefClaims(ctx, claims)
        case _ =>
          EitherT.rightT[Future, ServiceError](())
      }
    } yield result

  private def getBroughtForwardLossByBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[LossData]] = {
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

  def storeBroughtForwardLossAnswers(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      maybeLoss <- getBroughtForwardLossByBusinessId(ctx)
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
          updateBroughtForwardLossYear(
            ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossYearData(ctx, lossData.lossId, amount, whichYear.apiTaxYear))
            .map(_ => ())
        }
      case _ => deleteBroughtForwardLoss(ctx.nino, TaxYear.asTy(lossData.taxYearBroughtForwardFrom), lossData.lossId)
    }

  private def deleteBroughtForwardLoss(nino: Nino, taxYear: TaxYear, lossId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] =
    hipConnector.deleteBroughtForwardLoss(nino = nino, taxYear = taxYear, lossId = lossId)

  private def updateBroughtForwardLossYear(data: UpdateBroughtForwardLossYear)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    deleteBroughtForwardLoss(data.nino, data.taxYear, data.lossId).map(_ =>
      ifsBusinessDetailsConnector.createBroughtForwardLoss(CreateBroughtForwardLossRequestData(data.nino, data.taxYear, data.body)))

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

  private def getDbAnswers(ctx: JourneyContextWithNino): EitherT[Future, ServiceError, Option[ProfitOrLossJourneyAnswers]] =
    for {
      row            <- repository.get(ctx.toJourneyContext(JourneyName.ProfitOrLoss))
      maybeDbAnswers <- getPersistedAnswers[ProfitOrLossJourneyAnswers](row)
    } yield maybeDbAnswers

}
