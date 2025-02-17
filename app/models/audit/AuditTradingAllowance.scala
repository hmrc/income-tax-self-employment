/*
 * Copyright 2025 HM Revenue & Customs
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

package models.audit

import models.common.TaxYear.asTys
import models.common.{BusinessId, JourneyContextWithNino, Nino}
import models.frontend.income.HowMuchTradingAllowance.Maximum
import models.frontend.income.IncomeJourneyAnswers
import models.frontend.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import play.api.libs.json.{Json, OFormat}

case class AuditTradingAllowance(nino: Nino,
                                 businessId: BusinessId,
                                 businessName: String,
                                 taxYear: String,
                                 useTradingAllowance: Boolean,
                                 useMaximum: Option[Boolean],
                                 tradingAllowanceUsed: Option[BigDecimal])

object AuditTradingAllowance {

  val auditType: String = "TradingIncomeAllowance"

  implicit val format: OFormat[AuditTradingAllowance] = Json.format[AuditTradingAllowance]

  def apply(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers): AuditTradingAllowance =
    answers.tradingAllowance match {
      case DeclareExpenses =>
        AuditTradingAllowance(ctx.nino, ctx.businessId, ctx.businessId.toString, asTys(ctx.taxYear), useTradingAllowance = false, None, None)
      case UseTradingAllowance =>
        val useMaximum: Boolean                      = answers.howMuchTradingAllowance.contains(Maximum)
        val tradingAllowanceUsed: Option[BigDecimal] = if (useMaximum) None else answers.tradingAllowanceAmount
        AuditTradingAllowance(
          ctx.nino,
          ctx.businessId,
          ctx.businessId.toString,
          asTys(ctx.taxYear),
          useTradingAllowance = true,
          Option(useMaximum),
          tradingAllowanceUsed)
    }

}
