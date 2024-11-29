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

package models.connector.api_1867

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._
import java.time.LocalDate

class ReliefClaimSpec extends PlaySpec {

  "ReliefClaim" should {

    "write and read correctly" in {
      val reliefClaim = ReliefClaim(
        incomeSourceId = "12345",
        incomeSourceType = Some(UkProperty),
        reliefClaimed = CarryForward,
        taxYearClaimedFor = "2023-24",
        claimId = "claim123",
        sequence = Some(1),
        submissionDate = LocalDate.of(2024, 11, 29)
      )

      val json = Json.toJson(reliefClaim)
      json.validate[ReliefClaim] mustBe JsSuccess(reliefClaim)
    }

    "identify self-employment claim correctly" in {
      val reliefClaim = ReliefClaim(
        incomeSourceId = "12345",
        incomeSourceType = None,
        reliefClaimed = CarryForward,
        taxYearClaimedFor = "2023-24",
        claimId = "claim123",
        sequence = Some(1),
        submissionDate = LocalDate.of(2024, 11, 29)
      )

      reliefClaim.isSelfEmploymentClaim mustBe true
      reliefClaim.isPropertyClaim mustBe false
    }

    "identify property claim correctly" in {
      val reliefClaim = ReliefClaim(
        incomeSourceId = "12345",
        incomeSourceType = Some(UkProperty),
        reliefClaimed = CarrySidewaysFHL,
        taxYearClaimedFor = "2023-24",
        claimId = "claim123",
        sequence = Some(1),
        submissionDate = LocalDate.of(2024, 11, 29)
      )

      reliefClaim.isSelfEmploymentClaim mustBe false
      reliefClaim.isPropertyClaim mustBe true
    }

    "fail to read invalid JSON" in {
      val json = Json.obj(
        "incomeSourceId" -> "12345",
        "incomeSourceType" -> "InvalidType",
        "reliefClaimed" -> "CF",
        "taxYearClaimedFor" -> "2023-24",
        "claimId" -> "claim123",
        "sequence" -> 1,
        "submissionDate" -> "2024-11-29"
      )

      json.validate[ReliefClaim] mustBe a[JsError]
    }

    "fail to read invalid IncomeSourceType" in {
      val json = Json.obj(
        "incomeSourceId" -> "12345",
        "incomeSourceType" -> JsString("InvalidType"),
        "reliefClaimed" -> "CF",
        "taxYearClaimedFor" -> "2023-24",
        "claimId" -> "claim123",
        "sequence" -> 1,
        "submissionDate" -> "2024-11-29"
      )

      json.validate[ReliefClaim] mustBe a[JsError]
    }
  }
}