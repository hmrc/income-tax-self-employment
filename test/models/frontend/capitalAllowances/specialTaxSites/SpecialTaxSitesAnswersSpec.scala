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
  val answers: SpecialTaxSitesAnswers = SpecialTaxSitesAnswers(
    specialTaxSites = true,
    Some(Nil),
    Some(false),
    Some(false),
    Some(1.0)
  )

  "toDbModel" should {
    "create db model" in {
      assert(answers.toDbModel === Some(SpecialTaxSitesDb(true, Some(Nil))))
    }
  }

  "toDownStream" should {
    "create downstream model" in {
      val previous = AnnualAllowancesData.example
      val result   = answers.toDownStreamAnnualAllowances(Some(previous))
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
        Some(
          List(
            NewSpecialTaxSiteDb(
              None,
              None,
              None
            )))
      )

      val result = SpecialTaxSitesAnswers.apply(dbModel, SuccessResponseSchemaData.example)
      assert(
        result === SpecialTaxSitesAnswers(
          true,
          Some(List(NewSpecialTaxSite(None, None, None, None, None, Some(SpecialTaxSiteLocation(Some("name"), None, "postCode")), Some(1.0)))),
          None,
          None,
          None
        ))
    }
  }
}
