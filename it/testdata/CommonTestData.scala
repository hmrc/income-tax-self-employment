package testdata

import models.common._

trait CommonTestData extends IntegrationTimeData {

  val testTaxYear2024: TaxYear   = TaxYear(2024)
  val testTaxYear2025: TaxYear   = TaxYear(2025)
  val testMtdItId: Mtditid       = Mtditid("555555555")
  val testBusinessId: BusinessId = BusinessId("SJPR05893938418")
  val testNino: Nino             = Nino("AA123123A")
  val testClaimId: String        = "12345"

  val testTaxYear: TaxYear  = TaxYear(2024)
  val testAuthToken: String = "Bearer 123"
  val testApiToken          = "testToken"

  val testContextWithNino: JourneyContextWithNino = JourneyContextWithNino(testTaxYear2024, testBusinessId, testMtdItId, testNino)

}
