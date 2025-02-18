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

package services.journeyAnswers

import cats.data.EitherT
import cats.implicits._
import connectors.IFSConnector
import models.audit.AuditTradingAllowance
import models.common.JourneyName.{ExpensesTailoring, Income}
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1786.SuccessResponseSchema
import models.connector.api_1802.request._
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType, IncomesType}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Incomes}
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.database.income.IncomeStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.income.IncomeJourneyAnswers
import models.frontend.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import services.{AuditService, BusinessService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait IncomeAnswersService {
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeJourneyAnswers]]
  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class IncomeAnswersServiceImpl @Inject() (repository: JourneyAnswersRepository,
                                          connector: IFSConnector,
                                          auditService: AuditService,
                                          businessService: BusinessService)(implicit ec: ExecutionContext)
    extends IncomeAnswersService {

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeJourneyAnswers]] =
    for {
      maybeDbAnswers <- getDbAnswers(ctx)
      incomeAnswers <- maybeDbAnswers.traverse { journeyAnswers =>
        for {
          periodicSummaryDetails <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
          annualSummaries        <- EitherT(connector.getAnnualSummaries(ctx)).leftAs[ServiceError]
        } yield IncomeJourneyAnswers(journeyAnswers, periodicSummaryDetails, annualSummaries)
      }
    } yield incomeAnswers

  private def getDbAnswers(ctx: JourneyContextWithNino): EitherT[Future, ServiceError, Option[IncomeStorageAnswers]] =
    for {
      row            <- repository.get(ctx.toJourneyContext(Income))
      maybeDbAnswers <- getPersistedAnswers[IncomeStorageAnswers](row)
    } yield maybeDbAnswers

  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _               <- submitAnnualSummaries(ctx, answers)
      periodSummaries <- EitherT(connector.listSEPeriodSummary(ctx)).leftAs[ServiceError]
      _               <- upsertPeriodSummary(periodSummaries, ctx, answers)
      storageAnswers = IncomeStorageAnswers.fromJourneyAnswers(answers)
      _ <- repository.upsertAnswers(ctx.toJourneyContext(Income), Json.toJson(storageAnswers))
      _ <- maybeDeleteExpenses(ctx, answers)
    } yield ()

  private def submitAnnualSummaries(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] = {
    val submissionBody: Future[Either[ServiceError, Option[CreateAmendSEAnnualSubmissionRequestBody]]] = for {
      maybeAnnualSummaries <- connector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[IncomeStorageAnswers](maybeAnnualSummaries, answers)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(createUpdateDeleteAnnualSummaryAndLogEvent(ctx, answers, _))
  }

  private def createUpdateDeleteAnnualSummaryAndLogEvent(ctx: JourneyContextWithNino,
                                                         answers: IncomeJourneyAnswers,
                                                         x: Option[CreateAmendSEAnnualSubmissionRequestBody])(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val result = connector.createUpdateOrDeleteApiAnnualSummaries(ctx, x)
    businessService.getBusiness(ctx.nino, ctx.businessId).map(_.tradingName) map { businessName =>
      auditService.sendAuditEvent(AuditTradingAllowance.auditType, AuditTradingAllowance.apply(ctx, businessName, answers))
    }
    result
  }

  private def maybeDeleteExpenses(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers): EitherT[Future, ServiceError, Unit] =
    answers.tradingAllowance match {
      case UseTradingAllowance => repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(ExpensesTailoring), Option("expenses-"))
      case DeclareExpenses     => EitherT.rightT[Future, ServiceError](())
    }

  private def upsertPeriodSummary(response: ListSEPeriodSummariesResponse, ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] = {

    val submissionPeriod = response.periods.getOrElse(Nil).find { period =>
      period.from.contains(ctx.taxYear.fromAnnualPeriod) && period.to.contains(ctx.taxYear.toAnnualPeriod)
    }

    if (submissionPeriod.isEmpty) {
      createSEPeriod(ctx, answers)
    } else {
      updateSEPeriod(ctx, answers)
    }
  }

  private def createSEPeriod(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] = {
    val createIncome = IncomesType(answers.turnoverIncomeAmount.some, answers.nonTurnoverIncomeAmount)
    val createBody   = CreateSEPeriodSummaryRequestBody(startDate(ctx.taxYear), endDate(ctx.taxYear), Option(FinancialsType(createIncome.some, None)))
    val createData   = CreateSEPeriodSummaryRequestData(ctx.taxYear, ctx.businessId, ctx.nino, createBody)

    EitherT(connector.createSEPeriodSummary(createData)).leftAs[ServiceError].void
  }

  private def updateSEPeriod(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] =
    for {
      periodicSummaryDetails <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      _                      <- updateSEPeriodSummary(ctx, periodicSummaryDetails, answers)
    } yield ()

  private def updateSEPeriodSummary(ctx: JourneyContextWithNino, periodicSummaryDetails: SuccessResponseSchema, answers: IncomeJourneyAnswers)(
      implicit hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] = {
    def createIncome(taxTakenOffTradingIncome: Option[BigDecimal]) = Incomes(
      turnover = answers.turnoverIncomeAmount.some,
      other = answers.nonTurnoverIncomeAmount,
      taxTakenOffTradingIncome = taxTakenOffTradingIncome // TODO we don't set it on our UI - Confirm what to do with this field SASS-9555
    )

    val taxTakenOffTradingIncome = periodicSummaryDetails.financials.incomes.flatMap(_.taxTakenOffTradingIncome)
    val updatedIncomes           = createIncome(taxTakenOffTradingIncome)
    val updatedDeductions = answers.tradingAllowance match {
      case UseTradingAllowance => None
      case DeclareExpenses     => periodicSummaryDetails.financials.deductions
    }

    val amendBody = AmendSEPeriodSummaryRequestBody(
      incomes = Option(updatedIncomes),
      deductions = updatedDeductions.map(_.toApi1895)
    )

    val amendData = AmendSEPeriodSummaryRequestData(ctx.taxYear, ctx.nino, ctx.businessId, amendBody)
    EitherT(connector.amendSEPeriodSummary(amendData)).leftAs[ServiceError]
  }
}
