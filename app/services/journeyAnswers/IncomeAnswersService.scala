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
import connectors.SelfEmploymentConnector
import models.common.JourneyName.{ExpensesTailoring, Income}
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1802.request._
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType, IncomesType}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Deductions, Incomes}
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.database.income.IncomeStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.income.IncomeJourneyAnswers
import models.frontend.income.TradingAllowance.{DeclareExpenses, UseTradingAllowance}
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait IncomeAnswersService {
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeJourneyAnswers]]
  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class IncomeAnswersServiceImpl @Inject() (repository: JourneyAnswersRepository, connector: SelfEmploymentConnector)(implicit ec: ExecutionContext)
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

  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    import ctx._
    val storageAnswers = IncomeStorageAnswers.fromJourneyAnswers(answers)

    val upsertBody = CreateAmendSEAnnualSubmissionRequestBody.mkRequest(
      annualAdjustments = Some(
        AnnualAdjustments.empty.copy(includedNonTaxableProfits = answers.notTaxableAmount, outstandingBusinessIncome = answers.otherIncomeAmount)),
      annualAllowances = Some(AnnualAllowances.empty.copy(tradingIncomeAllowance = answers.tradingAllowanceAmount)),
      annualNonFinancials = None
    )
    val maybeUpsertData = upsertBody.map(CreateAmendSEAnnualSubmissionRequestData(taxYear, nino, businessId, _))

    val result = for {
      response <- EitherT(connector.listSEPeriodSummary(ctx)).leftAs[ServiceError]
      _        <- upsertPeriodSummary(response, ctx, answers)
      _        <- maybeUpsertData.traverse(upsertData => EitherT(connector.createAmendSEAnnualSubmission(upsertData))).leftAs[ServiceError]
      _        <- repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, Income), Json.toJson(storageAnswers))
      _        <- maybeDeleteExpenses(ctx, answers)
    } yield ()

    result.leftAs[ServiceError]
  }

  private def maybeDeleteExpenses(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers): EitherT[Future, ServiceError, Unit] =
    answers.tradingAllowance match {
      case UseTradingAllowance => repository.deleteOneOrMoreJourneys(ctx.toJourneyContext(ExpensesTailoring), Some("expenses-"))
      case DeclareExpenses     => EitherT.rightT[Future, ServiceError](())
    }

  private def upsertPeriodSummary(response: ListSEPeriodSummariesResponse, ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit
      hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] = {
    import ctx._

    val noSubmissionExists = response.periods.forall(_.isEmpty)

    val result = if (noSubmissionExists) {
      val createIncome = IncomesType(answers.turnoverIncomeAmount.some, answers.nonTurnoverIncomeAmount)
      val createBody   = CreateSEPeriodSummaryRequestBody(startDate(taxYear), endDate(taxYear), Some(FinancialsType(createIncome.some, None)))
      val createData   = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, createBody)

      EitherT(connector.createSEPeriodSummary(createData)).leftAs[ServiceError].void
    } else {
      val amendIncome = Incomes(
        turnover = answers.turnoverIncomeAmount.some,
        other = answers.nonTurnoverIncomeAmount,
        taxTakenOffTradingIncome = None
      )

      for {
        periodicSummaryDetails <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
        updatedDeductions = answers.tradingAllowance match {
          case UseTradingAllowance => None
          case DeclareExpenses     => periodicSummaryDetails.financials.deductions
        }
        amendBody = AmendSEPeriodSummaryRequestBody(amendIncome.some, updatedDeductions.map(Deductions.fromApi1786))
        amendData = AmendSEPeriodSummaryRequestData(taxYear, nino, businessId, amendBody)
        _ <- EitherT(connector.amendSEPeriodSummary(amendData)).leftAs[ServiceError]
      } yield ()
    }

    result
  }
}
