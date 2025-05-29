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

package services.journeyAnswers.expenses

import cats.data.EitherT
import cats.implicits._
import connectors.IFS.IFSConnector
import models.common.JourneyContextWithNino
import models.connector.api_1895.request.{AmendSEPeriodSummaryRequestBody, AmendSEPeriodSummaryRequestData}
import models.database.expenses.travel.TravelExpensesDb
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.expenses.advertisingOrMarketing.AdvertisingOrMarketingJourneyAnswers
import models.frontend.expenses.construction.ConstructionJourneyAnswers
import models.frontend.expenses.depreciation.DepreciationCostsJourneyAnswers
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.financialCharges.FinancialChargesJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.interest.InterestJourneyAnswers
import models.frontend.expenses.irrecoverableDebts.IrrecoverableDebtsJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.otherExpenses.OtherExpensesJourneyAnswers
import models.frontend.expenses.professionalFees.ProfessionalFeesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import models.frontend.expenses.workplaceRunningCosts.WorkplaceRunningCostsJourneyAnswers
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PeriodSummaryService @Inject()(connector: IFSConnector)(implicit ec: ExecutionContext) {

  private def updatePeriodSummary(ctx: JourneyContextWithNino, updateBody: AmendSEPeriodSummaryRequestBody => AmendSEPeriodSummaryRequestBody)(
    implicit hc: HeaderCarrier): EitherT[Future, ServiceError, Unit] =
    for {
      existingPeriodicSummary <- EitherT(connector.getPeriodicSummaryDetail(ctx)).leftAs[ServiceError]
      financials = existingPeriodicSummary.financials.toApi1895
      data = AmendSEPeriodSummaryRequestData(ctx.taxYear, ctx.nino, ctx.businessId, updateBody(financials))
      _ <- EitherT(connector.amendSEPeriodSummary(data)).leftAs[ServiceError]
    } yield ()

  def saveTravelExpenses(ctx: JourneyContextWithNino,
                         answers: TravelExpensesDb)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateTravelExpenses(answers))

  def saveOfficeSuppliesAnswers(ctx: JourneyContextWithNino,
                                answers: OfficeSuppliesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateOfficeSupplies(answers))

  def saveGoodsToSell(ctx: JourneyContextWithNino,
                      answers: GoodsToSellOrUseJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateGoodsToSell(answers))

  def saveRepairsAndMaintenance(ctx: JourneyContextWithNino,
                                answers: RepairsAndMaintenanceCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateRepairsAndMaintenance(answers))

  def saveWorkplaceRunningCosts(ctx: JourneyContextWithNino,
                                answers: WorkplaceRunningCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateWorkplaceRunningCosts(answers))

  def saveAdvertisingOrMarketing(ctx: JourneyContextWithNino,
                                 answers: AdvertisingOrMarketingJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateAdvertisingOrMarketing(answers))

  def saveEntertainmentCosts(ctx: JourneyContextWithNino,
                             answers: EntertainmentJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateEntertainmentCosts(answers))

  def saveStaffCosts(ctx: JourneyContextWithNino,
                     answers: StaffCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateStaffCosts(answers))

  def saveConstructionIndustrySubcontractors(ctx: JourneyContextWithNino,
                                             answers: ConstructionJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateConstructionIndustrySubcontractors(answers))

  def saveProfessionalFees(ctx: JourneyContextWithNino,
                           answers: ProfessionalFeesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateProfessionalFees(answers))

  def saveFinancialCharges(ctx: JourneyContextWithNino,
                           answers: FinancialChargesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateFinancialCharges(answers))

  def saveBadDebts(ctx: JourneyContextWithNino,
                   answers: IrrecoverableDebtsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateBadDebts(answers))

  def saveDepreciationCosts(ctx: JourneyContextWithNino,
                            answers: DepreciationCostsJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateDepreciationCosts(answers))

  def saveOtherExpenses(ctx: JourneyContextWithNino,
                        answers: OtherExpensesJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateOtherExpenses(answers))

  def saveInterests(ctx: JourneyContextWithNino,
                    answers: InterestJourneyAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    updatePeriodSummary(ctx, _.updateInterest(answers))

}
