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
import scala.reflect.ClassTag

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

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    lastUpsertedAnswer = Some(newData)
    EitherT.fromEither[Future](upsertDataField)
  }

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.fromEither[Future](upsertStatusField)

  def testOnlyClearAllData(): ApiResultT[Unit] = ???

  def deleteOneOrMoreJourneys(ctx: models.common.JourneyContext, multiplePrefix: Option[String]): ApiResultT[Unit] =
    EitherT.fromEither[Future](deleteOneOrMoreJourneys)

  def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]] =
    EitherT.rightT[Future, ServiceError](getAnswer)

  def getAll(taxYear: TaxYear, mtditid: Mtditid, businesses: List[Business]): ApiResultT[TaskList] =
    EitherT.fromEither[Future](getAllResult)

  def getAnswers[A: Reads](ctx: JourneyContext)(implicit ct: ClassTag[A]): ApiResultT[Option[A]] =
    EitherT.fromEither(getAnswers.map(_.map(data => jsonAs[A](data.as[JsObject]).value)))
}
