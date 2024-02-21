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
import models.connector.Api1802AnnualAllowancesBuilder
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import play.api.libs.json.Writes
import services.journeyAnswers.CapitalAllowancesAnswersService
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubCapitalAllowancesAnswersAnswersService(saveAnswers: ApiResultT[Unit] = serviceUnitT,
                                                      persistCapitalAllowancesTailoring: ApiResultT[Unit] = serviceUnitT,
                                                      getCapitalAllowancesTailoring: Either[ServiceError, Option[CapitalAllowancesTailoringAnswers]] =
                                                        Right(None))
    extends CapitalAllowancesAnswersService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    persistCapitalAllowancesTailoring

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] = persistCapitalAllowancesTailoring

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    EitherT.fromEither[Future](getCapitalAllowancesTailoring)
}
