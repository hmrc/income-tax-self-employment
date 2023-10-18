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

package mocks

import models.mdtp.JourneyAnswers
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaFirstStubbing
import org.scalatestplus.mockito.MockitoSugar
import repositories.{JourneyAnswersRepository, SetResult}

import scala.concurrent.Future

trait MockJourneyAnswersRepository extends MockitoSugar {
  val mockJourneyAnswersRepository: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  object MockJourneyAnswersRepository {

    def get(id: String): ScalaFirstStubbing[Future[Option[JourneyAnswers]]] =
      when(mockJourneyAnswersRepository.get(id))

    def set(answers: JourneyAnswers): ScalaFirstStubbing[Future[SetResult]] =
      when(mockJourneyAnswersRepository.set(answers))
  }

}
