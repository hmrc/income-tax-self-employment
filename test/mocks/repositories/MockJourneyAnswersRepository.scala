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

package mocks.repositories

import cats.data.EitherT
import cats.implicits._
import models.common._
import models.database.JourneyAnswers
import models.domain.{ApiResultT, Business}
import models.error.DownstreamError.SingleDownstreamError
import models.error.ServiceError
import models.frontend.TaskList
import org.scalamock.handlers.{CallHandler1, CallHandler2, CallHandler3}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, TestSuite}
import play.api.libs.json.{JsValue, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockJourneyAnswersRepository extends TestSuite with MockFactory {

  val mockJourneyAnswersRepository: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  object JourneyAnswersRepositoryMock {

    def getJourneyAnswers(context: JourneyContext)
                         (response: Future[Option[JourneyAnswers]]): CallHandler1[JourneyContext, Future[Option[JourneyAnswers]]] =
      (mockJourneyAnswersRepository.getJourneyAnswers(_: JourneyContext))
        .expects(context)
        .returning(response)

    def upsertJourneyAnswers(context: JourneyContext, data: JsValue)
                            (response: Future[Option[JsValue]]): CallHandler2[JourneyContext, JsValue, Future[Option[JsValue]]] =
      (mockJourneyAnswersRepository.upsertJourneyAnswers(_: JourneyContext, _: JsValue))
        .expects(context, data)
        .returning(response)

    def deleteJourneyAnswers(context: JourneyContext)
                            (wasDeleted: Boolean): CallHandler1[JourneyContext, Future[Boolean]] =
      (mockJourneyAnswersRepository.deleteJourneyAnswers(_: JourneyContext))
        .expects(context)
        .returning(Future.successful(wasDeleted))

    def get(context: JourneyContext)
           (returnValue: Option[JourneyAnswers]): CallHandler1[JourneyContext, ApiResultT[Option[JourneyAnswers]]] =
      (mockJourneyAnswersRepository.get(_: JourneyContext))
        .expects(context)
        .returning(EitherT.pure(returnValue))

    def getAll(taxYear: TaxYear, mtdId: Mtditid, businesses: List[Business])
              (returnValue: TaskList): CallHandler3[TaxYear, Mtditid, List[Business], ApiResultT[TaskList]] =
      (mockJourneyAnswersRepository.getAll(_: TaxYear, _: Mtditid, _: List[Business]))
        .expects(taxYear, mtdId, businesses)
        .returning(EitherT.pure(returnValue))

    def getAllError(taxYear: TaxYear, mtdId: Mtditid, businesses: List[Business])
                   (returnValue: SingleDownstreamError): CallHandler3[TaxYear, Mtditid, List[Business], ApiResultT[TaskList]] =
      (mockJourneyAnswersRepository.getAll(_: TaxYear, _: Mtditid, _: List[Business]))
        .expects(taxYear, mtdId, businesses)
        .returning(EitherT.leftT(returnValue))

    def getAnswers[A: Reads](ctx: JourneyContext)
                            (returnValue: Option[A]): CallHandler2[JourneyContext, Reads[A], ApiResultT[Option[A]]] =
      (mockJourneyAnswersRepository.getAnswers[A](_: JourneyContext)(_: Reads[A]))
        .expects(ctx, *)
        .returning(EitherT.pure(returnValue))

    def upsertAnswers(ctx: JourneyContext, newData: JsValue): CallHandler2[JourneyContext, JsValue, ApiResultT[Unit]] =
      (mockJourneyAnswersRepository.upsertAnswers(_: JourneyContext, _: JsValue))
        .expects(ctx, newData)
        .returning(EitherT.pure(()))

    def upsertAnswersFailure(ctx: JourneyContext, newData: JsValue)(error: ServiceError): CallHandler2[JourneyContext, JsValue, ApiResultT[Unit]] =
      (mockJourneyAnswersRepository.upsertAnswers(_: JourneyContext, _: JsValue))
        .expects(ctx, newData)
        .returning(EitherT.leftT(error))

    def setStatus(ctx: JourneyContext, status: JourneyStatus): CallHandler2[JourneyContext, JourneyStatus, ApiResultT[Unit]] =
      (mockJourneyAnswersRepository.setStatus(_: JourneyContext, _: JourneyStatus))
        .expects(ctx, status)
        .returning(EitherT.pure(()))

    def deleteOneOrMoreJourneys(ctx: JourneyContext, multiplePrefix: Option[String] = None): CallHandler2[JourneyContext, Option[String], ApiResultT[Unit]] =
      (mockJourneyAnswersRepository.deleteOneOrMoreJourneys(_: JourneyContext, _: Option[String]))
        .expects(ctx, multiplePrefix)
        .returning(EitherT.pure(()))

    def deleteOneOrMoreJourneysError(ctx: JourneyContext, multiplePrefix: Option[String] = None)
                                    (returnValue: ServiceError): CallHandler2[JourneyContext, Option[String], ApiResultT[Unit]] =
      (mockJourneyAnswersRepository.deleteOneOrMoreJourneys(_: JourneyContext, _: Option[String]))
        .expects(ctx, multiplePrefix)
        .returning(EitherT.leftT(returnValue))

  }

}
