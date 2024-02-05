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

package stubs.services

import cats.data.EitherT
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import play.api.libs.json.Writes
import services.journeyAnswers.CapitalAllowancesAnswersService
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubCapitalAllowancesAnswersAnswersService(getCapitalAllowancesTailoring: Either[ServiceError, Option[CapitalAllowancesTailoringAnswers]] =
                                                        Right(None),
                                                      persistCapitalAllowancesTailoring: ApiResultT[Unit] = serviceUnitT)
    extends CapitalAllowancesAnswersService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    EitherT.fromEither[Future](getCapitalAllowancesTailoring)

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] = persistCapitalAllowancesTailoring
}
