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

package helpers

import models.common.JourneyName
import models.common.JourneyName.TravelExpenses
import models.common.JourneyStatus.InProgress
import models.database.JourneyAnswers
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.DeleteResult
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import repositories.JourneyAnswersRepository
import testdata.CommonTestData

import java.time.temporal.ChronoUnit

trait JourneyAnswersHelper {
  self: CommonTestData with DefaultAwaitTimeout =>

  val mongo: JourneyAnswersRepository

  val filter: Bson = Filters.and(
    Filters.equal("mtditid", testMtdItId.value),
    Filters.equal("businessId", testBusinessId.value),
    Filters.equal("taxYear", testTaxYear.endYear)
  )

  val testBaseJourneyAnswers: JourneyAnswers = JourneyAnswers(
    mtditid = testMtdItId,
    businessId = testBusinessId,
    taxYear = testTaxYear,
    journey = TravelExpenses,
    status = InProgress,
    data = Json.obj(),
    expireAt = testInstant.plus(1, ChronoUnit.MINUTES),
    createdAt = testInstant,
    updatedAt = testInstant
  )

  object DbHelper {

    def insertMany(sections: (JourneyName, JsValue)*): Unit = {
      val answers = sections.map { case (section, json) =>
        testBaseJourneyAnswers.copy(journey = section, data = json.as[JsObject])
      }
      await(mongo.collection.insertMany(answers).toFuture())
    }

    def insertJson(section: JourneyName, data: JsValue): Unit =
      insertMany(section -> data)

    def insertOne[T](section: JourneyName, data: T)(implicit format: Format[T]): Unit =
      insertMany(section -> Json.toJson(data))

    def getJson(section: JourneyName): Option[JsValue] = {
      val optAnswers = await(mongo.collection.find(filter).toFuture()).find(_.journey == section)
      optAnswers.flatMap(answers => answers.data.asOpt[JsValue])
    }

    def get[T](section: JourneyName)(implicit format: Format[T]): Option[T] = {
      val optAnswers = await(mongo.collection.find(filter).toFuture()).find(_.journey == section)
      optAnswers.flatMap(answers => answers.data.validate[T].asOpt)
    }

    def teardown: DeleteResult =
      await(mongo.collection.deleteMany(filter).toFuture())
  }

}
