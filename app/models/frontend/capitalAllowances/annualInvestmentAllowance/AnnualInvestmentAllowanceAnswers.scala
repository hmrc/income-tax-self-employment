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

package models.frontend.capitalAllowances.annualInvestmentAllowance

import play.api.libs.json.{Format, Json, OFormat}

case class AnnualInvestmentAllowanceJourneyAnswers(annualInvestmentAllowance: BigDecimal)

object AnnualInvestmentAllowanceJourneyAnswers {
  implicit val formats: Format[AnnualInvestmentAllowanceJourneyAnswers] = Json.format[AnnualInvestmentAllowanceJourneyAnswers]
}

case class AnnualInvestmentAllowanceAnswers(annualInvestmentAllowance: Boolean, annualInvestmentAllowanceAmount: Option[BigDecimal])

object AnnualInvestmentAllowanceAnswers {
  implicit val formats: Format[AnnualInvestmentAllowanceAnswers] = Json.format[AnnualInvestmentAllowanceAnswers]
}

final case class AnnualInvestmentAllowanceDb(annualInvestmentAllowance: Boolean)

object AnnualInvestmentAllowanceDb {
  implicit val format: OFormat[AnnualInvestmentAllowanceDb] = Json.format[AnnualInvestmentAllowanceDb]
}
