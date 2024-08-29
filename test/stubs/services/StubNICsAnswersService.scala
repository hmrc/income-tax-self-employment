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
import models.frontend.nics.{NICsAnswers, NICsClass2Answers, NICsClass4Answers}
import services.journeyAnswers.NICsAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StubNICsAnswersService(getAnswersRes: Either[ServiceError, Option[NICsAnswers]] = Right(None)) extends NICsAnswersService {

  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](())

  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](())

  def saveClass4MultipleBusinesses(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](())

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] =
    EitherT.fromEither[Future](getAnswersRes)
}
