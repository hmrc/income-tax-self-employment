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

package data

import builders.BusinessDataBuilder.citizenDetailsDateOfBirth
import cats.implicits.catsSyntaxOptionId
import models.common.BusinessId
import models.connector._
import models.connector.api_1171.BusinessDataDetailsTestData
import models.connector.api_1500.LossType
import models.connector.api_1786.{DeductionsType, SelfEmploymentDeductionsDetailTypePosNeg}
import models.connector.api_1803.{AnnualAdjustmentsType, AnnualAllowancesType, SuccessResponseSchema}
import models.connector.api_1870.LossData
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import models.connector.businessDetailsConnector.{BusinessDataDetails, ResponseType}
import models.connector.citizen_details.{Ids, LegalNames, Name}
import utils.BaseSpec._

import java.time.OffsetDateTime


object IFSConnectorTestData {

  val citizenDetailsResponse: citizen_details.SuccessResponseSchema =
    citizen_details.SuccessResponseSchema(
      name = LegalNames(current = Name(firstName = "Mike", lastName = "Wazowski"), previous = List(Name(firstName = "Jess", lastName = "Smith"))),
      ids = Ids(nino.value),
      dateOfBirth = citizenDetailsDateOfBirth
    )

  val api1171EmptyResponse: businessDetailsConnector.BusinessDetailsSuccessResponseSchema =
    businessDetailsConnector.BusinessDetailsSuccessResponseSchema(
      OffsetDateTime.now().toString,
      ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, None))

  def api1171SingleBusinessResponse(businessId: BusinessId): businessDetailsConnector.BusinessDetailsSuccessResponseSchema =
    businessDetailsConnector.BusinessDetailsSuccessResponseSchema(
      OffsetDateTime.now().toString,
      businessDetailsConnector.ResponseType(
        "safeId",
        "nino",
        "mtdid",
        None,
        propertyIncome = false,
        Option(List(BusinessDataDetailsTestData.mkExample(businessId))))
    )

  def api1171MultipleBusinessResponse(businessIds: List[BusinessId]): businessDetailsConnector.BusinessDetailsSuccessResponseSchema = {
    val businessData: List[BusinessDataDetails] = businessIds.map(BusinessDataDetailsTestData.mkExample)
    businessDetailsConnector.BusinessDetailsSuccessResponseSchema(
      OffsetDateTime.now().toString,
      businessDetailsConnector.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, Option(businessData))
    )
  }

  val api1803SuccessResponse: SuccessResponseSchema = SuccessResponseSchema(
    None,
    Option(
      AnnualAllowancesType.emptyAnnualAllowancesType
        .copy(zeroEmissionsCarAllowance = Option(BigDecimal(5000.00)), zeroEmissionGoodsVehicleAllowance = Option(BigDecimal(5000.00)))),
    None
  )

  val api1803SuccessResponseWithAAType: SuccessResponseSchema = SuccessResponseSchema(
    Option(AnnualAdjustmentsType.empty.copy(goodsAndServicesOwnUse = Option(BigDecimal(200)))),
    Option(
      AnnualAllowancesType.emptyAnnualAllowancesType.copy(
        zeroEmissionsCarAllowance = Option(BigDecimal(5000.00)),
        zeroEmissionGoodsVehicleAllowance = Option(BigDecimal(5000.00))
      )),
    None
  )

  val api1803EmptyResponse: SuccessResponseSchema = SuccessResponseSchema.empty

  val api1965MatchedResponse: Option[ListSEPeriodSummariesResponse] = Option(
    ListSEPeriodSummariesResponse(
      Option(List(PeriodDetails(None, Option(s"${currTaxYear.endYear - 1}-04-06"), Option(s"${currTaxYear.endYear}-04-05"))))))

  val api1786EmptySuccessResponse: api_1786.SuccessResponseSchema =
    api_1786.SuccessResponseSchema(currTaxYearStart, currTaxYearEnd, api_1786.FinancialsType(None, None))

  val api1786DeductionsSuccessResponse: api_1786.SuccessResponseSchema =
    api_1786.SuccessResponseSchema(
      currTaxYearStart,
      currTaxYearEnd,
      api_1786.FinancialsType(
        Option(
          DeductionsType.empty.copy(
            costOfGoods =
              Some(SelfEmploymentDeductionsDetailTypePosNeg(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some)),
            premisesRunningCosts =
              Some(SelfEmploymentDeductionsDetailTypePosNeg(amount = BigDecimal(100.00).some, disallowableAmount = BigDecimal(100.00).some))
          )),
        None
      )
    )

  val api1965EmptyResponse: Option[ListSEPeriodSummariesResponse] = Some(ListSEPeriodSummariesResponse(Some(List.empty)))

  val api1871EmptyResponse: api_1871.BusinessIncomeSourcesSummaryResponse = api_1871.BusinessIncomeSourcesSummaryResponse.empty

  val api1500EmptyResponse: api_1500.SuccessResponseSchema   = api_1500.SuccessResponseSchema("")
  val api1500SuccessResponse: api_1500.SuccessResponseSchema = api_1500.SuccessResponseSchema("5678")
  val api1501EmptyResponse: api_1501.SuccessResponseSchema   = api_1501.SuccessResponseSchema("", LossType.SelfEmployment, 0, "", testDateTime)
  val api1501SuccessResponse: api_1501.SuccessResponseSchema =
    api_1501.SuccessResponseSchema("1234", LossType.SelfEmployment, 400, "2022-23", testDateTime)
  val api1502EmptyResponse: api_1502.SuccessResponseSchema =
    api_1502.SuccessResponseSchema("", LossType.SelfEmployment, 0, "", testDateTime, None)
  val api1502SuccessResponse: api_1502.SuccessResponseSchema =
    api_1502.SuccessResponseSchema(
      "1234",
      LossType.SelfEmployment,
      400,
      "2022-23",
      testDateTime,
      Some(
        List(
          api_1502.SuccessResponseSchemaLinks(
            "/individuals/losses/TC663795B/brought-forward-losses/AAZZ1234567890a",
            "self",
            "GET"
          )))
    )
  val api1505SuccessResponse: api_1505.ClaimId =
    api_1505.ClaimId("1234568790ABCDE")

  val api1870EmptyResponse: api_1870.SuccessResponseSchema = api_1870.SuccessResponseSchema(List.empty)
  val api2085EmptyResponse: api_2085.ListOfIncomeSources   = api_2085.ListOfIncomeSources(List.empty)
  val api1870SuccessResponse: api_1870.SuccessResponseSchema = api_1870.SuccessResponseSchema(
    List(
      LossData("5678", "SJPR05893938418", LossType.SelfEmployment, 400, "2018-19", testDateTime),
      LossData("5689", "1245", LossType.SelfEmployment, 500, "2021-22", testDateTime)
    )
  )
}
