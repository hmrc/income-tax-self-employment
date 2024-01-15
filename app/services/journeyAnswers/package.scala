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

package services

import cats.data.EitherT
import cats.implicits._
import models.database.JourneyAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import play.api.libs.json.Reads
import utils.EitherTOps._

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

package object journeyAnswers {

  def getPersistedAnswers[A: Reads](row: Option[JourneyAnswers])(implicit ec: ExecutionContext, ct: ClassTag[A]): ApiResultT[Option[A]] =
    row.traverse(getPersistedAnswers[A])

  def getPersistedAnswers[A: Reads](row: JourneyAnswers)(implicit ec: ExecutionContext, ct: ClassTag[A]): ApiResultT[A] =
    EitherT.fromEither[Future](row.validatedAs[A]).leftAs[ServiceError]

}
