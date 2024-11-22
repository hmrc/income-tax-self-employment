package models.frontend.capitalAllowances.structuresBuildings

import models.connector.api_1802.request.{Building, BuildingAllowance, FirstYear}
import models.connector.dateFormatter
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.LocalDate

class NewStructureBuildingSpec extends AnyWordSpecLike {
  val structureBuilding: NewStructureBuilding = NewStructureBuilding(
    Some(LocalDate.parse("2023-03-01", dateFormatter)),
    Some(BigDecimal(10000)),
    Some(StructuresBuildingsLocation(Some("name"), Some("number"), "AA11AA")),
    Some(BigDecimal(10000))
  )

  "toBuildingAllowance" should {
    "create building allowance" in {
      val buildingAllowance = structureBuilding.toBuildingAllowance
      assert(
        buildingAllowance === Some(BuildingAllowance(10000, Some(FirstYear("2023-03-01", 10000)), Building(Some("name"), Some("number"), "AA11AA")))
      )
    }

    "create building allowance when QualifyingExpenditure value is None" in {
      val buildingAllowance = structureBuilding.copy(newStructureBuildingQualifyingExpenditureAmount = None).toBuildingAllowance
      assert(
        buildingAllowance === Some(BuildingAllowance(10000, None, Building(Some("name"), Some("number"), "AA11AA")))
      )
    }
  }
}
