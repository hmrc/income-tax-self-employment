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
import connectors.SelfEmploymentBusinessConnector
import models.common.JourneyAnswersContext.{JourneyContext, JourneyContextWithNino}
import models.common.JourneyName.ExpensesTailoring
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.ExpensesTailoringAnswers
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DeductionsBuilder

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait ExpensesAnswersService {
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: ExpensesTailoringAnswers): ApiResultT[Unit]
  def saveAnswers[A: DeductionsBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class ExpensesAnswersServiceImpl @Inject() (businessConnector: SelfEmploymentBusinessConnector, repository: JourneyAnswersRepository)(implicit
    ec: ExecutionContext)
    extends ExpensesAnswersService {

  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: ExpensesTailoringAnswers): ApiResultT[Unit] =
    EitherT
      .right[ServiceError](repository.upsertData(JourneyContext(taxYear, businessId, mtditid, ExpensesTailoring), Json.toJson(answers)))
      .void

  def saveAnswers[A: DeductionsBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val financials  = FinancialsType.fromFrontendModel(answers)
    val body        = CreateSEPeriodSummaryRequestBody(startDate(ctx.taxYear), endDate(ctx.taxYear), Some(financials))
    val requestData = CreateSEPeriodSummaryRequestData(ctx.taxYear, ctx.businessId, ctx.nino, body)

    val result = businessConnector
      .createSEPeriodSummary(requestData)
      .map(_ => ())

    EitherT.right[ServiceError](result)
  }

}
