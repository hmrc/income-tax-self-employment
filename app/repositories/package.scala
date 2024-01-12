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

import cats.data.EitherT
import cats.implicits._
import models.common.JourneyContext
import models.domain.ApiResultT
import models.error.ServiceError
import org.mongodb.scala.result.UpdateResult
import play.api.Logger
import play.api.libs.json.{JsNumber, Reads, Writes}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

package object repositories {
  implicit val instantWrites: Writes[Instant] = Writes[Instant] { instant =>
    JsNumber(instant.toEpochMilli)
  }

  implicit val instantReads: Reads[Instant] = Reads[Instant] {
    _.validate[Long].map(Instant.ofEpochMilli)
  }

  def handleUpdateExactlyOne(ctx: JourneyContext, result: Future[UpdateResult])(implicit logger: Logger, ec: ExecutionContext): ApiResultT[Unit] =
    EitherT
      .rightT[Future, ServiceError](result.map { r =>
        if (r.getModifiedCount != 1) {
          logger.warn(s"Modified count was not 1, was ${r.getModifiedCount} for ctx=${ctx.toString}") // TODO Add Pager Duty
        }
        r
      })
      .void
}
