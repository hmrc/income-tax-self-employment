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

package services

import cats.data.EitherT
import models.common.{BusinessId, JourneyName, Mtditid, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import play.api.Logging
import play.api.libs.json.Writes
import repositories.JourneyAnswersRepository

import javax.inject.{Inject, Singleton}
import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

trait JourneyService {
  def getAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName): ApiResultT[A]
  def setAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, data: A): ApiResultT[Unit]
}

@nowarn
@Singleton
class JourneyServiceImpl @Inject() (repository: JourneyAnswersRepository)(implicit ec: ExecutionContext) extends JourneyService with Logging {

  def getAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName): ApiResultT[A] =
    ??? // TODO Implement when doing 'prior data' fetch

  def setAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, data: A): ApiResultT[Unit] = {
    EitherT(
      Future.successful(Right(): Either[ServiceError, Unit])
    ) // TODO SASS-6340
  }
}
