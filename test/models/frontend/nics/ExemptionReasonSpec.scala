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

import models.connector.api_1803.AnnualNonFinancialsType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class ExemptionReasonSpec extends AnyWordSpec with Matchers {

  "ExemptionReason" should {

    "have the correct values" in {
      ExemptionReason.values should contain allOf (
        ExemptionReason.TrusteeExecutorAdmin,
        ExemptionReason.DiverDivingInstructor
      )
    }

    "return the correct exemption code" in {
      ExemptionReason.TrusteeExecutorAdmin.exemptionCode shouldBe "002"
      ExemptionReason.DiverDivingInstructor.exemptionCode shouldBe "003"
    }

    "write to JSON correctly" in {
      val json = Json.toJson(ExemptionReason.TrusteeExecutorAdmin)
      json.toString() shouldBe """"trusteeExecutorAdmin""""
    }

    "read from JSON correctly" in {
      val json = Json.parse(""""trusteeExecutorAdmin"""")
      json.as[ExemptionReason] shouldBe ExemptionReason.TrusteeExecutorAdmin
    }

    "convert from non-financial type correctly" in {
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._001) shouldBe None
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._003) shouldBe Some(ExemptionReason.DiverDivingInstructor)
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._002) shouldBe Some(ExemptionReason.TrusteeExecutorAdmin)
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._004) shouldBe None
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._005) shouldBe None
      ExemptionReason.fromNonFinancialType(AnnualNonFinancialsType.Class4NicsExemptionReason._006) shouldBe None
    }
  }
}
