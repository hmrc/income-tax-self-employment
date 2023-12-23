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

import cats.implicits._
import models.common._
import models.database.JourneyAnswers
import models.domain.{Business, JourneyNameAndStatus, TradesJourneyStatuses}
import models.frontend.TaskList
import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Projections.{exclude, include}
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
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
  def getAll(taxYear: TaxYear, mtditid: Mtditid, businesses: List[Business]): Future[TaskList]
  def upsertAnswers(ctx: JourneyContext, newData: JsValue): Future[UpdateResult]
  def setStatus(ctx: JourneyContext, status: JourneyStatus): Future[UpdateResult]
  def testOnlyClearAllData(): Future[Unit]
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

  def testOnlyClearAllData(): Future[Unit] =
    collection
      .deleteMany(new org.bson.Document())
      .toFuture()
      .void

  private def filterJourney(ctx: JourneyContext) = Filters.and(
    Filters.eq("mtditid", ctx.mtditid.value),
    Filters.eq("taxYear", ctx.taxYear.endYear),
    Filters.eq("businessId", ctx.businessId.value),
    Filters.eq("journey", ctx.journey.entryName)
  )

  private def filterAllJourneys(taxYear: TaxYear, mtditid: Mtditid) = Filters.and(
    Filters.eq("mtditid", mtditid.value),
    Filters.eq("taxYear", taxYear.endYear)
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
      .withReadPreference(ReadPreference.primaryPreferred()) // TODO Why? Cannot we just use standard?
      .find(filter)
      .headOption()
  }

  def getAll(taxYear: TaxYear, mtditid: Mtditid, businesses: List[Business]): Future[TaskList] = {
    val filter     = filterAllJourneys(taxYear, mtditid)
    val projection = exclude("data")
    collection
      .find(filter)
      .projection(projection)
      .toFuture()
      .map { seqJourneyAnswers =>
        val groupedByBusinessId: Map[BusinessId, Seq[JourneyAnswers]] = seqJourneyAnswers.groupBy(_.businessId)
        val tradingDetailsStatus = groupedByBusinessId
          .get(BusinessId.tradeDetailsId)
          .flatMap(_.toList.headOption
            .map(a => JourneyNameAndStatus(a.journey, a.status)))

        val perBusinessStatuses = businesses.map { business =>
          val currentJourneys = groupedByBusinessId
            .get(BusinessId(business.businessId))
            .map(_.toList)
            .getOrElse(Nil)

          TradesJourneyStatuses(
            BusinessId(business.businessId),
            business.tradingName.map(TradingName(_)),
            currentJourneys.map(j => JourneyNameAndStatus(j.journey, j.status))
          )
        }

        TaskList(tradingDetailsStatus, perBusinessStatuses)
      }
  }

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): Future[UpdateResult] = {
    val filter  = filterJourney(ctx)
    val bson    = BsonDocument(Json.stringify(newData))
    val update  = createUpsert(ctx)("data", bson, JourneyStatus.NotStarted)
    val options = new UpdateOptions().upsert(true)

    collection.updateOne(filter, update, options).toFuture()
  }

  def setStatus(ctx: JourneyContext, status: JourneyStatus): Future[UpdateResult] = {
    val filter  = filterJourney(ctx)
    val update  = createUpsertStatus(ctx)(status)
    val options = new UpdateOptions().upsert(true)

    collection.updateOne(filter, update, options).toFuture()
  }

  private def createUpsert(ctx: JourneyContext)(fieldName: String, value: BsonValue, statusOnInsert: JourneyStatus) = {
    val now      = Instant.now(clock)
    val expireAt = calculateExpireAt(now)

    Updates.combine(
      Updates.set(fieldName, value),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("mtditid", ctx.mtditid.value),
      Updates.setOnInsert("taxYear", ctx.taxYear.endYear),
      Updates.setOnInsert("businessId", ctx.businessId.value),
      Updates.setOnInsert("status", statusOnInsert.entryName),
      Updates.setOnInsert("journey", ctx.journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }

  private def createUpsertStatus(ctx: JourneyContext)(status: JourneyStatus) = {
    val now      = Instant.now(clock)
    val expireAt = calculateExpireAt(now)

    Updates.combine(
      Updates.set("status", status.entryName),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("data", BsonDocument()),
      Updates.setOnInsert("mtditid", ctx.mtditid.value),
      Updates.setOnInsert("taxYear", ctx.taxYear.endYear),
      Updates.setOnInsert("businessId", ctx.businessId.value),
      Updates.setOnInsert("journey", ctx.journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }
}
