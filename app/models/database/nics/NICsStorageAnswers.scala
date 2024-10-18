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

package models.database.nics

import models.frontend.nics.NICsClass2Answers
import play.api.libs.json.{Json, OFormat}

/** The API does not support false. We store only false here.
  */
final case class NICsStorageAnswers(class2NICs: Option[Boolean], journeyIsYesButNoneAreExempt: Option[Boolean])

object NICsStorageAnswers {
  implicit val format: OFormat[NICsStorageAnswers] = Json.format[NICsStorageAnswers]

  /** We send class2=true to API, so we don't store it. We don't send false, so we must store it based on our rule to store everything not sent
    * downstream
    */
  def fromJourneyAnswers(answers: NICsClass2Answers): NICsStorageAnswers =
    if (answers.class2NICs) NICsStorageAnswers(None, None) else NICsStorageAnswers(Some(false), None)

  val journeyIsYesButNoneAreExemptStorageAnswers = NICsStorageAnswers(None, Some(true))
}
