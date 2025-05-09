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

import cats.implicits._
import models.common.JourneyName._
import models.common._
import models.domain.ApiResultT
import models.frontend.abroad.SelfEmploymentAbroadAnswers
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait IndustrySectorsAndAbroadAnswersService {
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SelfEmploymentAbroadAnswers]]
  def persistAnswers(ctx: JourneyContextWithNino, answers: SelfEmploymentAbroadAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class IndustrySectorsAndAbroadAnswersServiceImpl @Inject() (repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends IndustrySectorsAndAbroadAnswersService {

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SelfEmploymentAbroadAnswers]] =
    for {
      row    <- repository.get(ctx.toJourneyContext(SelfEmploymentAbroad))
      result <- getPersistedAnswers[SelfEmploymentAbroadAnswers](row)
    } yield result

  def persistAnswers(ctx: JourneyContextWithNino, answers: SelfEmploymentAbroadAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    repository.upsertAnswers(ctx.toJourneyContext(SelfEmploymentAbroad), Json.toJson(answers))

}
