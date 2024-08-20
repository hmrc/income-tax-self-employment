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

package models.frontend.nics

import cats.implicits.catsSyntaxOptionId
import models.connector.api_1639.SuccessResponseAPI1639
import models.database.nics.NICsStorageAnswers
import models.error.ServiceError
import play.api.libs.json.{Format, Json}

case class NICsAnswers(class2Answers: Option[NICsClass2Answers], class4Answers: Option[NICsClass4Answers])

object NICsAnswers {
  implicit val formats: Format[NICsAnswers] = Json.format[NICsAnswers]

  def invalidAnswersError(answers: NICsAnswers): ServiceError = ServiceError.ErrorFromUpstream(
    "\n---------------------\nNICsAnswers must contain only one of 'class2Answers' OR 'class4Answers'." +
      s"\nAnswers contained\nClass 2: ${answers.class2Answers}\nClass 4: ${answers.class4Answers}\n---------------------")

  def mkPriorClass2Data(maybeApiAnswers: Option[SuccessResponseAPI1639], maybeDbAnswers: Option[NICsStorageAnswers]): Option[NICsAnswers] = {
    val existingClass2Nics = for {
      answers     <- maybeApiAnswers
      nicsAnswers <- answers.class2Nics
      class2Nics  <- nicsAnswers.class2VoluntaryContributions
    } yield class2Nics

    val maybeNics = existingClass2Nics.map(value => NICsAnswers(class2Answers = NICsClass2Answers(value).some, None))
    maybeNics.orElse(maybeDbAnswers.flatMap(_.class2NICs.map(value => NICsAnswers(class2Answers = NICsClass2Answers(value).some, None))))
  }

}
