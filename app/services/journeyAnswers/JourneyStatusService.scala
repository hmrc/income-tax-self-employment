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
import connectors.SelfEmploymentConnector
import models.common._
import models.connector.api_1171
import models.domain.{ApiResultT, Business}
import models.error.ServiceError
import models.frontend.TaskList
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait JourneyStatusService {
  def set(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit]
  def get(ctx: JourneyContext): ApiResultT[JourneyStatus]
  def getTaskList(taxYear: TaxYear, mtditid: Mtditid, nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[TaskList]
}

@Singleton
class JourneyStatusServiceImpl @Inject() (businessConnector: SelfEmploymentConnector, repository: JourneyAnswersRepository)(implicit
    ec: ExecutionContext)
    extends JourneyStatusService {

  def set(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](repository.setStatus(ctx, status)).void

  def get(ctx: JourneyContext): ApiResultT[JourneyStatus] =
    for {
      answer <- EitherT.right(repository.get(ctx))
      status = answer.map(_.status).getOrElse(JourneyStatus.CheckOurRecords)
    } yield status

  def getTaskList(taxYear: TaxYear, mtditid: Mtditid, nino: Nino)(implicit hc: HeaderCarrier): ApiResultT[TaskList] = {
    def getBusinesses(businessesResp: api_1171.SuccessResponseSchema) =
      businessesResp.taxPayerDisplayResponse.businessData.getOrElse(Nil).map { details =>
        Business.mkBusiness(details, businessesResp.taxPayerDisplayResponse.yearOfMigration)
      }

    for {
      businessesResp <- EitherT(businessConnector.getBusinesses(IdType.Nino, nino.value))
      businesses = getBusinesses(businessesResp)
      taskList <- EitherT.right(repository.getAll(taxYear, mtditid, businesses))
    } yield taskList
  }
}
