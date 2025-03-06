package testdata

import models.common.{BusinessId, JourneyContextWithNino, Mtditid, Nino, TaxYear}
import uk.gov.hmrc.http.HeaderCarrier

trait CommonTestData {

  val testTaxYear2024: TaxYear   = TaxYear(2024)
  val testTaxYear2025: TaxYear   = TaxYear(2025)
  val testMtdItId: Mtditid       = Mtditid("mtditid")
  val testBusinessId: BusinessId = BusinessId("XAIS12345678901")
  val testNino: Nino             = Nino("AB123456C")
  val testClaimId: String        = "12345"

  val testContextWithNino: JourneyContextWithNino = JourneyContextWithNino(testTaxYear2024, testBusinessId, testMtdItId, testNino)

}
