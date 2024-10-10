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

package models.frontend.capitalAllowances.writingDownAllowance

import models.connector.api_1802.request.AnnualAllowances
import models.connector.api_1803
import models.database.capitalAllowances.WritingDownAllowanceDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Format, Json}

final case class WritingDownAllowanceAnswers(wdaSpecialRate: Option[Boolean],
                                             wdaSpecialRateClaimAmount: Option[BigDecimal],
                                             wdaMainRate: Option[Boolean],
                                             wdaMainRateClaimAmount: Option[BigDecimal],
                                             wdaSingleAsset: Option[Boolean],
                                             wdaSingleAssetClaimAmounts: Option[BigDecimal])
    extends FrontendAnswers[WritingDownAllowanceDb] {

  def toDbModel: Option[WritingDownAllowanceDb] = Some(
    WritingDownAllowanceDb(
      wdaSpecialRate,
      wdaMainRate,
      wdaSingleAsset
    ))

  override def toDownStreamAnnualAllowances(current: Option[AnnualAllowances]): AnnualAllowances =
    current
      .getOrElse(AnnualAllowances.empty)
      .copy(
        capitalAllowanceSpecialRatePool = wdaSpecialRateClaimAmount,
        capitalAllowanceMainPool = wdaMainRateClaimAmount,
        capitalAllowanceSingleAssetPool = wdaSingleAssetClaimAmounts
      )
}

object WritingDownAllowanceAnswers {
  implicit val format: Format[WritingDownAllowanceAnswers] = Json.format[WritingDownAllowanceAnswers]

  def apply(annualSummaries: api_1803.SuccessResponseSchema): WritingDownAllowanceAnswers = {
    val maybeSpecialRateClaim: Option[BigDecimal] = annualSummaries.annualAllowances.flatMap(_.capitalAllowanceSpecialRatePool)
    val maybeMainRateClaim                        = annualSummaries.annualAllowances.flatMap(_.capitalAllowanceMainPool)
    val maybeSingleRateClaim                      = annualSummaries.annualAllowances.flatMap(_.capitalAllowanceSingleAssetPool)

    new WritingDownAllowanceAnswers(
      Some(maybeSpecialRateClaim.isDefined),
      maybeSpecialRateClaim,
      Some(maybeMainRateClaim.isDefined),
      maybeMainRateClaim,
      Some(maybeSingleRateClaim.isDefined),
      maybeSingleRateClaim
    )
  }
}
