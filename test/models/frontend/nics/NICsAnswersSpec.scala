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
import models.common.BusinessId
import models.connector.api_1803.AnnualNonFinancialsType.Class4NicsExemptionReason
import models.connector.api_1803.{AnnualNonFinancialsType, SuccessResponseSchema}
import models.database.nics.NICsStorageAnswers
import models.frontend.nics.NICsClass4Answers.{Class4ExemptionAnswers, classFourNoneExempt, classFourOtherExemption}
import org.scalatest.wordspec.AnyWordSpecLike

class NICsAnswersSpec extends AnyWordSpecLike {

  "mkClass4ExemptionData" should {
    "return an empty list" in {
      val result = NICsAnswers.mkClass4ExemptionData(List.empty)
      assert(result.isEmpty)
    }

    "return Single Business Class 4 Exemption data from the API" in {
      val singleBusinessResponse                 = AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._002), None)
      val successResponse: SuccessResponseSchema = SuccessResponseSchema(None, None, Some(singleBusinessResponse))
      val result                                 = NICsAnswers.mkClass4ExemptionData(List((successResponse, BusinessId("id1"))))
      assert(result === List(Class4ExemptionAnswers(BusinessId("id1"), true, Some(ExemptionReason.TrusteeExecutorAdmin))))
    }

    "return Multiple Businesses Class 4 Exemption data from the API" in {
      val trusteeExecutorAdminExemptionReason           = AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._002), None)
      val successResponseTrustee: SuccessResponseSchema = SuccessResponseSchema(None, None, Some(trusteeExecutorAdminExemptionReason))
      val diverDivingInstructorReason                   = AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._003), None)
      val successResponseDiver: SuccessResponseSchema   = SuccessResponseSchema(None, None, Some(diverDivingInstructorReason))
      val result = NICsAnswers.mkClass4ExemptionData(List((successResponseTrustee, BusinessId("id1")), (successResponseDiver, BusinessId("id2"))))
      assert(
        result === List(
          Class4ExemptionAnswers(BusinessId("id1"), true, Some(ExemptionReason.TrusteeExecutorAdmin)),
          Class4ExemptionAnswers(BusinessId("id2"), true, Some(ExemptionReason.DiverDivingInstructor))
        ))
    }
  }

  "mkPriorClass4Data" should {
    "return a None when given an empty list" in {
      val result = NICsAnswers.mkPriorClass4Data(List.empty)
      assert(result === None)
    }

    "return Single Business Class 4 Nics Answers when given a list of the single business data" in {
      val singleBusinessData = List(Class4ExemptionAnswers(BusinessId("id1"), class4Exempt = true, Some(ExemptionReason.TrusteeExecutorAdmin)))
      val result             = NICsAnswers.mkPriorClass4Data(singleBusinessData)
      val expectedResult = Some(NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None))))
      assert(result === expectedResult)
    }

    "return Multiple Businesses Class 4 Nics Answers" when {
      "given a list of the Multiple business data" in {
        val businessData = List(
          Class4ExemptionAnswers(BusinessId("id1"), class4Exempt = true, Some(ExemptionReason.TrusteeExecutorAdmin)),
          Class4ExemptionAnswers(BusinessId("id2"), class4Exempt = true, Some(ExemptionReason.DiverDivingInstructor))
        )
        val result = NICsAnswers.mkPriorClass4Data(businessData)
        val expectedResult =
          Some(NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("id2"))), Some(List(BusinessId("id1")))))))
        assert(result === expectedResult)
      }

      "Trustee as exemption reasons of Multiple business data" in {
        val businessData = List(
          Class4ExemptionAnswers(BusinessId("id1"), class4Exempt = true, Some(ExemptionReason.TrusteeExecutorAdmin)),
          Class4ExemptionAnswers(BusinessId("id2"), class4Exempt = false, None)
        )
        val result = NICsAnswers.mkPriorClass4Data(businessData)
        val expectedResult = Some(
          NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, None, Some(List(classFourOtherExemption)), Some(List(BusinessId("id1")))))))
        assert(result === expectedResult)
      }

      "Diver as exemption reasons of Multiple business data" in {
        val businessData = List(
          Class4ExemptionAnswers(BusinessId("id1"), class4Exempt = false, None),
          Class4ExemptionAnswers(BusinessId("id2"), class4Exempt = true, Some(ExemptionReason.DiverDivingInstructor))
        )
        val result = NICsAnswers.mkPriorClass4Data(businessData)
        val expectedResult =
          Some(NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, None, Some(List(BusinessId("id2"))), Some(List(classFourNoneExempt))))))
        assert(result === expectedResult)
      }
    }
  }

  "mkPriorData" should {
    "return None when there is no API or database data" in {
      val result = NICsAnswers.mkPriorData(None, List.empty, None)
      assert(result === None)
    }

    "return Nics Answers with NICS Class 2 answers" when {
      "Class 2 exemption answer is returned from the API" in {
        val disclosuresData = Some(class2NicsTrue)
        val result          = NICsAnswers.mkPriorData(disclosuresData, List.empty, None)
        val expectedResult  = Some(NICsAnswers(Some(NICsClass2Answers(true)), None))
        assert(result === expectedResult)
      }

      "'No Class 2 exemption' answer is returned from the database" in {
        val databaseData   = Some(NICsStorageAnswers(Some(false)))
        val result         = NICsAnswers.mkPriorData(None, List.empty, databaseData)
        val expectedResult = Some(NICsAnswers(Some(NICsClass2Answers(false)), None))
        assert(result === expectedResult)
      }
    }
    "return Nics Answers with NICS Class 4 answers" when {
      "Class 4 exemption answer is returned from the API" in {
        val businessResponse                   = AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._002), None)
        val apiResponse: SuccessResponseSchema = SuccessResponseSchema(None, None, Some(businessResponse))
        val result                             = NICsAnswers.mkPriorData(None, List((apiResponse, BusinessId("id1"))), None)
        val class4Answers                      = NICsClass4Answers(true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None)
        val expectedResult                     = Some(NICsAnswers(None, Some(class4Answers)))
        assert(result === expectedResult)
      }

      "'No Class 4 exemption' answer is returned from the database" in {
        val businessResponse                   = AnnualNonFinancialsType(Some(false), None, None)
        val apiResponse: SuccessResponseSchema = SuccessResponseSchema(None, None, Some(businessResponse))
        val result                             = NICsAnswers.mkPriorData(None, List((apiResponse, BusinessId("id1"))), None)
        val class4Answers                      = NICsClass4Answers(false, None, None, None)
        val expectedResult                     = Some(NICsAnswers(None, Some(class4Answers)))
        assert(result === expectedResult)
      }
    }

  }
}
