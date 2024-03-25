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

import models.connector.api_1802.request.Building
import models.connector.api_1803.StructuredBuildingAllowanceTypeInnerBuilding
import org.scalatest.wordspec.AnyWordSpecLike

class SpecialTaxSiteLocationSpec extends AnyWordSpecLike {
  val location = SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA")

  "toBuilding" should {
    "create building" in {
      assert(
        location.toBuilding === Building(
          Some("name"),
          Some("number"),
          "AA11AA"
        ))
    }
  }

  "apply" should {
    "create SpecialTaxSiteLocation" in {
      assert(
        SpecialTaxSiteLocation.apply(
          StructuredBuildingAllowanceTypeInnerBuilding(Some("name"), Some("number"), "AA11AA")
        ) === SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA")
      )
    }
  }
}
