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

package stubs.services

import cats.data.EitherT
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StubExpensesAnswersService(expensesSaveTailoringAnswersRes: ApiResultT[Unit] = serviceUnitT,
                                      expensesSaveAnswersRes: ApiResultT[Unit] = serviceUnitT,
                                      expensesGetAnswersRes: ApiResultT[AnyRef] = EitherT.right[ServiceError](Future.successful("unused")),
                                      getTailoringJourneyAnswers: Option[ExpensesTailoringAnswers] = None,
                                      getGoodsToSellOrUseAnswers: Option[GoodsToSellOrUseAnswers] = None,
                                      getWorkplaceRunningCostsAnswers: Option[WorkplaceRunningCostsAnswers] = None,
                                      deleteSimplifiedExpensesAnswersRes: ApiResultT[Unit] = serviceUnitT,
                                      clearExpensesAndCapitalAllowancesDataRes: ApiResultT[Unit] = serviceUnitT,
                                      clearExpensesDataRes: ApiResultT[Unit] = serviceUnitT) {

  def getExpensesTailoringAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ExpensesTailoringAnswers]] =
    EitherT.rightT[Future, ServiceError](getTailoringJourneyAnswers)

  def getGoodsToSellOrUseAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[GoodsToSellOrUseAnswers]] =
    EitherT.rightT[Future, ServiceError](getGoodsToSellOrUseAnswers)

  def getWorkplaceRunningCostsAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WorkplaceRunningCostsAnswers]] =
    EitherT.rightT[Future, ServiceError](getWorkplaceRunningCostsAnswers)

  def saveTailoringAnswers(ctx: JourneyContextWithNino, answers: ExpensesTailoringAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def deleteSimplifiedExpensesAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] = deleteSimplifiedExpensesAnswersRes

  def clearExpensesAndCapitalAllowancesData(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    clearExpensesAndCapitalAllowancesDataRes

  def clearExpensesData(ctx: JourneyContextWithNino, journeyName: JourneyName)(implicit hc: HeaderCarrier): ApiResultT[Unit] = clearExpensesDataRes
}
