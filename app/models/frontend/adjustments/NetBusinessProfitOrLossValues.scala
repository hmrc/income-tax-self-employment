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

package models.frontend.adjustments

import models.connector.api_1786.IncomesType
import models.connector.api_1803.AnnualAdjustmentsType
import models.connector.api_1871.BusinessIncomeSourcesSummaryResponse
import models.connector.{api_1786, api_1803}
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.error.ServiceError
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Format, Json}

case class NetBusinessProfitOrLossValues(turnover: BigDecimal,
                                         incomeNotCountedAsTurnover: BigDecimal,
                                         totalExpenses: BigDecimal,
                                         netProfit: BigDecimal,
                                         netLoss: BigDecimal,
                                         balancingCharge: BigDecimal,
                                         goodsAndServicesForOwnUse: BigDecimal,
                                         disallowableExpenses: BigDecimal,
                                         totalAdditionsToNetProfit: BigDecimal,
                                         capitalAllowances: BigDecimal,
                                         turnoverNotTaxableAsBusinessProfit: BigDecimal,
                                         totalDeductionsFromNetProfit: BigDecimal) {}

object NetBusinessProfitOrLossValues {
  implicit val formats: Format[NetBusinessProfitOrLossValues] = Json.format[NetBusinessProfitOrLossValues]

  def fromApiAnswers(incomeSummary: BusinessIncomeSourcesSummaryResponse,
                     periodSummary: api_1786.SuccessResponseSchema,
                     annualSubmission: api_1803.SuccessResponseSchema): Either[ServiceError, NetBusinessProfitOrLossValues] = {
    val maybePeriodIncomes: Option[IncomesType]               = periodSummary.financials.incomes
    val maybeAnnualAdjustments: Option[AnnualAdjustmentsType] = annualSubmission.annualAdjustments
    maybePeriodIncomes match {
      case Some(periodIncomes) =>
        Right(
          NetBusinessProfitOrLossValues(
            turnover = periodIncomes.turnover.getOrElse(0),
            incomeNotCountedAsTurnover = periodIncomes.other.getOrElse(0),
            totalExpenses = incomeSummary.totalExpenses,
            netProfit = incomeSummary.netProfit,
            netLoss = incomeSummary.netLoss,
            balancingCharge = BigDecimal(0), // TODO when developed, this should be annualAdjustments.balancingChargeOther
            goodsAndServicesForOwnUse = maybeAnnualAdjustments.flatMap(_.goodsAndServicesOwnUse).getOrElse(BigDecimal(0)),
            disallowableExpenses = getDisallowableExpenses(periodSummary).getOrElse(0),
            totalAdditionsToNetProfit = incomeSummary.totalAdditions.getOrElse(0),
            capitalAllowances = getCapitalAllowances(annualSubmission).getOrElse(0),
            turnoverNotTaxableAsBusinessProfit = maybeAnnualAdjustments.flatMap(_.includedNonTaxableProfits).getOrElse(BigDecimal(0)),
            totalDeductionsFromNetProfit = incomeSummary.totalDeductions.getOrElse(0)
          ))
      case None => Left(SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody("NOT_FOUND", "API 1786 Financials Incomes is empty")))
    }
  }

  private def getCapitalAllowances(annualSubmission: api_1803.SuccessResponseSchema): Option[BigDecimal] =
    annualSubmission.annualAllowances.map { annualAllowances =>
      List(
        annualAllowances.annualInvestmentAllowance,
        annualAllowances.capitalAllowanceMainPool,
        annualAllowances.capitalAllowanceSpecialRatePool,
        annualAllowances.zeroEmissionGoodsVehicleAllowance,
        annualAllowances.businessPremisesRenovationAllowance,
        annualAllowances.enhanceCapitalAllowance,
        annualAllowances.allowanceOnSales,
        annualAllowances.capitalAllowanceSingleAssetPool,
        annualAllowances.electricChargePointAllowance,
        annualAllowances.zeroEmissionsCarAllowance,
        annualAllowances.structuredBuildingAllowance.map(_.map(_.amount).sum),
        annualAllowances.enhancedStructuredBuildingAllowance.map(_.map(_.amount).sum)
      ).flatten.sum
    }

  private def getDisallowableExpenses(periodSummary: api_1786.SuccessResponseSchema): Option[BigDecimal] =
    periodSummary.financials.deductions.map { deductions =>
      val listPos = List(
        deductions.constructionIndustryScheme,
        deductions.staffCosts,
        deductions.travelCosts,
        deductions.adminCosts,
        deductions.businessEntertainmentCosts,
        deductions.advertisingCosts,
        deductions.other
      ).flatten.map(_.disallowableAmount)
      val listPosNeg = List(
        deductions.costOfGoods,
        deductions.premisesRunningCosts,
        deductions.maintenanceCosts,
        deductions.interest,
        deductions.financialCharges,
        deductions.badDebt,
        deductions.depreciation
      ).flatten.map(_.disallowableAmount)
      val listAllowablePosNeg = List(deductions.professionalFees).flatten.map(_.disallowableAmount)
      (listPos ++ listPosNeg ++ listAllowablePosNeg).flatten.sum
    }
}
