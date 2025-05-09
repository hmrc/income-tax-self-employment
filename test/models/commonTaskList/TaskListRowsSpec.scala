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

package models.commonTaskList

import data.CommonTestData
import models.common.JourneyName._
import models.common.JourneyStatus.{Completed, InProgress}
import models.domain.JourneyNameAndStatus
import models.frontend.capitalAllowances.CapitalAllowances
import models.frontend.expenses.tailoring.individualCategories.{
  ProfessionalServiceExpenses,
  AdvertisingOrMarketing => AdvertisingOrMarketingAnswer,
  GoodsToSellOrUse => GoodsToSellOrUseAnswer,
  OfficeSupplies => OfficeSuppliesAnswer,
  OtherExpenses => OtherExpensesAnswer,
  RepairsAndMaintenance => RepairsAndMaintenanceAnswer,
  TravelForWork => TravelForWorkAnswer,
  WorkFromBusinessPremises => WorkFromBusinessPremisesAnswer
}
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.BaseSpec

class TaskListRowsSpec extends BaseSpec with CommonTestData with GuiceOneAppPerSuite with Matchers {

  override lazy val app: Application = GuiceApplicationBuilder().build()

  private val rows = app.injector.instanceOf[TaskListRows]

  "trade details row" must {
    "be a static row (display regardless of status or conditions)" in {
      val result = rows.tradeDetailsRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = TradeDetails.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/check")
      )
    }

    "display in progress" in {
      val statuses = Seq(JourneyNameAndStatus(TradeDetails, InProgress))

      val result = rows.tradeDetailsRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = TradeDetails.entryName,
        status = TaskStatus.InProgress(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/check")
      )
    }

    "display completed" in {
      val statuses = Seq(JourneyNameAndStatus(TradeDetails, Completed))

      val result = rows.tradeDetailsRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = TradeDetails.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/check")
      )
    }
  }

  "industry sectors row" must {
    "display with CannotStartYet status when prerequisites aren't met" in {
      val result = rows.industrySectorsRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = SelfEmploymentAbroad.entryName,
        status = TaskStatus.CannotStartYet(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/self-employment-abroad")
      )
    }

    "display with NotStarted status when prerequisites are met" in {
      val statuses = Seq(JourneyNameAndStatus(TradeDetails, Completed))

      val result = rows.industrySectorsRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = SelfEmploymentAbroad.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/self-employment-abroad")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(TradeDetails, Completed),
        JourneyNameAndStatus(SelfEmploymentAbroad, Completed)
      )

      val result = rows.industrySectorsRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = SelfEmploymentAbroad.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/details/self-employment-abroad/check")
      )
    }
  }

  "the income row" must {
    "display with CannotStartYet status when prerequisites aren't met" in {
      val result = rows.incomeRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Income.entryName,
        status = TaskStatus.CannotStartYet(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/income/not-counted-turnover")
      )
    }

    "use the CYA URL when the section is completed and prerequisites are met" in {
      val statuses = Seq(
        JourneyNameAndStatus(SelfEmploymentAbroad, Completed),
        JourneyNameAndStatus(Income, Completed)
      )

      val result = rows.incomeRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Income.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/income/check-your-income")
      )
    }
  }

  "the expenses categories row" must {
    "display with CannotStartYet status when prerequisites aren't met" in {
      val result = rows.expenseCategoriesRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ExpensesTailoring.entryName,
        status = TaskStatus.CannotStartYet(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses")
      )
    }

    "display with NotStarted status when prerequisites are met" in {
      val statuses = Seq(JourneyNameAndStatus(SelfEmploymentAbroad, Completed))

      val result = rows.expenseCategoriesRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ExpensesTailoring.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(SelfEmploymentAbroad, Completed),
        JourneyNameAndStatus(ExpensesTailoring, Completed)
      )

      val result = rows.expenseCategoriesRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ExpensesTailoring.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/check")
      )
    }
  }

  "the office supplies row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.officeSuppliesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.officeSuppliesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(officeSupplies = OfficeSuppliesAnswer.YesAllowable)

      val result = rows.officeSuppliesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = OfficeSupplies.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/office-supplies/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(OfficeSupplies, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(officeSupplies = OfficeSuppliesAnswer.YesAllowable)

      val result = rows.officeSuppliesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = OfficeSupplies.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/office-supplies/check")
      )
    }
  }

  "the goods to sell or use row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.goodsToSellOrUseRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.goodsToSellOrUseRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(goodsToSellOrUse = GoodsToSellOrUseAnswer.YesAllowable)

      val result = rows.goodsToSellOrUseRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = GoodsToSellOrUse.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/goods-sell-use/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(GoodsToSellOrUse, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(goodsToSellOrUse = GoodsToSellOrUseAnswer.YesAllowable)

      val result = rows.goodsToSellOrUseRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = GoodsToSellOrUse.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/goods-sell-use/check")
      )
    }
  }

  "the repairs and maintenance costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.repairsAndMaintenanceCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.repairsAndMaintenanceCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(repairsAndMaintenance = RepairsAndMaintenanceAnswer.YesAllowable)

      val result = rows.repairsAndMaintenanceCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = RepairsAndMaintenanceCosts.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/repairs-maintenance/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(RepairsAndMaintenanceCosts, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(repairsAndMaintenance = RepairsAndMaintenanceAnswer.YesAllowable)

      val result = rows.repairsAndMaintenanceCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = RepairsAndMaintenanceCosts.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/repairs-maintenance/check")
      )
    }
  }

  "the workplace running costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.workplaceRunningCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.workplaceRunningCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the 'work from home' display condition is met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(workFromHome = true)

      val result = rows.workplaceRunningCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = WorkplaceRunningCosts.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/workplace-running-costs/working-from-home/more-than-25-hours")
      )
    }

    "display with NotStarted status when prerequisites and the 'business premises' display condition is met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(workFromBusinessPremises = WorkFromBusinessPremisesAnswer.YesAllowable)

      val result = rows.workplaceRunningCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = WorkplaceRunningCosts.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/workplace-running-costs/working-from-home/more-than-25-hours")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(WorkplaceRunningCosts, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(workFromBusinessPremises = WorkFromBusinessPremisesAnswer.YesAllowable)

      val result = rows.workplaceRunningCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = WorkplaceRunningCosts.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/workplace-running-costs/workplace-running-costs/check")
      )
    }
  }

  "the advertising and marketing costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.advertisingOrMarketingRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.advertisingOrMarketingRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(advertisingOrMarketing = AdvertisingOrMarketingAnswer.YesAllowable)

      val result = rows.advertisingOrMarketingRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = AdvertisingOrMarketing.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/advertising-marketing/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(AdvertisingOrMarketing, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(advertisingOrMarketing = AdvertisingOrMarketingAnswer.YesAllowable)

      val result = rows.advertisingOrMarketingRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = AdvertisingOrMarketing.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/advertising-marketing/check")
      )
    }
  }

  "the travel costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.travelCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.travelCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(travelForWork = TravelForWorkAnswer.YesAllowable)

      val result = rows.travelCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = TravelExpenses.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/travel-costs/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(TravelExpenses, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(travelForWork = TravelForWorkAnswer.YesAllowable)

      val result = rows.travelCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = TravelExpenses.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/travel-costs/check")
      )
    }
  }

  "the entertainment costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.entertainmentRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.entertainmentRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(entertainmentCosts = Some(true))

      val result = rows.entertainmentRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Entertainment.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/entertainment/disallowable-amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(Entertainment, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(entertainmentCosts = Some(true))

      val result = rows.entertainmentRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Entertainment.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/entertainment/check")
      )
    }
  }

  "the staff costs row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.staffCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.staffCostsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(disallowableStaffCosts = Some(true))

      val result = rows.staffCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = StaffCosts.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/staff/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(StaffCosts, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(disallowableStaffCosts = Some(true))

      val result = rows.staffCostsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = StaffCosts.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/staff/check")
      )
    }
  }

  "the construction industry row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.constructionIndustryRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.constructionIndustryRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(disallowableSubcontractorCosts = Some(true))

      val result = rows.constructionIndustryRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Construction.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/construction-industry/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(Construction, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(disallowableSubcontractorCosts = Some(true))

      val result = rows.constructionIndustryRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Construction.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/construction-industry/check")
      )
    }
  }

  "the professional fees row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.professionalFeesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.professionalFeesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(professionalServiceExpenses = List(ProfessionalServiceExpenses.ProfessionalFees))

      val result = rows.professionalFeesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ProfessionalFees.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/professional-fees/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(ProfessionalFees, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(professionalServiceExpenses = List(ProfessionalServiceExpenses.ProfessionalFees))

      val result = rows.professionalFeesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ProfessionalFees.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/professional-fees/check")
      )
    }
  }

  "the interest row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.interestRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.interestRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(disallowableInterest = Some(true))

      val result = rows.interestRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Interest.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/interest-bank-business-loans/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(Interest, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(disallowableInterest = Some(true))

      val result = rows.interestRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Interest.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/interest-bank-business-loans/check")
      )
    }
  }

  "the financial charges row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.financialChargesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.financialChargesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(disallowableOtherFinancialCharges = Some(true))

      val result = rows.financialChargesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = FinancialCharges.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/bank-credit-card-financial-charges/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(FinancialCharges, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(disallowableOtherFinancialCharges = Some(true))

      val result = rows.financialChargesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = FinancialCharges.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/bank-credit-card-financial-charges/check")
      )
    }
  }

  "the irrecoverable debts row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.irrecoverableDebtsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.irrecoverableDebtsRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(disallowableIrrecoverableDebts = Some(true))

      val result = rows.irrecoverableDebtsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = IrrecoverableDebts.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/irrecoverable-debts/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(IrrecoverableDebts, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(disallowableIrrecoverableDebts = Some(true))

      val result = rows.irrecoverableDebtsRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = IrrecoverableDebts.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/irrecoverable-debts/check")
      )
    }
  }

  "the depreciation row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.depreciationRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.depreciationRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(depreciation = true)

      val result = rows.depreciationRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Depreciation.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/depreciation/disallowable-amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(Depreciation, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(depreciation = true)

      val result = rows.depreciationRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = Depreciation.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/depreciation/check")
      )
    }
  }

  "the other expenses row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.otherExpensesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.otherExpensesRow(testExpensesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses         = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))
      val tailoringAnswers = testExpensesTailoring.copy(otherExpenses = OtherExpensesAnswer.YesAllowable)

      val result = rows.otherExpensesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = OtherExpenses.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/other-expenses/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(OtherExpenses, Completed)
      )
      val tailoringAnswers = testExpensesTailoring.copy(otherExpenses = OtherExpensesAnswer.YesAllowable)

      val result = rows.otherExpensesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = OtherExpenses.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/expenses/other-expenses/check")
      )
    }
  }

  "the capital allowances tailoring row" must {
    "display with CannotStartYet status when prerequisites aren't met" in {
      val result = rows.capitalAllowancesTailoringRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = CapitalAllowancesTailoring.entryName,
        status = TaskStatus.CannotStartYet(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances")
      )
    }

    "display with NotStarted status when prerequisites are met" in {
      val statuses = Seq(JourneyNameAndStatus(ExpensesTailoring, Completed))

      val result = rows.capitalAllowancesTailoringRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = CapitalAllowancesTailoring.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(ExpensesTailoring, Completed),
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed)
      )

      val result = rows.capitalAllowancesTailoringRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = CapitalAllowancesTailoring.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/check")
      )
    }
  }

  "the balancing allowance row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.balancingAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.balancingAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.Balancing)
      )

      val result = rows.balancingAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = BalancingAllowance.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/balancing-allowance/amount")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(BalancingAllowance, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.Balancing)
      )

      val result = rows.balancingAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = BalancingAllowance.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/balancing-allowance/check")
      )
    }
  }

  "the annual investment allowance row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.annualInvestmentAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.annualInvestmentAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.AnnualInvestment)
      )

      val result = rows.annualInvestmentAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = AnnualInvestmentAllowance.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/annual-investment-allowance/use")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(AnnualInvestmentAllowance, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.AnnualInvestment)
      )

      val result = rows.annualInvestmentAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = AnnualInvestmentAllowance.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/annual-investment-allowance/check")
      )
    }
  }

  "the structures and buildings allowance row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.structuresAndBuildingsAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.structuresAndBuildingsAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.StructuresAndBuildings)
      )

      val result = rows.structuresAndBuildingsAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = StructuresBuildings.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/structures-buildings")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(StructuresBuildings, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.StructuresAndBuildings)
      )

      val result = rows.structuresAndBuildingsAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = StructuresBuildings.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/structures-buildings/check")
      )
    }
  }

  "the writing down allowance row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.writingDownAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.writingDownAllowanceRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.WritingDown)
      )

      val result = rows.writingDownAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = WritingDownAllowance.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/writing-down-allowance")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(WritingDownAllowance, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.WritingDown)
      )

      val result = rows.writingDownAllowanceRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = WritingDownAllowance.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/writing-down-allowance/check")
      )
    }
  }

  "the special tax sites allowance row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.specialTaxSitesRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.specialTaxSitesRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.SpecialTaxSitesStructuresAndBuildings)
      )

      val result = rows.specialTaxSitesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = SpecialTaxSites.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/special-tax-sites")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(SpecialTaxSites, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.SpecialTaxSitesStructuresAndBuildings)
      )

      val result = rows.specialTaxSitesRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = SpecialTaxSites.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/special-tax-sites/check")
      )
    }
  }

  "the balancing charge row" must {
    "not display when neither prerequisites or display conditions are met" in {
      val result = rows.balancingChargeRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, Seq())

      result mustBe None
    }

    "not display when prerequisites are met but display conditions are not" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.balancingChargeRow(testCapitalAllowancesTailoring).build(testBusinessId, testTaxYear, statuses)

      result mustBe None
    }

    "display with NotStarted status when prerequisites and the display conditions are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.BalancingCharge)
      )

      val result = rows.balancingChargeRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = BalancingCharge.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/balancing-charge")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(BalancingCharge, Completed)
      )
      val tailoringAnswers = testCapitalAllowancesTailoring.copy(
        claimCapitalAllowances = true,
        selectCapitalAllowances = List(CapitalAllowances.BalancingCharge)
      )

      val result = rows.balancingChargeRow(tailoringAnswers).build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = BalancingCharge.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/capital-allowances/balancing-charge/check")
      )
    }
  }

  "the profit or loss row" must {
    "display with CannotStartYet status when prerequisites aren't met" in {
      val result = rows.profitOrLossRow.build(testBusinessId, testTaxYear, Seq())

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ProfitOrLoss.entryName,
        status = TaskStatus.CannotStartYet(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/profit-or-loss")
      )
    }

    "display with NotStarted status when prerequisites are met" in {
      val statuses = Seq(JourneyNameAndStatus(CapitalAllowancesTailoring, Completed))

      val result = rows.profitOrLossRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ProfitOrLoss.entryName,
        status = TaskStatus.NotStarted(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/profit-or-loss")
      )
    }

    "use the CYA URL when the section is completed" in {
      val statuses = Seq(
        JourneyNameAndStatus(CapitalAllowancesTailoring, Completed),
        JourneyNameAndStatus(ProfitOrLoss, Completed)
      )

      val result = rows.profitOrLossRow.build(testBusinessId, testTaxYear, statuses)

      result mustBe defined
      result.get mustBe TaskListSectionItem(
        title = ProfitOrLoss.entryName,
        status = TaskStatus.Completed(),
        href = Some(rows.urlPrefix + s"/$testTaxYear/$testBusinessId/profit-or-loss/check")
      )
    }
  }

}
