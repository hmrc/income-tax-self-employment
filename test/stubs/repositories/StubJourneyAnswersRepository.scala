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

import cats.data.EitherT
import cats.implicits._
import com.google.errorprone.annotations.DoNotCall
import models.common._
import models.database.JourneyAnswers
import models.domain.{ApiResultT, Business}
import models.error.ServiceError
import models.frontend.TaskList
import models.jsonAs
import org.scalatest.EitherValues._
import play.api.libs.json.{JsObject, JsValue, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}

case class StubJourneyAnswersRepository(
    getAnswer: Option[JourneyAnswers] = None,
    getAnswers: Either[ServiceError, Option[JsValue]] = Right(None),
    upsertDataField: Either[ServiceError, Unit] = Right(()),
    upsertStatusField: Either[ServiceError, Unit] = Right(()),
    getAllResult: Either[ServiceError, TaskList] = Right(TaskList.empty),
    deleteOneOrMoreJourneys: Either[ServiceError, Unit] = Right(())
) extends JourneyAnswersRepository {
  implicit val ec: ExecutionContext       = ExecutionContext.global
  var lastUpsertedAnswer: Option[JsValue] = None

  @DoNotCall("Only implemented to satisfy JourneyAnswersTrait. StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead")
  def getJourneyAnswers(ctx: JourneyContext): Future[Option[JourneyAnswers]] = ???

  @DoNotCall("Only implemented to satisfy JourneyAnswersTrait. StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead")
  def upsertJourneyAnswers(ctx: JourneyContext, answerJson: JsValue): Future[Option[JsValue]] = ???

  @DoNotCall("Only implemented to satisfy JourneyAnswersTrait. StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead")
  def deleteJourneyAnswers(ctx: JourneyContext): Future[Boolean] = ???

  @deprecated("StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", "2024-01-01")
  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    lastUpsertedAnswer = Some(newData)
    EitherT.fromEither[Future](upsertDataField)
  }

  @deprecated(message = "StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", since = "2024-01-01")
  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.fromEither[Future](upsertStatusField)

  @DoNotCall("Only implemented to satisfy JourneyAnswersTrait. StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead")
  def testOnlyClearAllData(): ApiResultT[Unit] = ???

  @deprecated(message = "StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", since = "2024-01-01")
  def deleteOneOrMoreJourneys(ctx: models.common.JourneyContext, multiplePrefix: Option[String]): ApiResultT[Unit] = {
    lastUpsertedAnswer = None
    EitherT.fromEither[Future](deleteOneOrMoreJourneys)
  }

  @deprecated(message = "StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", since = "2024-01-01")
  def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]] =
    EitherT.rightT[Future, ServiceError](getAnswer)

  @deprecated(message = "StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", since = "2024-01-01")
  def getAll(taxYear: TaxYear, mtditid: Mtditid, businesses: List[Business]): ApiResultT[TaskList] =
    EitherT.fromEither[Future](getAllResult)

  @deprecated(message = "StubJourneyAnswersRepository is deprecated. Use MockJourneyAnswersRepository instead", since = "2024-01-01")
  def getAnswers[A: Reads](ctx: JourneyContext): ApiResultT[Option[A]] =
    EitherT.fromEither(getAnswers.map(_.map(data => jsonAs[A](data.as[JsObject]).value)))
}
