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
import connectors.{IFSBusinessDetailsConnector, IFSConnector}
import models.common.{JourneyContextWithNino, JourneyName}
import models.connector.api_1505.CreateLossClaimRequestBody
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
                                                repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _      <- createUpdateOrDeleteAnnualSummaries(ctx, answers)
      _      <- createUpdateOrDeleteLossClaim(ctx, answers) // TODO SASS-10335 update Spec to include the change
      _      <- createUpdateOrDeleteBroughtForwardLoss(ctx, answers)
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield result

  def getProfitOrLoss(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ProfitOrLossJourneyAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx)
      apiResponse <- ifsConnector.getLossClaim(ctx, "claimId") // TODO Need to check how claimId be passed
    } yield maybeData match {
      case Some(journeyAnswer) => Option(ProfitOrLossJourneyAnswers(apiResponse, journeyAnswer))
      case _                   => None
    }

  private def createUpdateOrDeleteAnnualSummaries(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- ifsConnector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[ProfitOrLossDb](maybeAnnualSummaries, answers)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(ifsConnector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  private def createUpdateOrDeleteLossClaim(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      maybeExistingLossClaim <- getLossClaimByBusinessId(ctx)
      result                 <- handleLossClaim(ctx, maybeExistingLossClaim, answers.toLossClaimSubmission(ctx))
    } yield result

  private def getLossClaimByBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[Unit]] =
    // TODO SASS-10335: -- this is now the List logic not 10335 connectivity ticket
    // 1. Call list of claims (individual claims are called/edited/deleted by claimId. We have businessId, not claimId)
    // 2. If returns Left(Error(Not_Found)), return Right(None)
    // 3. Filter Option{ single lossClaim } from the list by its businessId if it exists, else None
//    val lossClaims = ifsBusinessDetailsConnector.listLossClaims(ctx.nino, ctx.taxYear)
//    lossClaims.transform {
//      case Right(list) =>
//        Right(list.find(_.businessId == ctx.businessId.value))
//      case Left(error) if error.status == NOT_FOUND =>
//        Right(None)
//      case Left(otherError) =>
//        Left(otherError)
//    }
    EitherT.rightT[Future, ServiceError](None)

  private def handleLossClaim(ctx: JourneyContextWithNino, maybeExistingData: Option[Unit], maybeSubmissionData: Option[CreateLossClaimRequestBody])(
      implicit hc: HeaderCarrier): ApiResultT[Unit] =
    (maybeExistingData, maybeSubmissionData) match {
      // TODO SASS-10335 update endpoints below -- not just this ticket anymore, it was split into CRUD
      case (None, Some(submissionData)) => ifsConnector.createLossClaim(ctx, submissionData).map(_ => ()) // Create
      case (Some(_), Some(_))           => EitherT.rightT[Future, ServiceError](())                       // Update
      case (Some(_), None)              => EitherT.rightT[Future, ServiceError](())                       // Delete
      case (None, None)                 => EitherT.rightT[Future, ServiceError](())                       // Do nothing
    }

  def getBroughtForwardLossByBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[LossData]] = {
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
      hc: HeaderCarrier): ApiResultT[Unit] =
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

  private def getDbAnswers(ctx: JourneyContextWithNino): EitherT[Future, ServiceError, Option[ProfitOrLossJourneyAnswers]] =
    for {
      row            <- repository.get(ctx.toJourneyContext(JourneyName.ProfitOrLoss))
      maybeDbAnswers <- getPersistedAnswers[ProfitOrLossJourneyAnswers](row)
    } yield maybeDbAnswers

}
