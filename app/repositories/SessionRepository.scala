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
import models.mdtp.JourneyState
import org.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.compoundIndex
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Clock, LocalDate}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (mongoComponent: MongoComponent, appConfig: AppConfig, clock: Clock)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[JourneyState](
      collectionName = "journey-state",
      mongoComponent = mongoComponent,
      domainFormat = JourneyState.mongoJourneyStateFormat,
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
    ) {

  private def filterBy(id: String): Bson = Filters.equal("_id", id)

  private def filterBy(nino: String, taxYear: Int, journey: String): Bson =
    Filters.and(
      Filters.equal("journeyStateData.businessId", nino),
      Filters.equal("journeyStateData.journey", journey),
      Filters.equal("journeyStateData.taxYear", taxYear)
    )

  def keepAlive(id: String): Future[Boolean] = keepAlive(() => filterBy(id))

  def keepAlive(businessId: String, journey: String, taxYear: Int): Future[Boolean] =
    keepAlive(() => filterBy(businessId, taxYear, journey))

  private def keepAlive(filterFn: () => Bson): Future[Boolean] =
    collection
      .updateOne(
        filter = filterFn(),
        update = Updates.set("lastUpdated", LocalDate.now(clock))
      )
      .toFuture()
      .map(_ => true)

  def get(id: String): Future[Option[JourneyState]] =
    keepAlive(id).flatMap { _ => find(() => filterBy(id)) }

  def get(businessId: String, journey: String, taxYear: Int): Future[Option[JourneyState]] =
    keepAlive(businessId, journey, taxYear).flatMap { _ => find(() => filterBy(businessId, taxYear, journey)) }

  def set(journeyState: JourneyState): Future[Boolean] = {
    val updatedJourneyState = journeyState copy (lastUpdated = LocalDate.now(clock))

    collection
      .replaceOne(
        filter = filterBy(updatedJourneyState.id),
        replacement = updatedJourneyState,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => true)
  }

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(filterBy(id))
      .toFuture()
      .map(_ => true)

  private def find(filterFn: () => Bson) = {
    collection
      .find(filterFn())
      .headOption()
  }

}
