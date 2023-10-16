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

package services.persistedUserAnswers

import models.mdtp.PersistedUserAnswers
import repositories.PersistedUserAnswersRepository
import services.persistedUserAnswers.GetPersistedUserAnswersResult.{NoPersistedUserAnswersFound, PersistedUserAnswersFound}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait GetPersistedUserAnswersResult

object GetPersistedUserAnswersResult {
  case class PersistedUserAnswersFound(answers: PersistedUserAnswers) extends GetPersistedUserAnswersResult
  case object NoPersistedUserAnswersFound                             extends GetPersistedUserAnswersResult
}

class GetPersistedUserAnswersService @Inject() (repository: PersistedUserAnswersRepository)(implicit ec: ExecutionContext) {

  def getPersistedUserAnswers(id: String): Future[GetPersistedUserAnswersResult] = {
    repository.get(id).map {
      case Some(answers) => PersistedUserAnswersFound(answers)
      case None          => NoPersistedUserAnswersFound
    }
  }

}
