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
import models.common.{BusinessId, JourneyContextWithNino, Nino, TaxYear}
import models.connector.api_1803
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers

case class CreateAmendSEAnnualSubmissionRequestData(taxYear: TaxYear,
                                                    nino: Nino,
                                                    businessId: BusinessId,
                                                    body: CreateAmendSEAnnualSubmissionRequestBody)

object CreateAmendSEAnnualSubmissionRequestData {
  def mkNicsClassFourRequestData(ctx: JourneyContextWithNino,
                                 answer: Class4ExemptionAnswers,
                                 existingAnswers: api_1803.SuccessResponseSchema): CreateAmendSEAnnualSubmissionRequestData =
    CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, mkNicsClassFourRequestBody(answer, existingAnswers))

  def mkNicsClassFourRequestBody(answer: Class4ExemptionAnswers,
                                 existingAnswers: api_1803.SuccessResponseSchema): CreateAmendSEAnnualSubmissionRequestBody =
    CreateAmendSEAnnualSubmissionRequestBody(
      existingAnswers.annualAdjustments.map(_.toApi1802AnnualAdjustments),
      existingAnswers.annualAllowances.map(_.toApi1802AnnualAllowance),
      AnnualNonFinancials(answer.class4Exempt, answer.exemptionReason.map(_.exemptionCode)).some
    )
}
