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

package models.frontend.capitalAllowances.balancingAllowance

import models.connector.api_1802.request.AnnualAllowances
import models.connector.api_1803
import models.database.capitalAllowances.BalancingAllowanceDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Json, OFormat}

final case class BalancingAllowanceAnswers(balancingAllowance: Boolean, balancingAllowanceAmount: Option[BigDecimal])
    extends FrontendAnswers[BalancingAllowanceDb] {

  def toDbModel: Option[BalancingAllowanceDb] = Some(BalancingAllowanceDb(balancingAllowance))

  def toDownStream(current: Option[AnnualAllowances]): AnnualAllowances =
    current.getOrElse(AnnualAllowances.empty).copy(allowanceOnSales = balancingAllowanceAmount)
}

object BalancingAllowanceAnswers {
  implicit val formats: OFormat[BalancingAllowanceAnswers] = Json.format[BalancingAllowanceAnswers]

  def apply(dbAnswers: BalancingAllowanceDb, annualSummaries: api_1803.SuccessResponseSchema): BalancingAllowanceAnswers =
    new BalancingAllowanceAnswers(
      dbAnswers.balancingAllowance,
      balancingAllowanceAmount = annualSummaries.annualAllowances.flatMap(_.allowanceOnSales)
    )
}
