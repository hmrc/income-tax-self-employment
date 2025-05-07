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
