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

package gens

import models.frontend.expenses.entertainment.EntertainmentJourneyAnswers
import models.frontend.expenses.goodsToSellOrUse.GoodsToSellOrUseJourneyAnswers
import models.frontend.expenses.officeSupplies.OfficeSuppliesJourneyAnswers
import models.frontend.expenses.repairsandmaintenance.RepairsAndMaintenanceCostsJourneyAnswers
import models.frontend.expenses.staffcosts.StaffCostsJourneyAnswers
import org.scalacheck.Gen

object ExpensesJourneyAnswersGen {

  val goodsToSellOrUseJourneyAnswersGen: Gen[GoodsToSellOrUseJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield GoodsToSellOrUseJourneyAnswers(amount, disallowableAmount)

  val officeSuppliesJourneyAnswersGen: Gen[OfficeSuppliesJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield OfficeSuppliesJourneyAnswers(amount, disallowableAmount)

  val repairsAndMaintenanceCostsJourneyAnswersGen: Gen[RepairsAndMaintenanceCostsJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield RepairsAndMaintenanceCostsJourneyAnswers(amount, disallowableAmount)

  val staffCostsJourneyAnswersGen: Gen[StaffCostsJourneyAnswers] = for {
    amount             <- bigDecimalGen
    disallowableAmount <- Gen.option(bigDecimalGen)
  } yield StaffCostsJourneyAnswers(amount, disallowableAmount)

  val entertainmentCostsJourneyAnswersGen: Gen[EntertainmentJourneyAnswers] = for {
    disallowableAmount <- bigDecimalGen
  } yield EntertainmentJourneyAnswers(disallowableAmount)

}
