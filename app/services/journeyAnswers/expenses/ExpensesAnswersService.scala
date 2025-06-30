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

package services.journeyAnswers.expenses

import cats.data.EitherT
import cats.implicits._
import connectors.IFS.IFSConnector
import models.common.JourneyName._
import models.common._
import models.connector.api_1894.request.{Deductions, FinancialsType}
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestData
import models.connector.{Api1786ExpensesResponseParser, api_1786, api_1894}
import models.database.JourneyAnswers
import models.database.expenses.{ExpensesCategoriesDb, TaxiMinicabOrRoadHaulageDb, WorkplaceRunningCostsDb}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseAnswers
import models.frontend.expenses.tailoring
import models.frontend.expenses.tailoring.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import services.journeyAnswers.getPersistedAnswers
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ExpensesAnswersService @Inject() (connector: IFSConnector, repository: JourneyAnswersRepository)(implicit ec: ExecutionContext) {

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] = {
    println ("EGG")
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))
  }

  def saveTailoringAnswers(ctx: JourneyContextWithNino, answers: ExpensesTailoringAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    def createFinancials(existingFinancials: api_1786.FinancialsType): FinancialsType = {
      val financials = existingFinancials.toApi1894

      answers match {
        case NoExpensesAnswers => financials.copy(deductions = None) // clear all expenses
        case AsOneTotalAnswers(totalAmount) =>
          val deductions = api_1894.request.Deductions.empty
          financials.updateDeductions(deductions.copy(simplifiedExpenses = Option(totalAmount))) // clear all expenses and set one total
        case _: ExpensesTailoringIndividualCategoriesAnswers =>
          val deductions = financials.deductions.getOrElse(api_1894.request.Deductions.empty)
          financials.updateDeductions(deductions.copy(simplifiedExpenses = None)) // leave all existing deductions except simplifiedExpenses
      }
    }

    for {
      existingIncome <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      updatedFinancials = createFinancials(existingIncome.financials)
      _ <- submitTailoringAnswers(ctx, updatedFinancials, existingIncome.financials.incomes.flatMap(_.taxTakenOffTradingIncome))
      _ <- persistTailoringAnswers(ctx, answers)
    } yield ()
  }

  private def submitTailoringAnswers(ctx: JourneyContextWithNino, financials: FinancialsType, existingTaxTakenOffTradingIncome: Option[BigDecimal])(
      implicit hc: HeaderCarrier) = {
    val body        = financials.toApi1895(existingTaxTakenOffTradingIncome)
    val requestData = AmendSEPeriodSummaryRequestData(ctx.taxYear, ctx.nino, ctx.businessId, body)
    EitherT(connector.amendSEPeriodSummary(requestData)).leftAs[ServiceError]
  }

  private def persistTailoringAnswers(ctx: JourneyContextWithNino, answers: ExpensesTailoringAnswers) = answers match {
    case NoExpensesAnswers | _: ExpensesTailoringIndividualCategoriesAnswers =>
      persistAnswers(ctx.businessId, ctx.taxYear, ctx.mtditid, ExpensesTailoring, answers)
    case _: AsOneTotalAnswers =>
      persistAnswers(ctx.businessId, ctx.taxYear, ctx.mtditid, ExpensesTailoring, ExpensesCategoriesDb(tailoring.ExpensesTailoring.TotalAmount))
  }

  def getAnswers[A: Api1786ExpensesResponseParser](ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[A] = {
    val parser = implicitly[Api1786ExpensesResponseParser[A]]
    for {
      successResponse <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      result = parser.parse(successResponse)
    } yield result
  }

  def getExpensesTailoringAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ExpensesTailoringAnswers]] =
    for {
      maybeAnswers             <- getDbAnswers(ctx, ExpensesTailoring)
      existingTailoringAnswers <- maybeAnswers.traverse(getExistingTailoringAnswers(ctx, _))
    } yield existingTailoringAnswers

  private def getExistingTailoringAnswers(ctx: JourneyContextWithNino, answers: JourneyAnswers)(implicit hc: HeaderCarrier) =
    for {
      category         <- getExpenseTailoringCategory(answers)
      tailoringAnswers <- getTailoringAnswers(ctx, answers, category.expensesCategories)
    } yield tailoringAnswers

  private def getDbAnswers(ctx: JourneyContextWithNino, journey: JourneyName): ApiResultT[Option[JourneyAnswers]] =
    repository.get(ctx.toJourneyContext(journey))

  private def getTailoringAnswers(ctx: JourneyContextWithNino, answers: JourneyAnswers, tailoringCategory: tailoring.ExpensesTailoring)(implicit
      hc: HeaderCarrier): ApiResultT[ExpensesTailoringAnswers] =
    tailoringCategory match {
      case NoExpenses           => getNoExpensesTailoring
      case IndividualCategories => getExpensesIndividualCategories(answers)
      case TotalAmount          => getExpenseTailoringAsOneTotal(ctx)
    }

  private def getExpenseTailoringCategory(answers: JourneyAnswers): ApiResultT[ExpensesCategoriesDb] =
    getPersistedAnswers(answers)

  private def getNoExpensesTailoring: ApiResultT[ExpensesTailoringAnswers] =
    EitherT.rightT[Future, ServiceError](ExpensesTailoringAnswers.NoExpensesAnswers: ExpensesTailoringAnswers)

  private def getExpensesIndividualCategories(answers: JourneyAnswers): ApiResultT[ExpensesTailoringAnswers] =
    getPersistedAnswers(answers)

  private def getExpenseTailoringAsOneTotal(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[ExpensesTailoringAnswers] =
    getAnswers[AsOneTotalAnswers](ctx).map(identity[ExpensesTailoringAnswers])

  def getGoodsToSellOrUseAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[GoodsToSellOrUseAnswers]] =
    for {
      dbTaxiAnswer <- getTaxiAnswer(ctx)
      gtsouAnswers <- getFullGoodsAnswers(ctx, dbTaxiAnswer)
    } yield gtsouAnswers

  private def getTaxiAnswer(ctx: JourneyContextWithNino): ApiResultT[Option[TaxiMinicabOrRoadHaulageDb]] =
    for {
      maybeData <- getDbAnswers(ctx, GoodsToSellOrUse)
      dbAnswer  <- getPersistedAnswers[TaxiMinicabOrRoadHaulageDb](maybeData)
    } yield dbAnswer

  private def getFullGoodsAnswers(ctx: JourneyContextWithNino, dbTaxiAnswer: Option[TaxiMinicabOrRoadHaulageDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[GoodsToSellOrUseAnswers]] = {
    val result = connector.getPeriodicSummaryDetail(ctx).map {
      case Right(periodicSummaryDetails) => dbTaxiAnswer.map(taxi => GoodsToSellOrUseAnswers(taxi, periodicSummaryDetails))
      case Left(_)                       => None
    }
    EitherT.liftF(result)
  }

  def getWorkplaceRunningCostsAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WorkplaceRunningCostsAnswers]] =
    for {
      dbAnswers   <- getWorkplaceRunningCostsDbAnswers(ctx)
      fullAnswers <- getFullWorkplaceRunningCostsAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getWorkplaceRunningCostsDbAnswers(ctx: JourneyContextWithNino): ApiResultT[Option[WorkplaceRunningCostsDb]] =
    for {
      maybeData <- getDbAnswers(ctx, WorkplaceRunningCosts)
      dbAnswers <- getPersistedAnswers[WorkplaceRunningCostsDb](maybeData)
    } yield dbAnswers

  private def getFullWorkplaceRunningCostsAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[WorkplaceRunningCostsDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[WorkplaceRunningCostsAnswers]] = {
    val result = connector.getPeriodicSummaryDetail(ctx).map {
      case Right(periodicSummaryDetails) => dbAnswers.map(answers => WorkplaceRunningCostsAnswers(answers, periodicSummaryDetails))
      case Left(_)                       => None
    }
    EitherT.liftF(result)
  }

  def deleteSimplifiedExpensesAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      existingPeriodicSummary <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      deductionsWithoutSimplifiedExpenses = existingPeriodicSummary.financials.toApi1894.deductions.map(_.copy(simplifiedExpenses = None))
      financialsWithoutSimplifiedExpenses = existingPeriodicSummary.financials.toApi1894.copy(deductions = deductionsWithoutSimplifiedExpenses)
      existingTaxTakenOffTradingIncome    = existingPeriodicSummary.financials.incomes.flatMap(_.taxTakenOffTradingIncome)
      _ <- submitTailoringAnswers(ctx, financialsWithoutSimplifiedExpenses, existingTaxTakenOffTradingIncome)
      _ <- repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(ExpensesTailoring))
    } yield ()

  def clearExpensesAndCapitalAllowancesData(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val updateExpensesPeriodicSummaries =
      connector.getPeriodicSummaryDetail(ctx).flatMap {
        case Left(error) if error.status == NOT_FOUND => Future(Right(()))
        case Left(error)                              => Future(Left(error))
        case Right(periodicSummary) =>
          val updatedFinancials = periodicSummary.financials.copy(deductions = None).toApi1895
          connector.amendSEPeriodSummary(AmendSEPeriodSummaryRequestData(ctx.taxYear, ctx.nino, ctx.businessId, updatedFinancials))
      }
    val updateCapitalAllowancesAnnualSummaries =
      connector.getAnnualSummaries(ctx).flatMap {
        case Left(error) if error.status == NOT_FOUND => Future(Right(()))
        case Left(error)                              => Future(Left(error))
        case Right(annualSummaries) =>
          val updatedAnnualSubmissionBody = annualSummaries.toRequestBody.copy(annualAllowances = None).replaceEmptyModelsWithNone
          connector.createUpdateOrDeleteApiAnnualSummaries(ctx, updatedAnnualSubmissionBody).value
      }

    for {
      _      <- EitherT(updateExpensesPeriodicSummaries)
      _      <- EitherT(updateCapitalAllowancesAnnualSummaries)
      _      <- repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(ExpensesTailoring), Option("expenses-"))
      result <- repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(CapitalAllowancesTailoring), Option("capital-allowances-"))
    } yield result
  }

  def clearExpensesData(ctx: JourneyContextWithNino, journeyName: JourneyName)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      existingPeriodicSummary <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      deductionsWithoutMaintenanceCosts = existingPeriodicSummary.financials.toApi1894.deductions.map(d => clearSpecificExpensesData(d, journeyName))
      financialsWithoutMaintenanceCosts = existingPeriodicSummary.financials.toApi1894.copy(deductions = deductionsWithoutMaintenanceCosts)
      existingTaxTakenOffTradingIncome  = existingPeriodicSummary.financials.incomes.flatMap(_.taxTakenOffTradingIncome)
      _ <- submitTailoringAnswers(ctx, financialsWithoutMaintenanceCosts, existingTaxTakenOffTradingIncome)
      _ <- repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(journeyName))
    } yield ()

  private[journeyAnswers] def clearSpecificExpensesData(deductions: Deductions, journeyName: JourneyName): Deductions = {
    val journeyDeductionsMap: Map[JourneyName, Deductions] = Map(
      OfficeSupplies             -> deductions.copy(adminCosts = None),
      GoodsToSellOrUse           -> deductions.copy(costOfGoods = None),
      RepairsAndMaintenanceCosts -> deductions.copy(maintenanceCosts = None),
      WorkplaceRunningCosts      -> deductions.copy(premisesRunningCosts = None),
      AdvertisingOrMarketing     -> deductions.copy(advertisingCosts = None),
      StaffCosts                 -> deductions.copy(staffCosts = None),
      Construction               -> deductions.copy(constructionIndustryScheme = None),
      ProfessionalFees           -> deductions.copy(professionalFees = None),
      IrrecoverableDebts         -> deductions.copy(badDebt = None),
      OtherExpenses              -> deductions.copy(other = None),
      FinancialCharges           -> deductions.copy(financialCharges = None),
      Interest                   -> deductions.copy(interest = None)
    )
    journeyDeductionsMap.getOrElse(journeyName, deductions)
  }

}
