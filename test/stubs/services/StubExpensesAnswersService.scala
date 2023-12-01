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

package stubs.services

import models.common.{BusinessId, Mtditid, Nino, TaxYear}
import models.domain.ApiResultT
import models.frontend.expenses.ExpensesTailoringAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import services.journeyAnswers.ExpensesAnswersService
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

case class StubExpensesAnswersService(expensesTailoringAnswersRes: ApiResultT[Unit] = serviceUnitT,
                                      goodsToSellOrUseAnswersRes: ApiResultT[Unit] = serviceUnitT)
    extends ExpensesAnswersService {
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: ExpensesTailoringAnswers): ApiResultT[Unit] =
    expensesTailoringAnswersRes

  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, nino: Nino, answers: GoodsToSellOrUseJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = goodsToSellOrUseAnswersRes
}