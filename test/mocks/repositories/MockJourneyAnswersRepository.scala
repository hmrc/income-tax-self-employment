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
import data.CommonTestData
import models.common.{JourneyContext, JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.error.ServiceError
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar._
import play.api.libs.json._
import repositories.JourneyAnswersRepository

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MockJourneyAnswersRepository {
  testData: CommonTestData =>

  val mockInstance: JourneyAnswersRepository = mock[JourneyAnswersRepository]
  val now: Instant                           = Instant.now()

  def get[T](context: JourneyContext)(returnValue: Option[T]): Unit =
    when(mockInstance.get(context)).thenReturn(
      EitherT.pure[Future, ServiceError](
        returnValue.map(value =>
          JourneyAnswers(
            mtditid = testData.testMtdId,
            businessId = testData.testBusinessId,
            taxYear = testData.testCurrentTaxYear,
            journey = JourneyName.ProfitOrLoss,
            status = JourneyStatus.InProgress,
            data = Json.toJson(value).as[JsObject],
            expireAt = testData.now,
            createdAt = testData.now,
            updatedAt = testData.now
          )
        )
      )
    )

}
