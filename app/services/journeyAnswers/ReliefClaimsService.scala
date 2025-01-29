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
import cats.implicits._
import config.AppConfig
import connectors.ReliefClaimsConnector
import models.common.JourneyName.ProfitOrLoss
import models.common.{BusinessId, JourneyContextWithNino, JourneyName, Mtditid, TaxYear}
import models.connector.{ApiResponse, IFSApiName, lossClaimReads}
import models.connector.api_1505.{CreateLossClaimRequestBody, CreateLossClaimSuccessResponse}
import models.connector.common.ReliefClaim
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhatDoYouWantToDoWithLoss}
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers.logger
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class ReliefClaimsService @Inject()(reliefClaimsConnector: ReliefClaimsConnector,
                                     repository: JourneyAnswersRepository,
                                     appConfig: AppConfig)
                                    (implicit ec: ExecutionContext) {

//  def cacheReliefClaims(ctx: JourneyContextWithNino,
//                        optProfitOrLoss: Option[ProfitOrLossJourneyAnswers])
//                       (implicit hc: HeaderCarrier): ApiResultT[Option[ProfitOrLossJourneyAnswers]] =
//    optProfitOrLoss match {
//      case Some(profitOrLoss) if profitOrLoss.whatDoYouWantToDoWithLoss.isEmpty =>
//        for {
//          claims <- reliefClaimsConnector.getAllReliefClaims(ctx.taxYear, ctx.businessId)
//          doWithLossAnswers = filterReliefClaims(claims, ctx.taxYear, ctx.businessId).map(claim => WhatDoYouWantToDoWithLoss(claim.reliefClaimed))
//          updatedProfitOrLoss = profitOrLoss.copy(whatDoYouWantToDoWithLoss = doWithLossAnswers)
//          _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(updatedProfitOrLoss))
//        } yield Some(updatedProfitOrLoss)
//      case Some(profitOrLoss) =>
//        EitherT.right(Future.successful(Some(profitOrLoss)))
//      case None =>
//        EitherT.right(Future.successful(None))
//    }

  def getAllReliefClaims(ctx: JourneyContextWithNino)
                        (implicit hc: HeaderCarrier): ApiResultT[List[ReliefClaim]] =
    for {
      reliefClaims <- reliefClaimsConnector.getAllReliefClaims(ctx.taxYear, ctx.businessId)
    } yield reliefClaims.filter(claim =>
      claim.isSelfEmploymentClaim &&
      claim.taxYearClaimedFor == ctx.taxYear.endYear.toString &&
      claim.incomeSourceId == ctx.businessId.value
    )

  private def filterReliefClaims(claims: List[ReliefClaim], taxYear: TaxYear, businessId: BusinessId): List[ReliefClaim] =
    claims
      .filter(_.isSelfEmploymentClaim)
      .filter(_.taxYearClaimedFor == taxYear.endYear.toString)
      .filter(_.incomeSourceId == businessId.value)

  def createReliefLossClaims(ctx: JourneyContextWithNino, answers: Seq[WhatDoYouWantToDoWithLoss])
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[List[CreateLossClaimSuccessResponse]] = {

    val context = appConfig.mkMetadata(IFSApiName.Api1505, appConfig.api1505Url(ctx.businessId))
    implicit val reads: HttpReads[ApiResponse[CreateLossClaimSuccessResponse]] = lossClaimReads[CreateLossClaimSuccessResponse]

    if (answers.isEmpty) {
      EitherT.right[ServiceError](Future.successful(Nil))
    } else {
      val responses = Future.sequence {
        answers.map { answer =>
          val body = CreateLossClaimRequestBody(
            incomeSourceId = ctx.businessId.value,
            reliefClaimed = WhatDoYouWantToDoWithLoss.toReliefClaimType(answer).toString,
            taxYear = ctx.taxYear.endYear.toString
          )

          reliefClaimsConnector.createReliefClaims(context, body)
        }
      }

      EitherT(responses.map(_.sequence))
    }
  }

}
