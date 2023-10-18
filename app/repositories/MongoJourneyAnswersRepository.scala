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
import models.mdtp.JourneyAnswers
import org.mongodb.scala.ReadPreference
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// Awaiting guidelines on how we are going to calculate the TTL. Default TTL implemented for time-being.
@Singleton
class MongoJourneyAnswersRepository @Inject()(mongo: MongoComponent, appConfig: AppConfig, clock: Clock)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[JourneyAnswers](
      collectionName = "journey-answers",
      mongoComponent = mongo,
      domainFormat = JourneyAnswers.formats,
      replaceIndexes = true,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("journeyAnswersTTL")
            .expireAfter(appConfig.cacheTtl, TimeUnit.DAYS)
        )
      )
    )
    with JourneyAnswersRepository {

  /*
   * Do we really want to keepAlive upon access of the journey answers?
   */
  override def get(id: String): Future[Option[JourneyAnswers]] = {
    keepAlive(id).flatMap { _ =>
      collection
        .withReadPreference(ReadPreference.primaryPreferred())
        .find(filterByConstraint("_id", id))
        .headOption()
    }
  }

  override def set(answers: JourneyAnswers): Future[SetResult] = {
    val updatedAnswers = answers.copy(lastUpdated = Instant.now(clock))
    collection
      .replaceOne(
        filter = filterByConstraint("_id", updatedAnswers.id),
        replacement = updatedAnswers,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map { mongoResult =>
        if (mongoResult.getMatchedCount > 0) {
          SetResult.JourneyAnswersUpdated
        } else {
          SetResult.JourneyAnswersCreated
        }
      }
  }

  private def keepAlive(id: String): Future[Unit] =
    collection
      .updateOne(
        filter = filterByConstraint("_id", id),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => ())

  private def filterByConstraint(field: String, value: String): Bson = equal(field, value)

}
