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

import bulders.NICsAnswersBuilder._
import models.common.BusinessId
import models.frontend.nics.ExemptionReason.{DiverDivingInstructor, TrusteeExecutorAdmin}
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike

class NICsClass4AnswersSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {

  val toMultipleBusinessesAnswersCases = Table(
    ("class4Answers", "expectedAnswers"),
    (class4NoAnswer, List.empty[Class4ExemptionAnswers]),
    (class4SingleBusinessAnswers, List.empty[Class4ExemptionAnswers]),
    (NICsClass4Answers(true, None, Some(List.empty[BusinessId]), None), List.empty[Class4ExemptionAnswers]),
    (NICsClass4Answers(true, None, None, Some(List.empty[BusinessId])), List.empty[Class4ExemptionAnswers]),
    (
      NICsClass4Answers(true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), None),
      List(
        Class4ExemptionAnswers(BusinessId("businessId1"), true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId2"), true, Some(DiverDivingInstructor))
      )),
    (
      NICsClass4Answers(true, None, None, Some(List(BusinessId("businessId3")))),
      List(Class4ExemptionAnswers(BusinessId("businessId3"), true, Some(TrusteeExecutorAdmin)))),
    (
      NICsClass4Answers(true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), Some(List(BusinessId("businessId3")))),
      List(
        Class4ExemptionAnswers(BusinessId("businessId1"), true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId2"), true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId3"), true, Some(TrusteeExecutorAdmin))
      ))
  )

  "toMultipleBusinessesAnswers" should {
    "return a list of Class4ExemptionAnswers for each business ID in the class4DivingExempt and class4NonDivingExempt lists" in {
      forAll(toMultipleBusinessesAnswersCases) { case (class4Answers, expectedAnswers) =>
        assert(class4Answers.toMultipleBusinessesAnswers == expectedAnswers)
      }
    }
  }
}
