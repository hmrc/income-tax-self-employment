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

package models.frontend.capitalAllowances.specialTaxSites

import data.api1802.AnnualAllowancesData
import data.api1803.SuccessResponseSchemaData
import models.database.capitalAllowances.{NewSpecialTaxSiteDb, SpecialTaxSitesDb}
import org.scalatest.wordspec.AnyWordSpecLike

class SpecialTaxSitesAnswersSpec extends AnyWordSpecLike {
  val answers = SpecialTaxSitesAnswers(
    true,
    Nil,
    false,
    false,
    1.0
  )

  "toDbModel" should {
    "create db model" in {
      assert(answers.toDbModel === SpecialTaxSitesDb(true, Nil, false, false))
    }
  }

  "toDownStream" should {
    "create downstream model" in {
      val previous = AnnualAllowancesData.example
      val result   = answers.toDownStream(Some(previous))
      assert(
        result ===
          previous.copy(enhancedStructuredBuildingAllowance = Some(Nil))
      )
    }
  }

  "apply" should {
    "create SpecialTaxSitesAnswers" in {
      val dbModel = SpecialTaxSitesDb(
        true,
        List(
          NewSpecialTaxSiteDb(
            None,
            None,
            None
          )),
        false,
        false)
      val result = SpecialTaxSitesAnswers.apply(dbModel, SuccessResponseSchemaData.example)
      assert(
        result === Some(
          SpecialTaxSitesAnswers(
            true,
            List(NewSpecialTaxSite(None, None, None, None, Some(SpecialTaxSiteLocation(Some("name"), None, "postCode")), None)),
            false,
            false,
            1.0)))
    }
  }
}
