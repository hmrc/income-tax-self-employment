package models.frontend.expenses.financialCharges

import play.api.libs.json.{Format, Json}

case class FinancialChargesJourneyAnswers(financialChargesAmount: BigDecimal, financialChargesDisallowableAmount: Option[BigDecimal])

object FinancialChargesJourneyAnswers {
  implicit val formats: Format[FinancialChargesJourneyAnswers] = Json.format[FinancialChargesJourneyAnswers]
}
