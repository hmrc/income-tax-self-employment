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

import cats.data.EitherT
import models.common._
import models.connector.Api1786ExpensesResponseParser
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.{GoodsToSellOrUseAnswers, GoodsToSellOrUseJourneyAnswers}
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.tailoring.ExpensesTailoringAnswers
import models.frontend.expenses.workplaceRunningCosts.{WorkplaceRunningCostsAnswers, WorkplaceRunningCostsJourneyAnswers}
import play.api.libs.json.Writes
import services.journeyAnswers.ExpensesAnswersService
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
                                      clearOfficeSuppliesExpensesDataRes: ApiResultT[Unit] = serviceUnitT,
                                      clearRepairsAndMaintenanceExpensesDataRes: ApiResultT[Unit] = serviceUnitT)
    extends ExpensesAnswersService {

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] =
    expensesSaveTailoringAnswersRes

  def getAnswers[A: Api1786ExpensesResponseParser](ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[A] =
    expensesGetAnswersRes.map(_.asInstanceOf[A])

  def getExpensesTailoringAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ExpensesTailoringAnswers]] =
    EitherT.rightT[Future, ServiceError](getTailoringJourneyAnswers)

  def getGoodsToSellOrUseAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[GoodsToSellOrUseAnswers]] =
    EitherT.rightT[Future, ServiceError](getGoodsToSellOrUseAnswers)

  def getWorkplaceRunningCostsAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WorkplaceRunningCostsAnswers]] =
    EitherT.rightT[Future, ServiceError](getWorkplaceRunningCostsAnswers)

  def saveTailoringAnswers(ctx: JourneyContextWithNino, answers: ExpensesTailoringAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveOfficeSuppliesAnswers(ctx: JourneyContextWithNino, answers: OfficeSuppliesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveGoodsToSell(ctx: JourneyContextWithNino, answers: GoodsToSellOrUseJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveRepairsAndMaintenance(ctx: JourneyContextWithNino, answers: RepairsAndMaintenanceCostsJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveWorkplaceRunningCosts(ctx: JourneyContextWithNino, answers: WorkplaceRunningCostsJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveAdvertisingOrMarketing(ctx: JourneyContextWithNino, answers: AdvertisingOrMarketingJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveEntertainmentCosts(ctx: JourneyContextWithNino, answers: EntertainmentJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveStaffCosts(ctx: JourneyContextWithNino, answers: StaffCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveConstructionIndustrySubcontractors(ctx: JourneyContextWithNino, answers: ConstructionJourneyAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveProfessionalFees(ctx: JourneyContextWithNino, answers: ProfessionalFeesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveFinancialCharges(ctx: JourneyContextWithNino, answers: FinancialChargesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveBadDebts(ctx: JourneyContextWithNino, answers: IrrecoverableDebtsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveDepreciationCosts(ctx: JourneyContextWithNino, answers: DepreciationCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveOtherExpenses(ctx: JourneyContextWithNino, answers: OtherExpensesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def saveInterests(ctx: JourneyContextWithNino, answers: InterestJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    expensesSaveAnswersRes

  def deleteSimplifiedExpensesAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] = deleteSimplifiedExpensesAnswersRes

  def clearExpensesAndCapitalAllowancesData(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    clearExpensesAndCapitalAllowancesDataRes

  def clearOfficeSuppliesExpensesData(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    clearOfficeSuppliesExpensesDataRes

  def clearRepairsAndMaintenanceExpensesData(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    clearRepairsAndMaintenanceExpensesDataRes

}
