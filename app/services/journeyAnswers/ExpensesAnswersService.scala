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
import connectors.SelfEmploymentBusinessConnector
import models.common.TaxYear.{endDate, startDate}
import models.common.{BusinessId, Mtditid, Nino, TaxYear}
import models.connector.api_1894.request.{CreateSEPeriodSummaryRequestBody, CreateSEPeriodSummaryRequestData, FinancialsType}
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.ExpensesTailoringAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait ExpensesAnswersService {
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: ExpensesTailoringAnswers): ApiResultT[Unit]
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, nino: Nino, answers: GoodsToSellOrUseJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class ExpensesAnswersServiceImpl @Inject() (businessConnector: SelfEmploymentBusinessConnector)(implicit ec: ExecutionContext)
    extends ExpensesAnswersService {
  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, answers: ExpensesTailoringAnswers): ApiResultT[Unit] =
    EitherT.right[ServiceError](Future.successful(()))

  def saveAnswers(businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, nino: Nino, answers: GoodsToSellOrUseJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val financials  = Json.toJson(answers).as[FinancialsType]
    val body        = CreateSEPeriodSummaryRequestBody(startDate(taxYear), endDate(taxYear), Some(financials))
    val requestData = CreateSEPeriodSummaryRequestData(taxYear, businessId, nino, body)
    val result      = businessConnector.createSEPeriodSummary(requestData).map(_ => ())

    EitherT.right[ServiceError](result)
  }

}
