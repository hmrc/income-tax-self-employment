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

package models.connector.api_1638

import data.api1639.SuccessResponseAPI1639Data
import models.frontend.nics.NICsClass2Answers
import org.scalatest.wordspec.AnyWordSpecLike
import utils.BaseSpec.currTaxYear

class RequestSchemaAPI1638Spec extends AnyWordSpecLike {

  "mkRequestBody" should {
    "return class2 set to true" in {
      val answers = NICsClass2Answers(true)
      val result  = RequestSchemaAPI1638.mkRequestBody(answers, None)
      assert(result === Some(RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
    }

    "return None when answer class2 set to false and no other fields in the object exist" in {
      val answers = NICsClass2Answers(false)
      val result  = RequestSchemaAPI1638.mkRequestBody(answers, maybeExistingDisclosures = None)
      assert(result === None)
    }

    "return an object with class 2 set to None if other fields exist" in {
      val answers = NICsClass2Answers(false)
      val result  = RequestSchemaAPI1638.mkRequestBody(answers, Some(SuccessResponseAPI1639Data.full))
      assert(result === Some(RequestSchemaAPI1638(Some(List(RequestSchemaAPI1638TaxAvoidanceInner("arn", currTaxYear.toString))), None)))
    }
  }
}
