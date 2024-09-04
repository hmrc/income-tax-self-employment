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
      // TODO get and send data for API 1500
      maybeAnnualSummaries <- getExistingAnnualSummaries(ctx)
      _ <- EitherT[Future, ServiceError, Unit](connector.createAmendSEAnnualSubmission(answers.toAnnualSummariesData(ctx, maybeAnnualSummaries))) // TODO delete if empty?
      _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.ProfitOrLoss), Json.toJson(answers.toDbAnswers))
    } yield NoContent

  private def getExistingAnnualSummaries(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[api_1803.SuccessResponseSchema]] = {
    val result = connector.getAnnualSummaries(JourneyContextWithNino(ctx.taxYear, ctx.businessId, ctx.mtditid, ctx.nino)).map {
      case Right(annualSummaries) => Right(Some(annualSummaries))
      case Left(_)                => Right(None) // TODO return non not found errors as Left
    }
    EitherT[Future, ServiceError, Option[api_1803.SuccessResponseSchema]](result)
  }
}
