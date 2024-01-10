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

package models.connector

import models.connector.api_1786.SuccessResponseSchema
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import utils.ResponseParser

trait Api1786ExpensesResponseParser[Result] extends ResponseParser[api_1786.SuccessResponseSchema, Result] {
  override def parse(response: api_1786.SuccessResponseSchema): Result
}

object Api1786ExpensesResponseParser {

  private val noneFound = 0 // TODO: What if it's None?

  implicit val goodsToSellOrUseParser: Api1786ExpensesResponseParser[GoodsToSellOrUseJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      GoodsToSellOrUseJourneyAnswers(
        goodsToSellOrUseAmount = response.financials.deductions.flatMap(_.costOfGoods.map(_.amount)).getOrElse(noneFound),
        disallowableGoodsToSellOrUseAmount = response.financials.deductions.flatMap(_.costOfGoods.flatMap(_.disallowableAmount))
      )

  implicit val repairsAndMaintenanceCostsParser: Api1786ExpensesResponseParser[RepairsAndMaintenanceCostsJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      RepairsAndMaintenanceCostsJourneyAnswers(
        repairsAndMaintenanceAmount = response.financials.deductions.flatMap(_.maintenanceCosts.map(_.amount)).getOrElse(noneFound),
        repairsAndMaintenanceDisallowableAmount = response.financials.deductions.flatMap(_.maintenanceCosts.flatMap(_.disallowableAmount))
      )

  implicit val entertainmentCostsParser: Api1786ExpensesResponseParser[EntertainmentJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      EntertainmentJourneyAnswers(
        entertainmentAmount = response.financials.deductions.flatMap(_.businessEntertainmentCosts.map(_.amount)).getOrElse(noneFound)
      )

  implicit val officeSuppliesParser: Api1786ExpensesResponseParser[OfficeSuppliesJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      OfficeSuppliesJourneyAnswers(
        officeSuppliesAmount = response.financials.deductions.flatMap(_.adminCosts.map(_.amount)).getOrElse(noneFound),
        officeSuppliesDisallowableAmount = response.financials.deductions.flatMap(_.adminCosts.flatMap(_.disallowableAmount))
      )

  implicit val constructionParser: Api1786ExpensesResponseParser[ConstructionJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      ConstructionJourneyAnswers(
        constructionIndustryAmount = response.financials.deductions.flatMap(_.constructionIndustryScheme.map(_.amount)).getOrElse(noneFound),
        constructionIndustryDisallowableAmount = response.financials.deductions.flatMap(_.constructionIndustryScheme.flatMap(_.disallowableAmount))
      )

  implicit val professionalFeesParser: Api1786ExpensesResponseParser[ProfessionalFeesJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      ProfessionalFeesJourneyAnswers(
        professionalFeesAmount = response.financials.deductions.flatMap(_.professionalFees.map(_.amount)).getOrElse(noneFound),
        professionalFeesDisallowableAmount = response.financials.deductions.flatMap(_.professionalFees.flatMap(_.disallowableAmount))
      )

  implicit val interestParser: Api1786ExpensesResponseParser[InterestJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      InterestJourneyAnswers(
        interestAmount = response.financials.deductions.flatMap(_.interest.map(_.amount)).getOrElse(noneFound),
        interestDisallowableAmount = response.financials.deductions.flatMap(_.interest.flatMap(_.disallowableAmount))
      )

  implicit val staffCostsParser: Api1786ExpensesResponseParser[StaffCostsJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      StaffCostsJourneyAnswers(
        staffCostsAmount = response.financials.deductions.flatMap(_.staffCosts.map(_.amount)).getOrElse(noneFound),
        staffCostsDisallowableAmount = response.financials.deductions.flatMap(_.staffCosts.flatMap(_.disallowableAmount))
      )

  implicit val depreciationCostsParser: Api1786ExpensesResponseParser[DepreciationCostsJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      DepreciationCostsJourneyAnswers(
        depreciationDisallowableAmount = response.financials.deductions.flatMap(_.depreciation.map(_.amount)).getOrElse(noneFound)
      )

  implicit val asOneTotalAnswersParser: Api1786ExpensesResponseParser[ExpensesTailoringAnswers.AsOneTotalAnswers] =
    (response: SuccessResponseSchema) =>
      ExpensesTailoringAnswers.AsOneTotalAnswers(
        response.financials.deductions.flatMap(_.simplifiedExpenses).getOrElse(noneFound)
      )

  implicit val advertisingOrMarketingParser: Api1786ExpensesResponseParser[AdvertisingOrMarketingJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      AdvertisingOrMarketingJourneyAnswers(
        advertisingOrMarketingAmount = response.financials.deductions.flatMap(_.advertisingCosts.map(_.amount)).getOrElse(noneFound),
        advertisingOrMarketingDisallowableAmount = response.financials.deductions.flatMap(_.advertisingCosts.flatMap(_.disallowableAmount))
      )

  implicit val financialChargesParser: Api1786ExpensesResponseParser[FinancialChargesJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      FinancialChargesJourneyAnswers(
        financialChargesAmount = response.financials.deductions.flatMap(_.financialCharges.map(_.amount)).getOrElse(noneFound),
        financialChargesDisallowableAmount = response.financials.deductions.flatMap(_.financialCharges.flatMap(_.disallowableAmount))
      )

  implicit val otherExpensesParser: Api1786ExpensesResponseParser[OtherExpensesJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      OtherExpensesJourneyAnswers(
        otherExpensesAmount = response.financials.deductions.flatMap(_.other.map(_.amount)).getOrElse(noneFound),
        otherExpensesDisallowableAmount = response.financials.deductions.flatMap(_.other.flatMap(_.disallowableAmount))
      )

  implicit val irrecoverableDebtsParser: Api1786ExpensesResponseParser[IrrecoverableDebtsJourneyAnswers] =
    (response: SuccessResponseSchema) =>
      IrrecoverableDebtsJourneyAnswers(
        irrecoverableDebtsAmount = response.financials.deductions.flatMap(_.badDebt.map(_.amount)).getOrElse(noneFound),
        irrecoverableDebtsDisallowableAmount = response.financials.deductions.flatMap(_.badDebt.flatMap(_.disallowableAmount))
      )
}
