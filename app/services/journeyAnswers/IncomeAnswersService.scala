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
import models.common.{BusinessId, Mtditid, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.income.IncomeJourneyAnswers
import repositories.JourneyAnswersRepository

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

trait IncomeAnswersService {
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: IncomeJourneyAnswers): ApiResultT[Unit]
}

@nowarn
@Singleton
class IncomeAnswersServiceImpl @Inject() (repository: JourneyAnswersRepository)(implicit ec: ExecutionContext) extends IncomeAnswersService {

  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: IncomeJourneyAnswers): ApiResultT[Unit] =
    EitherT.right[ServiceError](Future.successful(()))
}
