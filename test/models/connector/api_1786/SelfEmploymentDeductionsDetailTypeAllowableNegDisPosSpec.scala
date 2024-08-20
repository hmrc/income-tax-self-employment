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

package models.connector.api_1786

import models.connector.{api_1894, api_1895}
import org.scalatest.wordspec.AnyWordSpecLike

class SelfEmploymentDeductionsDetailTypeAllowableNegDisPosSpec extends AnyWordSpecLike {
  private val data = SelfEmploymentDeductionsDetailTypeAllowableNegDisPos(1.0, Some(2.0))

  "toApi1895" should {
    "convert the model to API 1895" in {
      assert(data.toApi1895 === api_1895.request.SelfEmploymentDeductionsDetailAllowablePosNegType(Some(1.0), Some(2.0)))
    }
  }

  "toApi1894" should {
    "convert the model to API 1894" in {
      assert(data.toApi1894 === api_1894.request.SelfEmploymentDeductionsDetailAllowablePosNegType(Some(1.0), Some(2.0)))
    }
  }
}
