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
import cats.implicits.toFunctorOps
import connectors.SelfEmploymentConnector
import models.common._
import models.connector.api_1638.RequestSchemaAPI1638
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.nics.NICsAnswers
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait NICsAnswersService {
  def saveAnswers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class NICsAnswersServiceImpl @Inject() (connector: SelfEmploymentConnector)(implicit ec: ExecutionContext) extends NICsAnswersService {

  def saveAnswers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      maybeClass2Nics <- getNicsRequestBody(ctx, answers)
      _               <- upsertOrDeleteData(maybeClass2Nics, ctx)
    } yield ()

  private def upsertOrDeleteData(maybeClass2Nics: Option[RequestSchemaAPI1638], ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    maybeClass2Nics match {
      case Some(data) => connector.upsertDisclosuresSubmission(ctx, data).void
      case None       => connector.deleteDisclosuresSubmission(ctx)
    }

  private def getNicsRequestBody(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Option[RequestSchemaAPI1638]] =
    connector
      .getDisclosuresSubmission(ctx)
      .map(RequestSchemaAPI1638.mkRequestBody(answers, _))
}
