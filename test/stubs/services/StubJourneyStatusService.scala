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
import models.common.{JourneyContext, JourneyStatus, Mtditid, Nino, TaxYear}
import models.domain.ApiResultT
import models.frontend.TaskList
import services.journeyAnswers.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier
import cats.implicits._
import models.error.ServiceError

import scala.concurrent.{ExecutionContext, Future}

case class StubJourneyStatusService(
    setRes: Either[ServiceError, Unit] = ().asRight[ServiceError],
    getRes: Either[ServiceError, JourneyStatus] = JourneyStatus.CheckOurRecords.asRight[ServiceError],
    getTaskListRes: Either[ServiceError, TaskList] = TaskList.empty.asRight[ServiceError]
) extends JourneyStatusService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def set(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] = EitherT.fromEither[Future](setRes)

  def get(ctx: JourneyContext): ApiResultT[JourneyStatus] = EitherT.fromEither[Future](getRes)

  def getTaskList(taxYear: TaxYear, mtditid: Mtditid, nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[TaskList] =
    EitherT.fromEither[Future](getTaskListRes)
}
