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

package models.connector.api_1802.request

import cats.implicits.catsSyntaxOptionId
import models.common.{BusinessId, Nino, TaxYear}
import models.connector.api_1803
import models.connector.api_1803.AnnualNonFinancialsType
import models.frontend.nics.NICsClass4Answers

case class CreateAmendSEAnnualSubmissionRequestData(taxYear: TaxYear,
                                                    nino: Nino,
                                                    businessId: BusinessId,
                                                    body: CreateAmendSEAnnualSubmissionRequestBody)

object CreateAmendSEAnnualSubmissionRequestData {
  def mkNicsClassFourSingleBusinessRequestBody(answers: NICsClass4Answers,
                                               existingAnswers: api_1803.SuccessResponseSchema): CreateAmendSEAnnualSubmissionRequestBody = {
//    val maybeBusinessDetailsChangedRecently = existingAnswers.annualNonFinancials.flatMap(_.businessDetailsChangedRecently)

//    val annualNonFinancialsAnswers = AnnualNonFinancials(maybeBusinessDetailsChangedRecently, answers.class4NICs.some, answers.class4ExemptionReason.map(_.exemptionCode))
    val annualNonFinancialsAnswers = AnnualNonFinancials(Some(true), answers.class4NICs.some, answers.class4ExemptionReason.map(_.exemptionCode))
    // TODO SASS-8728 businessDetailsChangedRecently is a compulsory value, where does it come from?

    CreateAmendSEAnnualSubmissionRequestBody(
      existingAnswers.annualAdjustments.map(_.toApi1802AnnualAdjustments),
      existingAnswers.annualAllowances.map(_.toApi1802AnnualAllowance),
      annualNonFinancialsAnswers.some
    )
  }

  def mkEmptyNicsClassFourSingleBusinessRequestBody(
      existingAnswers: api_1803.SuccessResponseSchema): Option[CreateAmendSEAnnualSubmissionRequestBody] = {
    val maybeAnnualNonFinancials: Option[AnnualNonFinancialsType] =
      existingAnswers.annualNonFinancials.map(_.copy(exemptFromPayingClass4Nics = None, class4NicsExemptionReason = None))

    CreateAmendSEAnnualSubmissionRequestBody.mkRequest(
      existingAnswers.annualAdjustments.map(_.toApi1802AnnualAdjustments),
      existingAnswers.annualAllowances.map(_.toApi1802AnnualAllowance),
      maybeAnnualNonFinancials.map(_.toApi1802AnnualNonFinancials)
    )
  }
}
