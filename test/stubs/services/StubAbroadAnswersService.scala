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

package stubs.services

import cats.data.EitherT
import models.common.JourneyContextWithNino
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.abroad.SelfEmploymentAbroadAnswers
import services.journeyAnswers.AbroadAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubAbroadAnswersService(saveAnswersRes: Either[ServiceError, Unit] = Right(()),
                                    getAnswersRes: Either[ServiceError, Option[SelfEmploymentAbroadAnswers]] = Right(None))
    extends AbroadAnswersService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SelfEmploymentAbroadAnswers]] =
    EitherT.fromEither[Future](getAnswersRes)

  def persistAnswers(ctx: JourneyContextWithNino, answers: SelfEmploymentAbroadAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither[Future](saveAnswersRes)
}
