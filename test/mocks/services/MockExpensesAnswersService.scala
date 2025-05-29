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

package mocks.services

import cats.data.EitherT
import models.common._
import models.connector.Api1786ExpensesResponseParser
import models.domain.ApiResultT
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsAnswers
import org.mockito.stubbing.ScalaOngoingStubbing
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.Writes
import services.journeyAnswers.expenses.ExpensesAnswersService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

object MockExpensesAnswersService {

  val mockInstance: ExpensesAnswersService = mock[ExpensesAnswersService]

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)
                    (returnValue: ApiResultT[Unit])(implicit writes: Writes[A]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.persistAnswers(eqTo(businessId), eqTo(taxYear), eqTo(mtditid), eqTo(journey), eqTo(answers)))
      .thenReturn(returnValue)
  }

  def saveTailoringAnswers(ctx: JourneyContextWithNino, answers: ExpensesTailoringAnswers)
                              (returnValue: ApiResultT[Unit])(implicit hc: HeaderCarrier): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveTailoringAnswers(eqTo(ctx), eqTo(answers)))
      .thenReturn(returnValue)
  }

  def getAnswers[A: Api1786ExpensesResponseParser](ctx: JourneyContextWithNino)
  (returnValue: ApiResultT[A])(implicit hc: HeaderCarrier): ScalaOngoingStubbing[ApiResultT[A]] = {
    when(mockInstance.getAnswers[A](eqTo(ctx)))
      .thenReturn(returnValue)
  }

  def getExpensesTailoringAnswers(ctx: JourneyContextWithNino)
                                 (returnValue: ApiResultT[Option[ExpensesTailoringAnswers]]
                                 )(implicit hc: HeaderCarrier): ScalaOngoingStubbing[ApiResultT[Option[ExpensesTailoringAnswers]]] = {
    when(mockInstance.getExpensesTailoringAnswers(eqTo(ctx)))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def getGoodsToSellOrUseAnswers(ctx: JourneyContextWithNino)
                             (returnValue: ApiResultT[Option[GoodsToSellOrUseAnswers]]
                             )(implicit hc: HeaderCarrier): ScalaOngoingStubbing[ApiResultT[Option[GoodsToSellOrUseAnswers]]] = {
    when(mockInstance.getGoodsToSellOrUseAnswers(eqTo(ctx)))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def getWorkplaceRunningCostsAnswers(ctx: JourneyContextWithNino)
                                     (returnValue: ApiResultT[Option[WorkplaceRunningCostsAnswers]]
                                     )(implicit hc: HeaderCarrier): ScalaOngoingStubbing[ApiResultT[Option[WorkplaceRunningCostsAnswers]]] = {
    when(mockInstance.getWorkplaceRunningCostsAnswers(eqTo(ctx)))
      .thenReturn(EitherT.rightT(returnValue))
  }

}
