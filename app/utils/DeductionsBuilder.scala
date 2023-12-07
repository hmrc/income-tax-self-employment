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

package utils

import models.connector.api_1894.request._
import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers

trait DeductionsBuilder[A] {
  def build(answers: A): DeductionsType
}

object DeductionsBuilder {
  implicit val officeSupplies: DeductionsBuilder[OfficeSuppliesJourneyAnswers] =
    (answers: OfficeSuppliesJourneyAnswers) =>
      DeductionsType.empty.copy(
        adminCosts = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.officeSuppliesAmount), answers.officeSuppliesDisallowableAmount)
        )
      )

  implicit val goodsToSellOrUse: DeductionsBuilder[GoodsToSellOrUseJourneyAnswers] =
    (answers: GoodsToSellOrUseJourneyAnswers) =>
      DeductionsType.empty.copy(
        costOfGoods = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.goodsToSellOrUseAmount), answers.disallowableGoodsToSellOrUseAmount)
        )
      )

  implicit val repairsAndMaintenanceCosts: DeductionsBuilder[RepairsAndMaintenanceCostsJourneyAnswers] =
    (answers: RepairsAndMaintenanceCostsJourneyAnswers) =>
      DeductionsType.empty.copy(
        maintenanceCosts = Some(
          SelfEmploymentDeductionsDetailPosNegType(Some(answers.repairsAndMaintenanceAmount), answers.repairsAndMaintenanceDisallowableAmount)
        )
      )

  implicit val staffCosts: DeductionsBuilder[StaffCostsJourneyAnswers] =
    (answers: StaffCostsJourneyAnswers) =>
      DeductionsType.empty.copy(
        staffCosts = Some(
          SelfEmploymentDeductionsDetailType(Some(answers.staffCostsAmount), answers.staffCostsDisallowableAmount)
        )
      )

  implicit val entertainmentCosts: DeductionsBuilder[EntertainmentJourneyAnswers] =
    (answers: EntertainmentJourneyAnswers) =>
      DeductionsType.empty.copy(
        businessEntertainmentCosts = Some(
          SelfEmploymentDeductionsDetailType(None, Some(answers.entertainmentAmount))
        )
      )
}
