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
import org.scalatest.OptionValues._
import org.scalatest.wordspec.AnyWordSpecLike

class NICsAnswersSpec extends AnyWordSpecLike {

  "mkPriorClass2Data fromApi1639" should {
    "return None when there are no class 2 NICs" in {
      val result = NICsAnswers.mkPriorClass2Data(None, None)
      assert(result.isEmpty)
    }

    "return class2 from the API" in {
      val result = NICsAnswers.mkPriorClass2Data(Some(class2NicsTrue), None).value.class2Answers
      assert(result === Some(NICsClass2Answers(true)))
    }

    "return class2 from the DB if does not exist in API" in {
      val result = NICsAnswers.mkPriorClass2Data(None, Some(NICsStorageAnswers(Some(false)))).value.class2Answers
      assert(result === Some(NICsClass2Answers(false)))
    }

    "ignore value from DB if it exist in the API" in {
      val result = NICsAnswers.mkPriorClass2Data(Some(class2NicsTrue), Some(NICsStorageAnswers(Some(false)))).value.class2Answers
      assert(result === Some(NICsClass2Answers(true)))
    }
  }

  "3 - mkPriorData" in {
    // No Data
    // Class 2 Yes Journey
    // Class 2 No Journey
    // Class 4 Yes
    // Class 4 No
  }

  "1 - mkClass4ExemptionData" in {
    // Empty List
    // Single Businesses
    // Multiple Businesses with different exemption reason

  }

  "2 - mkPriorClass4Data" in {
    // Empty List
    // Single Business
    // Multiple Businesses
        // - Both reasons
        // Trustee
        // Diver
  }
}
