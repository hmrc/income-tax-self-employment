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

package models.common.connector.api_1894.request

import cats.implicits.catsSyntaxOptionId
import gens.ExpensesJourneyAnswersGen._
import models.connector.api_1894.request._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import utils.DeductionsBuilder.{goodsToSellOrUse, officeSupplies}

class FinancialsTypeSpec extends AnyWordSpec with Matchers {

  "converting expenses answers to downstream model" should {
    "work with office supplies" in {
      val answers = officeSuppliesJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(adminCosts =
            Some(SelfEmploymentDeductionsDetailType(answers.officeSuppliesAmount.some, answers.officeSuppliesDisallowableAmount))))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult

    }
    "work with goods to sell or use" in {
      val answers = goodsToSellOrUseJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(costOfGoods =
            Some(SelfEmploymentDeductionsDetailPosNegType(answers.goodsToSellOrUseAmount.some, answers.disallowableGoodsToSellOrUseAmount))))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
    "work with repairs and maintenance" in {
      val answers = repairsAndMaintenanceCostsJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(maintenanceCosts = Some(
            SelfEmploymentDeductionsDetailPosNegType(Some(answers.repairsAndMaintenanceAmount), answers.repairsAndMaintenanceDisallowableAmount)
          )))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
    "work with staff costs" in {
      val answers = staffCostsJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(staffCosts = Some(
            SelfEmploymentDeductionsDetailType(Some(answers.staffCostsAmount), answers.staffCostsDisallowableAmount)
          )))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
    "work with advertising costs" in {
      val answers = advertisingOrMarketingJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(advertisingCosts = Some(
            SelfEmploymentDeductionsDetailType(Some(answers.advertisingOrMarketingAmount), answers.advertisingOrMarketingDisallowableAmount)
          )))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
    "work with entertainment costs" in {
      val answers = entertainmentCostsJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(businessEntertainmentCosts = Some(
            SelfEmploymentDeductionsDetailType(None, Some(answers.entertainmentAmount))
          )))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
    "work with construction" in {
      val answers = constructionJourneyAnswersGen.sample.get

      val expectedResult = FinancialsType(
        None,
        Some(
          Deductions.empty.copy(constructionIndustryScheme = Some(
            SelfEmploymentDeductionsDetailType(Some(answers.constructionIndustryAmount), answers.constructionIndustryDisallowableAmount)
          )))
      )

      FinancialsType.fromFrontendModel(answers) shouldBe expectedResult
    }
  }

}
