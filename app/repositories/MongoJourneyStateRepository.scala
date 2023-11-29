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

import config.AppConfig
import models.database.JourneyState
import org.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.compoundIndex
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Clock, LocalDate}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait JourneyStateRepository {
  def get(businessId: String, taxYear: Int): Future[Seq[JourneyState]]
  def get(businessId: String, taxYear: Int, journey: String): Future[Option[JourneyState]]
  def set(journeyState: JourneyState): Future[Unit]
}

@Singleton
class MongoJourneyStateRepository @Inject() (mongoComponent: MongoComponent, appConfig: AppConfig, clock: Clock)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[JourneyState](
      collectionName = "journey-state",
      mongoComponent = mongoComponent,
      domainFormat = JourneyState.mongoJourneyStateFormat,
      replaceIndexes = true,
      indexes = Seq(
        IndexModel(
          compoundIndex(
            Indexes.ascending("journeyStateData.businessId"),
            Indexes.ascending("journeyStateData.journey"),
            Indexes.ascending("journeyStateData.taxYear")
          ),
          IndexOptions()
            .name("businessIdJourneyTaxYear")
            .unique(true)
        ),
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("journeyStateTTL")
            .expireAfter(appConfig.cacheTtl, TimeUnit.DAYS)
        )
      )
    )
    with JourneyStateRepository {

  /*
   * Do we really want to keepAlive upon access of the journey state?
   */
  override def get(businessId: String, taxYear: Int): Future[Seq[JourneyState]] = {
    val queryFilters = buildFilters(businessId = Some(businessId), taxYear = Some(taxYear))

    keepAlive(queryFilters)
      .flatMap(_ =>
        collection
          .find(queryFilters)
          .toFuture())
  }

  override def get(businessId: String, taxYear: Int, journey: String): Future[Option[JourneyState]] = {
    val queryFilters = buildFilters(businessId = Some(businessId), journey = Some(journey), taxYear = Some(taxYear))

    keepAlive(queryFilters)
      .flatMap(_ =>
        collection
          .find(queryFilters)
          .headOption())
  }

  private def keepAlive(filter: Bson): Future[Unit] =
    collection
      .updateOne(
        filter = filter,
        update = Updates.set("lastUpdated", LocalDate.now(clock))
      )
      .toFuture()
      .map(_ => ())

  override def set(journeyState: JourneyState): Future[Unit] = {
    val updatedJourneyState = journeyState.copy(lastUpdated = LocalDate.now(clock))
    collection
      .replaceOne(
        filter = buildFilters(id = Some(updatedJourneyState.id)),
        replacement = updatedJourneyState,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())
  }

  private def buildFilters(id: Option[String] = None,
                           businessId: Option[String] = None,
                           journey: Option[String] = None,
                           taxYear: Option[Int] = None): Bson = {
    val filters = Seq(
      id.map(Filters.equal("_id", _)),
      businessId.map(Filters.equal("journeyStateData.businessId", _)),
      journey.map(Filters.equal("journeyStateData.journey", _)),
      taxYear.map(Filters.equal("journeyStateData.taxYear", _))
    ).flatten

    Filters.and(filters: _*)
  }

}
