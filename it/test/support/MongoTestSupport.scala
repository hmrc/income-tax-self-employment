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

package support

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.IndexModel
import org.scalatest.Assertion
import org.scalatest.Assertions.fail
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

trait MongoTestSupport[A] {

  val repository: PlayMongoRepository[A]

  protected def indexWithField(fieldName: String): IndexModel => Boolean =
    _.getKeys match {
      case keys: BsonDocument => keys.containsKey(fieldName)
      case _                  => false // Could be a compound index
    }

  protected def indexByName(name: String): IndexModel => Boolean = _.getOptions.getName == name

  protected def checkIndex(selector: IndexModel => Boolean)(check: IndexModel => Assertion): Unit = {
    val indexes = repository.indexes
    indexes.find(selector) match {
      case Some(index) => check(index)
      case None        => fail(s"Required index does not exist")
    }
    ()
  }

}
