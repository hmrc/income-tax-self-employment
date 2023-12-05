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

package stubs.repositories

import com.mongodb.client.result.UpdateResult
import models.common._
import models.database.JourneyAnswers
import play.api.libs.json.JsValue
import repositories.JourneyAnswersRepository

import scala.concurrent.Future

case class StubJourneyAnswersRepository(
    getAnswer: Option[JourneyAnswers] = None,
    upsertDateField: Future[UpdateResult] = Future.successful(updatedOne),
    upsertStatusField: Future[UpdateResult] = Future.successful(updatedOne)
) extends JourneyAnswersRepository {

  def get(id: String): Future[Option[JourneyAnswers]] = Future.successful(getAnswer)

  def get(ctx: JourneyAnswersContext.JourneyContext): Future[Option[JourneyAnswers]] =
    Future.successful(getAnswer)

  def upsertData(ctx: JourneyAnswersContext.JourneyContext, newData: JsValue): Future[UpdateResult] =
    upsertDateField

  def updateStatus(ctx: JourneyAnswersContext.JourneyContext, status: JourneyStatus): Future[UpdateResult] =
    upsertStatusField

}
