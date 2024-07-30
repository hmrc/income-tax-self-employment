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

package models.frontend.nics

import data.api1639.SuccessResponseAPI1639Data.class2NicsTrue
import models.database.nics.NICsStorageAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.OptionValues._

class NICsAnswersSpec extends AnyWordSpecLike {
  "fromApi1639" should {
    "return None when there are no class 2 NICs" in {
      val result = NICsAnswers.mkPriorData(None, None)
      assert(result.isEmpty)
    }

    "return class2 from the API" in {
      val result = NICsAnswers.mkPriorData(Some(class2NicsTrue), None).value
      assert(result === NICsAnswers(true))
    }

    "return class2 from the DB if does not exist in API" in {
      val result = NICsAnswers.mkPriorData(None, Some(NICsStorageAnswers(Some(false)))).value
      assert(result === NICsAnswers(false))
    }

    "ignore value from DB if it exist in the API" in {
      val result = NICsAnswers.mkPriorData(Some(class2NicsTrue), Some(NICsStorageAnswers(Some(false)))).value
      assert(result === NICsAnswers(true))
    }

  }
}
