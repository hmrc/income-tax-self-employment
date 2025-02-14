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
import models.common.{JourneyContext, JourneyStatus, Mtditid, TaxYear}
import models.database.JourneyAnswers
import models.domain.{ApiResultT, Business}
import models.frontend.TaskList
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.mockito.stubbing.ScalaOngoingStubbing
import org.scalatestplus.mockito.MockitoSugar._
import play.api.libs.json.{JsValue, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.ExecutionContext.Implicits.global

object MockJourneyAnswersRepository {

  val mockInstance: JourneyAnswersRepository = mock[JourneyAnswersRepository]

  def get(context: JourneyContext)
         (returnValue: Option[JourneyAnswers]): ScalaOngoingStubbing[ApiResultT[Option[JourneyAnswers]]] =
    when(mockInstance.get(context)).thenReturn(EitherT.pure(returnValue))

  def getAll(taxYear: TaxYear,
             mtdId: Mtditid,
             businesses: List[Business])
            (returnValue: TaskList): ApiResultT[TaskList] =
    when(mockInstance.getAll(taxYear, mtdId, businesses)).thenReturn(EitherT.pure(returnValue))

  def getAnswers[A: Reads](ctx: JourneyContext)
                   (returnValue: Option[A]): ApiResultT[Option[A]] =
    when(mockInstance.getAnswers[A](ctx)(any(), any())).thenReturn(EitherT.pure(returnValue))

  def upsertAnswers(ctx: JourneyContext,
                    newData: JsValue): ApiResultT[Unit] =
    when(mockInstance.upsertAnswers(ctx, newData)).thenReturn(EitherT.pure(()))

  def setStatus(ctx: JourneyContext,
                status: JourneyStatus): ApiResultT[Unit] =
    when(mockInstance.setStatus(ctx, status)).thenReturn(EitherT.pure(()))

  def deleteOneOrMoreJourneys(ctx: JourneyContext,
                              multiplePrefix: Option[String] = None): ApiResultT[Unit] =
    when(mockInstance.deleteOneOrMoreJourneys(ctx, multiplePrefix)).thenReturn(EitherT.pure(()))

}
