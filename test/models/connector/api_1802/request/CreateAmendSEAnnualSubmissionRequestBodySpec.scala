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

package models.connector.api_1802.request

import cats.implicits._
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestBody._
import org.scalatest.wordspec.AnyWordSpecLike

class CreateAmendSEAnnualSubmissionRequestBodySpec extends AnyWordSpecLike {

  "mkRequest" should {
    "create None if only empty objects passed" in {
      assert(
        mkRequest(AnnualAdjustments.empty.some, AnnualAllowances.empty.some, None) === None
      )
    }

    "create an object if at least one value in AnnualAdjustments is defined" in {
      val adjustments = AnnualAdjustments.empty.copy(includedNonTaxableProfits = Some(1.0)).some
      assert(
        mkRequest(adjustments, AnnualAllowances.empty.some, None) === CreateAmendSEAnnualSubmissionRequestBody(adjustments, None, None).some
      )
    }

    "create an object if at least one value in AnnualAllowances is defined" in {
      val allowances = AnnualAllowances.empty.copy(annualInvestmentAllowance = Some(1.0)).some
      assert(
        mkRequest(AnnualAdjustments.empty.some, allowances, None) === CreateAmendSEAnnualSubmissionRequestBody(None, allowances, None).some
      )
    }

    "create an object if AnnualNonFinancials is defined" in {
      val financials = AnnualNonFinancials(false, None).some
      assert(
        mkRequest(AnnualAdjustments.empty.some, AnnualAllowances.empty.some, financials) === CreateAmendSEAnnualSubmissionRequestBody(
          None,
          None,
          financials).some
      )
    }

  }
}
