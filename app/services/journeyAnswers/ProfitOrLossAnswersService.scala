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
import cats.implicits.catsSyntaxEitherId
import connectors.IFSConnector
import models.common.{JourneyContextWithNino, JourneyName}
import models.connector.api_1803
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.adjustments.ProfitOrLossJourneyAnswers
import play.api.libs.json.Json
import play.api.mvc.Results.NoContent
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

}

@Singleton
class ProfitOrLossAnswersServiceImpl @Inject() (connector: IFSConnector, repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends ProfitOrLossAnswersService {

  def saveProfitOrLoss(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _ <- createUpdateOrDeleteAnnualSummaries(ctx, answers)
      _ <- createUpdateOrDeleteBroughtForwardLoss
      _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield NoContent

  private def createUpdateOrDeleteAnnualSummaries(ctx: JourneyContextWithNino, answers: ProfitOrLossJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      annualSummaries <- EitherT[Future, ServiceError, api_1803.SuccessResponseSchema](connector.getAnnualSummaries(ctx))
      _ <- EitherT[Future, ServiceError, Unit](
        connector.createAmendSEAnnualSubmission(answers.toAnnualSummariesData(ctx, annualSummaries))
      ) // TODO delete if empty
    } yield NoContent

  private def createUpdateOrDeleteBroughtForwardLoss: ApiResultT[Unit] =
    // 1. Get API answers if they exist (1502)
    // 2. If API answers not found, and journey answers (CYA) are none, do nothing
    // 3. If API answers not found, and journey answers are some, create new 1500
    // 4. If API existing answers, and journey answers are some, amend and send 1501
    // 5. If API existing answers, and journey answers are none, delete 1504
    EitherT[Future, ServiceError, Unit](Future.successful(().asRight))
}
