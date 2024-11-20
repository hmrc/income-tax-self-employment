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

package models.frontend.capitalAllowances.balancingCharge

import models.connector.api_1802.request.AnnualAdjustments
import models.database.capitalAllowances.BalancingChargeDb
import models.frontend.FrontendAnswers
import play.api.libs.json.{Json, OFormat}

final case class BalancingChargeAnswers(balancingCharge: Boolean, balancingChargeAmount: Option[BigDecimal])
    extends FrontendAnswers[BalancingChargeDb] {

  def toDbModel: Option[BalancingChargeDb] = Some(BalancingChargeDb(balancingCharge))

  override def toDownStreamAnnualAdjustments(current: Option[AnnualAdjustments]): AnnualAdjustments =
    current.getOrElse(AnnualAdjustments.empty).copy(balancingChargeOther = balancingChargeAmount)
}

object BalancingChargeAnswers {
  implicit val formats: OFormat[BalancingChargeAnswers] = Json.format[BalancingChargeAnswers]
}
