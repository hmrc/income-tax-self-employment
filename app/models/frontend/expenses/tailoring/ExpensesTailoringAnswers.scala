/*
 * Copyright 2024 HM Revenue & Customs
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

package models.frontend.expenses.tailoring

import models.frontend.expenses.tailoring.individualCategories._
import play.api.libs.json._
import utils.JsonOps._

sealed trait ExpensesTailoringAnswers {
  val expensesCategories: ExpensesTailoring
}

object ExpensesTailoringAnswers {
  val expensesCategoriesKey = "expensesCategories"

  final object NoExpensesAnswers extends ExpensesTailoringAnswers {
    val expensesCategories: ExpensesTailoring = ExpensesTailoring.NoExpenses

    implicit val writes: OWrites[NoExpensesAnswers.type] =
      OWrites(_ => Json.obj().withType(expensesCategoriesKey, ExpensesTailoring.NoExpenses.toString))

    implicit val reads: Reads[NoExpensesAnswers.type] =
      Reads(_ => JsSuccess(NoExpensesAnswers))
  }

  final case class AsOneTotalAnswers(totalAmount: BigDecimal) extends ExpensesTailoringAnswers {
    val expensesCategories: ExpensesTailoring = ExpensesTailoring.TotalAmount
  }

  object AsOneTotalAnswers {
    implicit val writes: OWrites[AsOneTotalAnswers] = OWrites[AsOneTotalAnswers] { answers =>
      Json.writes[AsOneTotalAnswers].writes(answers) + (expensesCategoriesKey -> JsString(answers.expensesCategories.toString))
    }

    implicit val reads = Json.reads[AsOneTotalAnswers]
  }

  final case class ExpensesTailoringIndividualCategoriesAnswers(
      officeSupplies: OfficeSupplies,
      taxiMinicabOrRoadHaulage: TaxiMinicabOrRoadHaulage,
      goodsToSellOrUse: GoodsToSellOrUse,
      repairsAndMaintenance: RepairsAndMaintenance,
      workFromHome: WorkFromHome,
      workFromBusinessPremises: WorkFromBusinessPremises,
      travelForWork: TravelForWork,
      advertisingOrMarketing: AdvertisingOrMarketing,
      entertainmentCosts: Option[EntertainmentCosts],
      professionalServiceExpenses: List[ProfessionalServiceExpenses],
      financialExpenses: List[FinancialExpenses],
      depreciation: Depreciation,
      otherExpenses: OtherExpenses,
      disallowableInterest: Option[DisallowableInterest],
      disallowableOtherFinancialCharges: Option[DisallowableOtherFinancialCharges],
      disallowableIrrecoverableDebts: Option[DisallowableIrrecoverableDebts],
      disallowableStaffCosts: Option[DisallowableStaffCosts],
      disallowableSubcontractorCosts: Option[DisallowableSubcontractorCosts],
      disallowableProfessionalFees: Option[DisallowableProfessionalFees]
  ) extends ExpensesTailoringAnswers {
    val expensesCategories: ExpensesTailoring = ExpensesTailoring.IndividualCategories
  }

  object ExpensesTailoringIndividualCategoriesAnswers {

    implicit val writes: OWrites[ExpensesTailoringIndividualCategoriesAnswers] = OWrites[ExpensesTailoringIndividualCategoriesAnswers] { answers =>
      Json.writes[ExpensesTailoringIndividualCategoriesAnswers].writes(answers) + (expensesCategoriesKey -> JsString(
        answers.expensesCategories.toString))
    }
    implicit val reads = Json.reads[ExpensesTailoringIndividualCategoriesAnswers]

  }

  implicit val writes: OWrites[ExpensesTailoringAnswers] = OWrites[ExpensesTailoringAnswers] {
    case noExpenses: NoExpensesAnswers.type                       => NoExpensesAnswers.writes.writes(noExpenses)
    case oneTotal: AsOneTotalAnswers                              => AsOneTotalAnswers.writes.writes(oneTotal)
    case individual: ExpensesTailoringIndividualCategoriesAnswers => ExpensesTailoringIndividualCategoriesAnswers.writes.writes(individual)
  }

  implicit val reads: Reads[ExpensesTailoringAnswers] =
    (__ \ expensesCategoriesKey).read[ExpensesTailoring].flatMap {
      case ExpensesTailoring.NoExpenses           => NoExpensesAnswers.reads.map(identity[ExpensesTailoringAnswers])
      case ExpensesTailoring.TotalAmount          => AsOneTotalAnswers.reads.map(identity[ExpensesTailoringAnswers])
      case ExpensesTailoring.IndividualCategories => ExpensesTailoringIndividualCategoriesAnswers.reads.map(identity[ExpensesTailoringAnswers])
    }

  implicit val format: OFormat[ExpensesTailoringAnswers] = OFormat.apply(reads, writes)
}
