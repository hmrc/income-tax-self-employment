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
import connectors.ReliefClaimsConnector
import models.common._
import models.connector.ReliefClaimType
import models.connector.api_1505.CreateLossClaimSuccessResponse
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss
import models.frontend.adjustments.WhatDoYouWantToDoWithLoss.toReliefClaimType
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReliefClaimsService @Inject()(reliefClaimsConnector: ReliefClaimsConnector,
                                    appConfig: AppConfig)
                                    (implicit ec: ExecutionContext) {

  def getAllReliefClaims(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] =
    for {
      reliefClaims <- reliefClaimsConnector.getAllReliefClaims(ctx.taxYear, ctx.businessId)
    } yield reliefClaims.filter(claim =>
      claim.isSelfEmploymentClaim &&
        claim.taxYearClaimedFor == ctx.taxYear.endYear.toString &&
        claim.incomeSourceId == ctx.businessId.value)

  def createReliefClaims(ctx: JourneyContextWithNino,
                         answers: Seq[WhatDoYouWantToDoWithLoss])
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Seq[CreateLossClaimSuccessResponse]] =
    if (answers.isEmpty) {
      EitherT.pure(Future.successful(Nil: List[CreateLossClaimSuccessResponse]))
    } else {
      answers.map { answer =>
        reliefClaimsConnector.createReliefClaim(ctx, toReliefClaimType(answer))
      }.sequence
    }

  case class UpdateReliefClaimsResponse(created: Seq[ReliefClaimType], deleted: Seq[ReliefClaimType])

  def updateReliefClaims(ctx: JourneyContextWithNino,
                         oldAnswers: List[ReliefClaim],
                         newAnswers: Seq[WhatDoYouWantToDoWithLoss])
                        (implicit hc: HeaderCarrier): ApiResultT[UpdateReliefClaimsResponse] = {

    val newAnswersAsReliefClaimType = newAnswers.map(toReliefClaimType)
    val answersToKeep               = oldAnswers.filter(claim => newAnswersAsReliefClaimType.contains(claim.reliefClaimed))
    val answersToCreate             = newAnswersAsReliefClaimType.filter(claim => oldAnswers.exists(_.reliefClaimed == claim))
    val answersToDelete             = oldAnswers.diff(answersToKeep)

    val deleteResponses = answersToDelete.map(answer => reliefClaimsConnector.deleteReliefClaim(ctx, answer.claimId))
    val createResponses = answersToCreate.map(answer => reliefClaimsConnector.createReliefClaim(ctx, answer))

    for {
      _ <- deleteResponses.sequence
      _ <- createResponses.sequence
    } yield UpdateReliefClaimsResponse(answersToCreate, answersToDelete.map(_.reliefClaimed))
  }

}
