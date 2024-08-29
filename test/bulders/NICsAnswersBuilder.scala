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

package bulders

import models.common.BusinessId
import models.frontend.nics.{ExemptionReason, NICsClass4Answers}

object NICsAnswersBuilder {

  val class4NoAnswer = NICsClass4Answers(false, None, None, None)

  val class4SingleBusinessAnswers = NICsClass4Answers(true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None)

  val class4DiverAndTrusteeMultipleBusinessesAnswers =
    NICsClass4Answers(true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), Some(List(BusinessId("businessId3"))))
  val class4DiverMultipleBusinessesAnswers   = NICsClass4Answers(true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), None)
  val class4TrusteeMultipleBusinessesAnswers = NICsClass4Answers(true, None, None, Some(List(BusinessId("businessId3"))))

}
