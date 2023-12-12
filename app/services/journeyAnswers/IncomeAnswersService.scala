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
import models.common.JourneyName.Income
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1802.request._
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType, IncomesType}
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData, Incomes}
import models.connector.api_1965.ListSEPeriodSummariesResponse
import models.database.income.IncomeStorageAnswers
import models.domain.ApiResultT
import models.error.{DownstreamError, ServiceError}
import models.frontend.income.IncomeJourneyAnswers
import models.frontend.income.IncomeJourneyAnswers.fromJourneyAnswers
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import services.mapDownstreamErrors
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait IncomeAnswersService {
  def getAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid): ApiResultT[Option[IncomeJourneyAnswers]]
  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class IncomeAnswersServiceImpl @Inject() (repository: JourneyAnswersRepository, connector: SelfEmploymentConnector)(implicit ec: ExecutionContext)
    extends IncomeAnswersService {

  def getAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid): ApiResultT[Option[IncomeJourneyAnswers]] =
    for {
      row     <- EitherT.right[ServiceError](repository.get(JourneyContext(taxYear, businessId, mtditid, Income)))
      answers <- EitherT.fromEither[Future](fromJourneyAnswers(row))
    } yield answers

  def saveAnswers(ctx: JourneyContextWithNino, answers: IncomeJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    import ctx._
    val storageAnswers = IncomeStorageAnswers.fromJourneyAnswers(answers)

    val createIncome = IncomesType(answers.turnoverIncomeAmount.some, answers.nonTurnoverIncomeAmount)
    val createBody   = CreateSEPeriodSummaryRequestBody(startDate(taxYear), endDate(taxYear), Some(FinancialsType(createIncome.some, None)))
    val createData   = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, createBody)

    val amendIncome = Incomes(answers.turnoverIncomeAmount.some, answers.nonTurnoverIncomeAmount, None)
    val amendBody   = AmendSEPeriodSummaryRequestBody(amendIncome.some, None)
    val amendData   = AmendSEPeriodSummaryRequestData(taxYear, nino, businessId, amendBody)

    val upsertBody = CreateAmendSEAnnualSubmissionRequestBody(
      Some(AnnualAdjustments.empty.copy(includedNonTaxableProfits = answers.notTaxableAmount, outstandingBusinessIncome = answers.otherIncomeAmount)),
      Some(AnnualAllowances.empty.copy(tradingIncomeAllowance = answers.tradingAllowanceAmount)),
      None
    )
    val upsertData = CreateAmendSEAnnualSubmissionRequestData(taxYear, nino, businessId, upsertBody)

    val result = for {
      _ <- EitherT.right[DownstreamError](repository.upsertData(JourneyContext(taxYear, businessId, mtditid, Income), Json.toJson(storageAnswers)))
      response <- EitherT(connector.listSEPeriodSummary(ctx))
      _ <-
        if (noSubmissionExists(response)) EitherT(connector.createSEPeriodSummary(createData)) else EitherT(connector.amendSEPeriodSummary(amendData))
      _ <- EitherT(connector.createAmendSEAnnualSubmission(upsertData))
    } yield ()

    result.leftMap(mapDownstreamErrors)
  }

  private def noSubmissionExists(response: ListSEPeriodSummariesResponse) =
    response.periods.forall(_.isEmpty)

}
