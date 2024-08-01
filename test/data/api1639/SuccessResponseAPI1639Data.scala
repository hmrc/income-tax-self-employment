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

package data.api1639

import models.connector.api_1639.{SuccessResponseAPI1639, SuccessResponseAPI1639Class2Nics, SuccessResponseAPI1639TaxAvoidanceInner}
import utils.BaseSpec.currTaxYear

object SuccessResponseAPI1639Data {
  val class2NicsTrue = SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))
  val full = SuccessResponseAPI1639(
    Some(List(SuccessResponseAPI1639TaxAvoidanceInner("arn", currTaxYear.endYear.toString))),
    Some(SuccessResponseAPI1639Class2Nics(Some(true))))
}
