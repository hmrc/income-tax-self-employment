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
import cats.implicits._
import connectors.IFSConnector
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.prepop.{AdjustmentsPrepopAnswers, IncomePrepopAnswers}
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps._

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait PrepopAnswersService {
  def getIncomeAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[IncomePrepopAnswers]
  def getAdjustmentsAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AdjustmentsPrepopAnswers]
}

@Singleton
class PrepopAnswersServiceImpl @Inject() (connector: IFSConnector)(implicit ec: ExecutionContext) extends PrepopAnswersService {

  def getIncomeAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[IncomePrepopAnswers] =
    EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError].map(IncomePrepopAnswers(_))

  def getAdjustmentsAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AdjustmentsPrepopAnswers] =
    EitherT(connector.getAnnualSummaries(ctx)).leftAs[ServiceError].map(AdjustmentsPrepopAnswers(_))
}
