/*
 * Copyright 2025 HM Revenue & Customs
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

package services.answers

import jakarta.inject.Inject
import models.common.{JourneyContextWithNino, JourneyName}
import play.api.libs.json.{Format, Json, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}

class AnswerService @Inject() (repo: JourneyAnswersRepository)(implicit ec: ExecutionContext) {

  def getJourneyAnswers[T](ctx: JourneyContextWithNino, journey: JourneyName)(implicit reads: Reads[T]): Future[Option[T]] =
    for {
      optSection <- repo.getJourneyAnswers(ctx.toJourneyContext(journey))
      optValidatedJson = optSection.flatMap(_.data.validate[T].asOpt)
    } yield optValidatedJson

  def upsertJourneyAnswers[T](ctx: JourneyContextWithNino, journey: JourneyName, data: T)(implicit writes: Format[T]): Future[Option[T]] =
    for {
      optNewData <- repo.upsertJourneyAnswers(ctx.toJourneyContext(journey), Json.toJson(data))
      optValidatedJson = optNewData.flatMap(_.validate[T].asOpt)
    } yield optValidatedJson

  def deleteJourneyAnswers(ctx: JourneyContextWithNino, journey: JourneyName): Future[Boolean] =
    repo.deleteJourneyAnswers(ctx.toJourneyContext(journey), journey)

}
