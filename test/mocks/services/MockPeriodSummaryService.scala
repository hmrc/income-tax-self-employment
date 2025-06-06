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
import models.database.expenses.travel.TravelExpensesDb
import models.domain.ApiResultT
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
import org.mockito.ArgumentMatchers.any
import org.mockito.stubbing.ScalaOngoingStubbing
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar.mock
import services.journeyAnswers.expenses.PeriodSummaryService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global


object MockPeriodSummaryService {

  val mockInstance: PeriodSummaryService = mock[PeriodSummaryService]


  def saveTravelExpenses(ctx: JourneyContextWithNino, answers: TravelExpensesDb)
                       (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveTravelExpenses(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveOfficeSuppliesAnswers(ctx: JourneyContextWithNino, answers: OfficeSuppliesJourneyAnswers)
                        (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveOfficeSuppliesAnswers(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveGoodsToSell(ctx: JourneyContextWithNino, answers: GoodsToSellOrUseJourneyAnswers)
                               (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveGoodsToSell(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveRepairsAndMaintenance(ctx: JourneyContextWithNino, answers: RepairsAndMaintenanceCostsJourneyAnswers)
                     (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveRepairsAndMaintenance(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveWorkplaceRunningCosts(ctx: JourneyContextWithNino, answers: WorkplaceRunningCostsJourneyAnswers)
                               (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveWorkplaceRunningCosts(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveAdvertisingOrMarketing(ctx: JourneyContextWithNino, answers: AdvertisingOrMarketingJourneyAnswers)
                               (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveAdvertisingOrMarketing(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveEntertainmentCosts(ctx: JourneyContextWithNino, answers: EntertainmentJourneyAnswers)
                                (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveEntertainmentCosts(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveStaffCosts(ctx: JourneyContextWithNino, answers: StaffCostsJourneyAnswers)
                            (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveStaffCosts(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveConstructionIndustrySubcontractors(ctx: JourneyContextWithNino, answers: ConstructionJourneyAnswers)
                    (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveConstructionIndustrySubcontractors(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveProfessionalFees(ctx: JourneyContextWithNino, answers: ProfessionalFeesJourneyAnswers)
                                            (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveProfessionalFees(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveFinancialCharges(ctx: JourneyContextWithNino, answers: FinancialChargesJourneyAnswers)
                          (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveFinancialCharges(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveBadDebts(ctx: JourneyContextWithNino, answers: IrrecoverableDebtsJourneyAnswers)
                          (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveBadDebts(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveDepreciationCosts(ctx: JourneyContextWithNino, answers: DepreciationCostsJourneyAnswers)
                  (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveDepreciationCosts(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveOtherExpenses(ctx: JourneyContextWithNino, answers: OtherExpensesJourneyAnswers)
                           (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveOtherExpenses(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

  def saveInterests(ctx: JourneyContextWithNino, answers: InterestJourneyAnswers)
                       (returnValue: ApiResultT[Unit]): ScalaOngoingStubbing[ApiResultT[Unit]] = {
    when(mockInstance.saveInterests(eqTo(ctx), eqTo(answers))(any[HeaderCarrier]))
      .thenReturn(EitherT.rightT(returnValue))
  }

}
