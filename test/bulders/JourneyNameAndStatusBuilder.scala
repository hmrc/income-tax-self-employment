package bulders

import models.common.JourneyName._
import models.common.JourneyStatus.Completed
import models.domain.JourneyStatusAndLink
import utils.BaseSpec.{businessId, taxYear}

object JourneyNameAndStatusBuilder {

  val expensesJourneysListCompleted = List(
    JourneyStatusAndLink(ExpensesTailoring, Completed, ExpensesTailoring.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(GoodsToSellOrUse, Completed, GoodsToSellOrUse.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(WorkplaceRunningCosts, Completed, WorkplaceRunningCosts.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(RepairsAndMaintenanceCosts, Completed, RepairsAndMaintenanceCosts.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(AdvertisingOrMarketing, Completed, AdvertisingOrMarketing.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(OfficeSupplies, Completed, OfficeSupplies.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(Entertainment, Completed, Entertainment.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(StaffCosts, Completed, StaffCosts.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(Construction, Completed, Construction.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(ProfessionalFees, Completed, ProfessionalFees.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(Interest, Completed, Interest.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(OtherExpenses, Completed, OtherExpenses.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(FinancialCharges, Completed, FinancialCharges.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(IrrecoverableDebts, Completed, IrrecoverableDebts.getHref(taxYear, businessId, true)),
    JourneyStatusAndLink(Depreciation, Completed, Depreciation.getHref(taxYear, businessId, true))
  )

  val commonTaskListData = Seq(
    List(
      JourneyStatusAndLink(TradeDetails, Completed, TradeDetails.getHref(taxYear, businessId)),
      JourneyStatusAndLink(SelfEmploymentAbroad, Completed, SelfEmploymentAbroad.getHref(taxYear, businessId, true)),
      JourneyStatusAndLink(Income, Completed, Income.getHref(taxYear, businessId, true))
    ) ++ expensesJourneysListCompleted,
    List()
  )

}
