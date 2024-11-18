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

import models.connector.api_1802.request.{Building, BuildingAllowance}
import models.connector.dateFormatter
import models.database.capitalAllowances.NewSpecialTaxSiteDb
import org.scalatest.OptionValues._
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate

class NewSpecialTaxSiteSpec extends AnyWordSpecLike {
  val site: NewSpecialTaxSite = NewSpecialTaxSite(
    Some(true),
    Some(LocalDate.parse("2023-03-01", dateFormatter)),
    Some(LocalDate.parse("2023-03-02", dateFormatter)),
    Some(LocalDate.parse("2023-03-03", dateFormatter)),
    Some(BigDecimal(20000)),
    Some(SpecialTaxSiteLocation(Some("name"), Some("number"), "AA11AA")),
    Some(BigDecimal(10000))
  )

  "toDbModel" should {
    "create db model" in {
      val dbModel = site.toDbModel.value
      assert(
        dbModel === NewSpecialTaxSiteDb(
          Some(true),
          Some(LocalDate.parse("2023-03-01", dateFormatter)),
          Some(LocalDate.parse("2023-03-02", dateFormatter))
        ))
    }
  }

  "toBuildingAllowance" should {
    "create building allowance" in {
      val buildingAllowance = site.toBuildingAllowance
      assert(
        buildingAllowance === Some(
          BuildingAllowance(
            BigDecimal(10000),
            Some(models.connector.api_1802.request.FirstYear("2023-03-03", BigDecimal(20000))),
            Building(Some("name"), Some("number"), "AA11AA")
          ))
      )
    }
  }
}
