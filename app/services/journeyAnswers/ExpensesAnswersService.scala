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
import models.common.JourneyName.ExpensesTailoring
import models.common.TaxYear.{endDate, startDate}
import models.common._
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType}
import models.connector.{Api1786ExpensesResponseParser, Api1894DeductionsBuilder}
import models.database.JourneyAnswers
import models.database.expenses.ExpensesCategoriesDb
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.tailoring
import models.frontend.expenses.tailoring.ExpensesTailoring.{IndividualCategories, NoExpenses, TotalAmount}
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers._
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ExpensesAnswersService {
  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit]
  def saveAnswers[A: Api1894DeductionsBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getAnswers[A: Api1786ExpensesResponseParser](ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[A]
  def getExpensesTailoringAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ExpensesTailoringAnswers]]
}

@Singleton
class ExpensesAnswersServiceImpl @Inject() (connector: SelfEmploymentConnector, repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends ExpensesAnswersService {

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] =
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))

  def saveAnswers[A: Api1894DeductionsBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val financials  = FinancialsType.fromFrontendModel(answers)
    val body        = CreateSEPeriodSummaryRequestBody(startDate(ctx.taxYear), endDate(ctx.taxYear), Some(financials))
    val requestData = CreateSEPeriodSummaryRequestData(ctx.taxYear, ctx.businessId, ctx.nino, body)

    val result = connector
      .createSEPeriodSummary(requestData)
      .map(_ => ())

    EitherT.right[ServiceError](result)
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
      maybeAnswers             <- getExpenseTailoringDbAnswers(ctx)
      existingTailoringAnswers <- maybeAnswers.traverse(getExistingTailoringAnswers(ctx, _))
    } yield existingTailoringAnswers

  private def getExistingTailoringAnswers(ctx: JourneyContextWithNino, answers: JourneyAnswers)(implicit hc: HeaderCarrier) =
    for {
      category         <- getExpenseTailoringCategory(answers)
      tailoringAnswers <- getTailoringAnswers(ctx, answers, category.expensesCategories)
    } yield tailoringAnswers

  private def getExpenseTailoringDbAnswers(ctx: JourneyContextWithNino): ApiResultT[Option[JourneyAnswers]] =
    repository.get(ctx.toJourneyContext(ExpensesTailoring))

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

}
