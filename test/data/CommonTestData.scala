
package data

import models.common._

trait CommonTestData {

  val testBusinessId: BusinessId = BusinessId("XH1234567890")
  val testMtdId: Mtditid = Mtditid("12345")
  val testNino: Nino = Nino("AB123456C")

  val testCurrentTaxYear: TaxYear = TaxYear(2025)
  val testPrevTaxYear: TaxYear = TaxYear(2024)

  val testContextCurrentYear: JourneyContextWithNino = JourneyContextWithNino(testCurrentTaxYear, testBusinessId, testMtdId, testNino)
  val testContextPrevYear: JourneyContextWithNino = JourneyContextWithNino(testPrevTaxYear, testBusinessId, testMtdId, testNino)

}
