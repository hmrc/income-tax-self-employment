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

package repositories

import models.common._
import models.database.JourneyAnswers
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonDocument, _}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala.{ReadPreference, _}
import play.api.libs.json.{JsValue, Json}
import repositories.ExpireAtCalculator.calculateExpireAt
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait JourneyAnswersRepository {
  def get(id: String): Future[Option[JourneyAnswers]]

  def get(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName): Future[Option[JourneyAnswers]]

  def upsertData(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName, newData: JsValue): Future[UpdateResult]

  def updateStatus(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName, status: JourneyStatus): Future[UpdateResult]
}

@Singleton
class MongoJourneyAnswersRepository @Inject() (mongo: MongoComponent, clock: Clock)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[JourneyAnswers](
      collectionName = "journey-answers",
      mongoComponent = mongo,
      domainFormat = JourneyAnswers.formats,
      replaceIndexes = true,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("expireAt"),
          IndexOptions()
            .name("expireAt")
            .expireAfter(0, TimeUnit.SECONDS)
        ),
        IndexModel(
          Indexes.ascending("mtditid", "taxYear", "businessId", "journey"),
          IndexOptions().name("mtditid_taxYear_businessId_journey")
        ),
        IndexModel(
          Indexes.ascending("mtditid", "taxYear"),
          IndexOptions().name("mtditid_taxYear")
        )
      )
    )
    with JourneyAnswersRepository {

  private def filterJourney(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName) = Filters.and(
    Filters.eq("mtditid", mtditid.value),
    Filters.eq("taxYear", taxYear.endYear),
    Filters.eq("businessId", businessId.value),
    Filters.eq("journey", journey.entryName)
  )

  // TODO remove
  def get(id: String): Future[Option[JourneyAnswers]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(filterByConstraint("_id", id))
      .headOption()

  private def filterByConstraint(field: String, value: String): Bson = equal(field, value)

  def get(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName): Future[Option[JourneyAnswers]] = {
    val filter = filterJourney(mtditid, taxYear, businessId, journey)
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(filter)
      .headOption()
  }

  def upsertData(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName, newData: JsValue): Future[UpdateResult] = {
    val filter  = filterJourney(mtditid, taxYear, businessId, journey)
    val bson    = BsonDocument(Json.stringify(newData))
    val update  = createUpsert(mtditid, taxYear, businessId, journey)("data", bson)
    val options = new UpdateOptions().upsert(true)

    collection.updateOne(filter, update, options).toFuture()
  }

  def updateStatus(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName, status: JourneyStatus): Future[UpdateResult] = {
    val now    = Instant.now(clock)
    val filter = filterJourney(mtditid, taxYear, businessId, journey)
    val update = Updates.combine(
      Updates.set("status", status.entryName),
      Updates.set("updatedAt", now)
    )
    val options = new UpdateOptions().upsert(false)
    collection.updateOne(filter, update, options).toFuture()
  }

  private def createUpsert(mtditid: Mtditid, taxYear: TaxYear, businessId: BusinessId, journey: JourneyName)(fieldName: String, value: BsonValue) = {
    val now      = Instant.now(clock)
    val expireAt = calculateExpireAt(now)

    Updates.combine(
      Updates.set(fieldName, value),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("mtditid", mtditid.value),
      Updates.setOnInsert("taxYear", taxYear.endYear),
      Updates.setOnInsert("businessId", businessId.value),
      Updates.setOnInsert("status", JourneyStatus.InProgress.entryName),
      Updates.setOnInsert("journey", journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }

}
