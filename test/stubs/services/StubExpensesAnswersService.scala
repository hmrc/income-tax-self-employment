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
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import stubs.serviceUnitT

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

  def getExpensesTailoringAnswers(): ApiResultT[Option[ExpensesTailoringAnswers]] =
    EitherT.rightT[Future, ServiceError](getTailoringJourneyAnswers)

  def getOptGoodsToSellOrUseAnswers(): ApiResultT[Option[GoodsToSellOrUseAnswers]] =
    EitherT.rightT[Future, ServiceError](getGoodsToSellOrUseAnswers)

  def getOptWorkplaceRunningCostsAnswers(): ApiResultT[Option[WorkplaceRunningCostsAnswers]] =
    EitherT.rightT[Future, ServiceError](getWorkplaceRunningCostsAnswers)

  def saveTailoringAnswers(): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def deleteSimplifiedExpensesAnswers(): ApiResultT[Unit] = deleteSimplifiedExpensesAnswersRes

  def clearExpensesAndCapitalAllowancesData(): ApiResultT[Unit] =
    clearExpensesAndCapitalAllowancesDataRes

  def clearExpensesData(): ApiResultT[Unit] = clearExpensesDataRes
}
