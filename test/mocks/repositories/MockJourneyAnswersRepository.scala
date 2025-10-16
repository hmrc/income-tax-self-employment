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
import models.frontend.TaskList
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.JsValue
import repositories.JourneyAnswersRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MockJourneyAnswersRepository {

  val mockInstance: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  def getJourneyAnswers(context: JourneyContext)
                       (response: Future[Option[JourneyAnswers]]): ScalaOngoingStubbing[Future[Option[JourneyAnswers]]] = {
    when(mockInstance.getJourneyAnswers(eqTo(context)))
      .thenReturn(response)
  }

  def upsertJourneyAnswers(context: JourneyContext, data: JsValue)
                          (response: Future[Option[JsValue]]): ScalaOngoingStubbing[Future[Option[JsValue]]] =
    when(mockInstance.upsertJourneyAnswers(eqTo(context), eqTo(data)))
      .thenReturn(response)

  def deleteJourneyAnswers(context: JourneyContext)
                          (wasDeleted: Boolean): ScalaOngoingStubbing[Future[Boolean]] =
    when(mockInstance.deleteJourneyAnswers(eqTo(context)))
      .thenReturn(Future.successful(wasDeleted))

  def get(context: JourneyContext)
         (returnValue: Option[JourneyAnswers]): ScalaOngoingStubbing[ApiResultT[Option[JourneyAnswers]]] =
    when(mockInstance.get(eqTo(context)))
      .thenReturn(EitherT.pure(returnValue))

  def getAll(taxYear: TaxYear, mtdId: Mtditid, businesses: List[Business])
            (returnValue: TaskList): ScalaOngoingStubbing[ApiResultT[TaskList]] =
    when(mockInstance.getAll(eqTo(taxYear), eqTo(mtdId), eqTo(businesses)))
      .thenReturn(EitherT.pure(returnValue))

  def getAllError(taxYear: TaxYear, mtdId: Mtditid, businesses: List[Business])
                 (returnValue: SingleDownstreamError): ScalaOngoingStubbing[ApiResultT[TaskList]] =
    when(mockInstance.getAll(eqTo(taxYear), eqTo(mtdId), eqTo(businesses)))
      .thenReturn(EitherT.leftT(returnValue))

  def getAnswers[A](ctx: JourneyContext)
                          (returnValue: Option[A]): ScalaOngoingStubbing[ApiResultT[Option[A]]] =
    when(mockInstance.getAnswers[A](eqTo(ctx))(any()))
      .thenReturn(EitherT.pure(returnValue))

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.upsertAnswers(eqTo(ctx), eqTo(newData)))
      .thenReturn(EitherT.pure(()))

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(mockInstance.setStatus(eqTo(ctx), eqTo(status)))
      .thenReturn(EitherT.pure(()))

  def deleteOneOrMoreJourneys(ctx: JourneyContext, multiplePrefix: Option[String] = None): ScalaOngoingStubbing[ApiResultT[Unit]] =
    when(
      mockInstance.deleteOneOrMoreJourneys(eqTo(ctx), eqTo(multiplePrefix)))
      .thenReturn(EitherT.pure(()))

}
