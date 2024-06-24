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

package models.commonTaskList

import models.common.{Enumerable, WithName}
import play.api.libs.json.{Json, OWrites, Reads}

trait TaskStatus extends Enumerable.Implicits

object TaskStatus extends TaskStatus {

  case class Completed() extends WithName("completed") with TaskStatus
  object Completed {
    implicit val nonStrictReads: Reads[Completed] = Reads.pure(Completed())
    implicit val writes: OWrites[Completed]       = OWrites[Completed](_ => Json.obj())
  }

  case class InProgress() extends WithName("inProgress") with TaskStatus
  object InProgress {
    implicit val nonStrictReads: Reads[InProgress] = Reads.pure(InProgress())
    implicit val writes: OWrites[InProgress]       = OWrites[InProgress](_ => Json.obj())
  }

  case class NotStarted() extends WithName("notStarted") with TaskStatus
  object NotStarted {
    implicit val nonStrictReads: Reads[NotStarted] = Reads.pure(NotStarted())
    implicit val writes: OWrites[NotStarted]       = OWrites[NotStarted](_ => Json.obj())
  }

  case class CannotStartYet() extends WithName("cannotStartYet") with TaskStatus
  object CannotStartYet { // TODO 8772 this isn't in tailor-returns-FE. Need to let them know
    implicit val nonStrictReads: Reads[CannotStartYet] = Reads.pure(CannotStartYet())
    implicit val writes: OWrites[CannotStartYet]       = OWrites[CannotStartYet](_ => Json.obj())
  }

  case class CheckNow() extends WithName("checkNow") with TaskStatus
  object CheckNow {
    implicit val nonStrictReads: Reads[CheckNow] = Reads.pure(CheckNow())
    implicit val writes: OWrites[CheckNow]       = OWrites[CheckNow](_ => Json.obj())
  }

  val values: Seq[TaskStatus] = Seq(
    Completed(),
    InProgress(),
    NotStarted(),
    CannotStartYet(),
    CheckNow()
  )

  implicit val enumerable: Enumerable[TaskStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
