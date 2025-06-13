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
import cats.implicits._
import config.AppConfig
import connectors.HIP.HipReliefClaimsConnector
import connectors.ReliefClaimsConnector
import models.common._
import models.connector.api_1505.ClaimId
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.toReliefClaimType
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ReliefClaimsService @Inject()(
                                     reliefClaimsConnector: ReliefClaimsConnector,
                                     hipReliefClaimsConnector: HipReliefClaimsConnector,
                                     appConfig: AppConfig
                                   )(implicit ec: ExecutionContext) {

  def getAllReliefClaims(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] =
    for {
      reliefClaims <- reliefClaimsConnector.getAllReliefClaims(ctx)
    } yield reliefClaims.filter(claim =>
      claim.isSelfEmploymentClaim &&
        claim.taxYearClaimedFor == ctx.taxYear.endYear.toString &&
        claim.incomeSourceId == ctx.businessId.value)

  def createReliefClaims(ctx: JourneyContextWithNino, answers: Seq[WhatDoYouWantToDoWithLoss])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[List[ClaimId]] =
    if (answers.isEmpty) {
      EitherT.pure(Nil: List[ClaimId])
    } else {
      answers
        .map { answer =>
          if (appConfig.hipMigration1505Enabled) hipReliefClaimsConnector.createReliefClaim(ctx, toReliefClaimType(answer))
          else reliefClaimsConnector.createReliefClaim(ctx, toReliefClaimType(answer))
        }
        .sequence
        .map(_.toList)
    }

  def updateReliefClaims(ctx: JourneyContextWithNino, oldAnswers: List[ReliefClaim], newAnswers: Seq[WhatDoYouWantToDoWithLoss])(implicit
      hc: HeaderCarrier): ApiResultT[UpdateReliefClaimsResponse] = {

    val newAnswersAsReliefClaimType = newAnswers.map(toReliefClaimType)
    val answersToKeep               = oldAnswers.filter(claim => newAnswersAsReliefClaimType.contains(claim.reliefClaimed))
    val answersToCreate             = newAnswersAsReliefClaimType.filter(claim => oldAnswers.exists(_.reliefClaimed == claim))
    val answersToDelete             = oldAnswers.diff(answersToKeep)

    val deleteResponses = answersToDelete.map(answer =>
      if(appConfig.hipMigration1509Enabled) hipReliefClaimsConnector.deleteReliefClaim(ctx, answer.claimId)
      else reliefClaimsConnector.deleteReliefClaim(ctx, answer.claimId))

    val createResponses = answersToCreate.map(answer =>
      if(appConfig.hipMigration1505Enabled) hipReliefClaimsConnector.createReliefClaim(ctx, answer)
      else reliefClaimsConnector.createReliefClaim(ctx, answer))

    for {
      _ <- deleteResponses.sequence
      _ <- createResponses.sequence
    } yield UpdateReliefClaimsResponse(
      created = answersToCreate.map(WhatDoYouWantToDoWithLoss.fromReliefClaimType),
      deleted = answersToDelete.map(answer => WhatDoYouWantToDoWithLoss.fromReliefClaimType(answer.reliefClaimed))
    )
  }

  def deleteReliefClaims(ctx: JourneyContextWithNino, reliefClaims: Seq[ReliefClaim])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    if (reliefClaims.isEmpty) {
      EitherT.pure(())
    } else {
      reliefClaims
        .map { reliefClaim =>
          if (appConfig.hipMigration1509Enabled)
            hipReliefClaimsConnector.deleteReliefClaim(ctx, reliefClaim.claimId)
          else
            reliefClaimsConnector.deleteReliefClaim(ctx, reliefClaim.claimId)
        }
        .sequence
        .map(_ => ())
    }

}

case class UpdateReliefClaimsResponse(created: Seq[WhatDoYouWantToDoWithLoss], deleted: Seq[WhatDoYouWantToDoWithLoss])
