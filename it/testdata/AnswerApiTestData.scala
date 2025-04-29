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
