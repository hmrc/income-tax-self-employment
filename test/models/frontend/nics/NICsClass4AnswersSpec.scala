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
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2, TableFor3}
import org.scalatest.wordspec.AnyWordSpecLike

class NICsClass4AnswersSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {

  val cleanUpExemptionListsFromFECases: TableFor3[String, NICsClass4Answers, NICsClass4Answers] = Table(
    ("testDescription", "nicsClass4Answers", "expectedAnswers"),
    ("not change 'No Class 4' answers", class4NoAnswer, class4NoAnswer),
    (
      "not change answers with valid lists of diving and trustee exemptions",
      class4DiverAndTrusteeMultipleBusinessesAnswers,
      class4DiverAndTrusteeMultipleBusinessesAnswers),
    ("not change answers with valid lists of diving exemptions", class4DiverMultipleBusinessesAnswers, class4DiverMultipleBusinessesAnswers),
    ("not change answers with valid lists of trustee exemptions", class4TrusteeMultipleBusinessesAnswers, class4TrusteeMultipleBusinessesAnswers),
    (
      "remove false IDs from diving list",
      NICsClass4Answers(
        class4NICs = true,
        None,
        Some(List(BusinessId("businessId1"), BusinessId("class-four-other-exemption"), BusinessId("businessId2"))),
        None),
      NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), None)),
    (
      "remove false IDs from trustee list",
      NICsClass4Answers(class4NICs = true, None, None, Some(List(BusinessId("class-four-none-exempt"), BusinessId("businessId3")))),
      NICsClass4Answers(class4NICs = true, None, None, Some(List(BusinessId("businessId3"))))),
    (
      "replace empty lists with 'None'",
      NICsClass4Answers(class4NICs = true, None, Some(List.empty[BusinessId]), Some(List(BusinessId("businessId3")))),
      NICsClass4Answers(class4NICs = true, None, None, Some(List(BusinessId("businessId3"))))),
    (
      "replace empty (after filtering) lists with 'None'",
      NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("businessId3"))), Some(List(BusinessId("class-four-none-exempt")))),
      NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("businessId3"))), None)),
    (
      "overwrite 'class4NICs' value with 'false' if diving and trustee lists are both 'None'",
      NICsClass4Answers(class4NICs = true, None, Some(List.empty[BusinessId]), Some(List(BusinessId("class-four-none-exempt")))),
      NICsClass4Answers(class4NICs = false, None, None, None))
  )

  "cleanUpExemptionListsFromFE" when {
    "user has multiple businesses" should {
      forAll(cleanUpExemptionListsFromFECases) { case (testDescription, nicsClass4Answers, expectedAnswers) =>
        testDescription in {
          assert(nicsClass4Answers.cleanUpExemptionListsFromFE == expectedAnswers)
        }
      }
    }
  }

  val toMultipleBusinessesAnswersCases: TableFor2[NICsClass4Answers, List[Class4ExemptionAnswers]] = Table(
    ("class4Answers", "expectedAnswers"),
    (class4NoAnswer, List.empty[Class4ExemptionAnswers]),
    (class4SingleBusinessAnswers, List.empty[Class4ExemptionAnswers]),
    (NICsClass4Answers(class4NICs = true, None, Some(List.empty[BusinessId]), None), List.empty[Class4ExemptionAnswers]),
    (NICsClass4Answers(class4NICs = true, None, None, Some(List.empty[BusinessId])), List.empty[Class4ExemptionAnswers]),
    (
      NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("businessId1"), BusinessId("businessId2"))), None),
      List(
        Class4ExemptionAnswers(BusinessId("businessId1"), class4Exempt = true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId2"), class4Exempt = true, Some(DiverDivingInstructor))
      )),
    (
      NICsClass4Answers(class4NICs = true, None, None, Some(List(BusinessId("businessId3")))),
      List(Class4ExemptionAnswers(BusinessId("businessId3"), class4Exempt = true, Some(TrusteeExecutorAdmin)))),
    (
      NICsClass4Answers(
        class4NICs = true,
        None,
        Some(List(BusinessId("businessId1"), BusinessId("businessId2"))),
        Some(List(BusinessId("businessId3")))),
      List(
        Class4ExemptionAnswers(BusinessId("businessId1"), class4Exempt = true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId2"), class4Exempt = true, Some(DiverDivingInstructor)),
        Class4ExemptionAnswers(BusinessId("businessId3"), class4Exempt = true, Some(TrusteeExecutorAdmin))
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
