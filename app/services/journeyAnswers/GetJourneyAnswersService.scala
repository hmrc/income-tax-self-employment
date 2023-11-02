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

import models.database.JourneyAnswers
import repositories.JourneyAnswersRepository
import services.journeyAnswers.GetJourneyAnswersResult.{JourneyAnswersFound, NoJourneyAnswersFound}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait GetJourneyAnswersResult

object GetJourneyAnswersResult {
  case class JourneyAnswersFound(answers: JourneyAnswers) extends GetJourneyAnswersResult
  case object NoJourneyAnswersFound                       extends GetJourneyAnswersResult
}

class GetJourneyAnswersService @Inject()(repository: JourneyAnswersRepository)(implicit ec: ExecutionContext) {

  def getJourneyAnswers(id: String): Future[GetJourneyAnswersResult] = {
    repository.get(id).map {
      case Some(answers) => JourneyAnswersFound(answers)
      case None          => NoJourneyAnswersFound
    }
  }

}
