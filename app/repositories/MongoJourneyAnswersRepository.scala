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

import models.common.JourneyAnswersContext.JourneyContext
import models.common._
import models.database.JourneyAnswers
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
import org.mongodb.scala._
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

  def get(ctx: JourneyContext): Future[Option[JourneyAnswers]]

  def upsertData(ctx: JourneyContext, newData: JsValue): Future[UpdateResult]

  def updateStatus(ctx: JourneyContext, status: JourneyStatus): Future[UpdateResult]
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

  private def filterJourney(ctx: JourneyContext) = Filters.and(
    Filters.eq("mtditid", ctx.mtditid.value),
    Filters.eq("taxYear", ctx.taxYear.endYear),
    Filters.eq("businessId", ctx.businessId.value),
    Filters.eq("journey", ctx.journey.entryName)
  )

  // TODO remove
  def get(id: String): Future[Option[JourneyAnswers]] =
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(filterByConstraint("_id", id))
      .headOption()

  private def filterByConstraint(field: String, value: String): Bson = equal(field, value)

  def get(ctx: JourneyContext): Future[Option[JourneyAnswers]] = {
    val filter = filterJourney(ctx)
    collection
      .withReadPreference(ReadPreference.primaryPreferred())
      .find(filter)
      .headOption()
  }

  def upsertData(ctx: JourneyContext, newData: JsValue): Future[UpdateResult] = {
    val filter  = filterJourney(ctx)
    val bson    = BsonDocument(Json.stringify(newData))
    val update  = createUpsert(ctx)("data", bson)
    val options = new UpdateOptions().upsert(true)

    collection.updateOne(filter, update, options).toFuture()
  }

  def updateStatus(ctx: JourneyContext, status: JourneyStatus): Future[UpdateResult] = {
    val now    = Instant.now(clock)
    val filter = filterJourney(ctx)
    val update = Updates.combine(
      Updates.set("status", status.entryName),
      Updates.set("updatedAt", now)
    )
    val options = new UpdateOptions().upsert(false)
    collection.updateOne(filter, update, options).toFuture()
  }

  private def createUpsert(ctx: JourneyContext)(fieldName: String, value: BsonValue) = {
    val now      = Instant.now(clock)
    val expireAt = calculateExpireAt(now)

    Updates.combine(
      Updates.set(fieldName, value),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("mtditid", ctx.mtditid.value),
      Updates.setOnInsert("taxYear", ctx.taxYear.endYear),
      Updates.setOnInsert("businessId", ctx.businessId.value),
      Updates.setOnInsert("status", JourneyStatus.InProgress.entryName),
      Updates.setOnInsert("journey", ctx.journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }

}
