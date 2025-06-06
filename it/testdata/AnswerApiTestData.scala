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

package testdata

import models.common.JourneyName
import models.common.JourneyName.{IndustrySectors, TravelExpenses}
import models.database.IndustrySectorsDb
import models.database.expenses.travel.{LeasedVehicles, OwnVehicles, TravelExpensesDb}
import play.api.libs.json.{JsObject, JsValue, Json}

trait AnswerApiTestData {

  val validScenarios: Map[JourneyName, JsValue] = Map(
    TravelExpenses -> Json.toJson(
      TravelExpensesDb(
        expensesToClaim = Some(Seq(OwnVehicles)),
        allowablePublicTransportExpenses = Some(100),
        disallowablePublicTransportExpenses = Some(50))),
    IndustrySectors -> Json.toJson(
      IndustrySectorsDb(isFarmerOrMarketGardener = Some(true), hasProfitFromCreativeWorks = Some(false), isAllSelfEmploymentAbroad = Some(true)))
  )

  val invalidScenarios: Map[JourneyName, JsObject] = Map(
    TravelExpenses  -> Json.obj("expensesToClaim" -> "invalid"),
    IndustrySectors -> Json.obj("isFarmerOrMarketGardener" -> "invalid")
  )

  val replaceScenarios: Map[JourneyName, (JsValue, JsValue)] = Map(
    TravelExpenses -> (
      Json.toJson(
        TravelExpensesDb(
          expensesToClaim = Some(Seq(OwnVehicles)),
          allowablePublicTransportExpenses = Some(100),
          disallowablePublicTransportExpenses = Some(50))),
      Json.toJson(
        TravelExpensesDb(
          expensesToClaim = Some(Seq(LeasedVehicles)),
          allowablePublicTransportExpenses = Some(200),
          disallowablePublicTransportExpenses = Some(100)))
    ),
    IndustrySectors -> (
      Json.toJson(
        IndustrySectorsDb(isFarmerOrMarketGardener = Some(true), hasProfitFromCreativeWorks = Some(false), isAllSelfEmploymentAbroad = Some(true))),
      Json.toJson(
        IndustrySectorsDb(isFarmerOrMarketGardener = Some(false), hasProfitFromCreativeWorks = Some(true), isAllSelfEmploymentAbroad = None))
    )
  )

}
