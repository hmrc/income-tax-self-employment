package testdata

import models.common._
import models.connector.api_1171.{BusinessDataDetails, ResponseType, SuccessResponseSchema => Api1171ResponseSchema}
import models.domain.Business

trait CommonTestData extends IntegrationTimeData {

  val testTaxYear2024: TaxYear    = TaxYear(2024)
  val testTaxYear2025: TaxYear    = TaxYear(2025)
  val testMtdItId: Mtditid        = Mtditid("555555555")
  val testBusinessId: BusinessId  = BusinessId("SJPR05893938418")
  val testBusinessId2: BusinessId = BusinessId("SJPR05893938419")
  val testNino: Nino              = Nino("AA123123A")
  val testClaimId: String         = "12345"
  val testTradingName             = "Test trading name"
  val testTradingName2            = "Test trading name 2"

  val testTaxYear: TaxYear  = TaxYear(2024)
  val testAuthToken: String = "Bearer 123"
  val testApiToken          = "testToken"

  val testContextWithNino: JourneyContextWithNino = JourneyContextWithNino(testTaxYear2024, testBusinessId, testMtdItId, testNino)

  val testBusinessDetails1 = BusinessDataDetails(
    incomeSourceId = testBusinessId.value,
    accountingPeriodStartDate = "2024-04-06",
    accountingPeriodEndDate = "2025-04-05",
    cessationDate = None,
    incomeSource = None,
    tradingName = Some(testTradingName),
    businessAddressDetails = None,
    businessContactDetails = None,
    tradingStartDate = None,
    cashOrAccruals = None,
    seasonal = None,
    paperLess = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    incomeSourceStartDate = None,
    latencyDetails = None
  )

  val testBusinessDetails2 = BusinessDataDetails(
    incomeSourceId = testBusinessId2.value,
    accountingPeriodStartDate = "2024-04-06",
    accountingPeriodEndDate = "2025-04-05",
    cessationDate = None,
    incomeSource = None,
    tradingName = Some(testTradingName2),
    businessAddressDetails = None,
    businessContactDetails = None,
    tradingStartDate = None,
    cashOrAccruals = None,
    seasonal = None,
    paperLess = None,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    incomeSourceStartDate = None,
    latencyDetails = None
  )

  val test1171Response = Api1171ResponseSchema(
    processingDate = "2023-10-01T12:00:00Z",
    taxPayerDisplayResponse = ResponseType(
      safeId = "1",
      nino = testNino.value,
      mtdId = testMtdItId.value,
      yearOfMigration = Some("2024"),
      propertyIncome = false,
      businessData = Some(
        List(
          testBusinessDetails1,
          testBusinessDetails2
        ))
    )
  )

}
