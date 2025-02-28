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

package stubs.connectors

import bulders.BusinessDataBuilder.citizenDetailsDateOfBirth
import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import connectors.IFSConnector
import connectors.IFSConnector._
import models.common.{BusinessId, JourneyContextWithNino}
import models.connector.ReliefClaimType.CF
import models.connector._
import models.connector.api_1171.{BusinessDataDetails, BusinessDataDetailsTestData}
import models.connector.api_1500.LossType
import models.connector.api_1505.{CreateLossClaimRequestBody, ClaimId}
import models.connector.api_1508.GetLossClaimSuccessResponse
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1786.{DeductionsType, SelfEmploymentDeductionsDetailTypePosNeg}
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803.{AnnualAllowancesType, SuccessResponseSchema}
import models.connector.api_1870.LossData
import models.connector.api_1894.request.CreateSEPeriodSummaryRequestData
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestData
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import models.connector.citizen_details.{Ids, LegalNames, Name}
import models.domain.ApiResultT
import models.error.{DownstreamError, ServiceError}
import stubs.connectors.StubIFSConnector._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.{LocalDateTime, OffsetDateTime}
import scala.concurrent.{ExecutionContext, Future}

case class StubIFSConnector(
                             createSEPeriodSummaryResult: Future[Api1894Response] = Future.successful(().asRight),
                             amendSEPeriodSummaryResult: Either[DownstreamError, Unit] = Right(()),
                             getAnnualSummariesResult: Either[DownstreamError, api_1803.SuccessResponseSchema] = Right(api1803SuccessResponse),
                             createAmendSEAnnualSubmissionResult: Either[DownstreamError, Unit] = Right(()),
                             deleteSEAnnualSummariesResult: Either[ServiceError, Unit] = Right(()),
                             listSEPeriodSummariesResult: Future[Api1965Response] = Future.successful(api1965MatchedResponse.asRight),
                             getPeriodicSummaryDetailResult: Future[Api1786Response] = Future.successful(api1786EmptySuccessResponse.asRight),
                             getDisclosuresSubmissionResult: Either[ServiceError, Option[SuccessResponseAPI1639]] = Right(None),
                             upsertDisclosuresSubmissionResult: Either[ServiceError, Unit] = Right(()),
                             deleteDisclosuresSubmissionResult: Either[ServiceError, Unit] = Right(()),
                             getAnnualSummariesResultTest1: Either[DownstreamError, api_1803.SuccessResponseSchema] = Right(api1803SuccessResponse),
                             getAnnualSummariesResultTest2: Either[DownstreamError, api_1803.SuccessResponseSchema] = Right(api1803SuccessResponse),
                             getAnnualSummariesResultTest3: Either[DownstreamError, api_1803.SuccessResponseSchema] = Right(api1803SuccessResponse),
                             getAnnualSummariesResultTest4: Either[DownstreamError, api_1803.SuccessResponseSchema] = Right(api1803SuccessResponse),
                             createLossClaimResult: Either[DownstreamError, ClaimId] = Right(api1505SuccessResponse),
                             getLossClaimResult: Either[DownstreamError, GetLossClaimSuccessResponse] = Right(api1508SuccessResponse)
) extends IFSConnector {
  var amendSEPeriodSummaryResultData: Option[AmendSEPeriodSummaryRequestData]                    = None
  var upsertDisclosuresSubmissionData: Option[RequestSchemaAPI1638]                              = None
  var upsertAnnualSummariesSubmissionData: Option[CreateAmendSEAnnualSubmissionRequestData]      = None
  var upsertAnnualSummariesSubmissionDataTest1: Option[CreateAmendSEAnnualSubmissionRequestData] = None
  var upsertAnnualSummariesSubmissionDataTest2: Option[CreateAmendSEAnnualSubmissionRequestData] = None
  var upsertAnnualSummariesSubmissionDataTest3: Option[CreateAmendSEAnnualSubmissionRequestData] = None
  var upsertAnnualSummariesSubmissionDataTest4: Option[CreateAmendSEAnnualSubmissionRequestData] = None

  override def createSEPeriodSummary(
      data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] =
    createSEPeriodSummaryResult

  override def amendSEPeriodSummary(
      data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] = {
    if (amendSEPeriodSummaryResult.isRight) amendSEPeriodSummaryResultData = Some(data)
    Future.successful(amendSEPeriodSummaryResult)
  }

  override def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] =
    listSEPeriodSummariesResult

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response] =
    getPeriodicSummaryDetailResult

  override def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response] =
    ctx.businessId match {
      case BusinessId("BusinessId1") => Future.successful(getAnnualSummariesResultTest1)
      case BusinessId("BusinessId2") => Future.successful(getAnnualSummariesResultTest2)
      case BusinessId("BusinessId3") => Future.successful(getAnnualSummariesResultTest3)
      case BusinessId("BusinessId4") => Future.successful(getAnnualSummariesResultTest4)
      case _                         => Future.successful(getAnnualSummariesResult)
    }

  override def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] = {
    if (createAmendSEAnnualSubmissionResult.isRight) data.businessId match {
      case BusinessId("BusinessId1") => upsertAnnualSummariesSubmissionDataTest1 = Some(data)
      case BusinessId("BusinessId2") => upsertAnnualSummariesSubmissionDataTest2 = Some(data)
      case BusinessId("BusinessId3") => upsertAnnualSummariesSubmissionDataTest3 = Some(data)
      case BusinessId("BusinessId4") => upsertAnnualSummariesSubmissionDataTest4 = Some(data)
      case _                         => upsertAnnualSummariesSubmissionData = Some(data)
    }
    Future.successful(createAmendSEAnnualSubmissionResult)
  }

  def deleteSEAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    if (deleteDisclosuresSubmissionResult.isRight) upsertAnnualSummariesSubmissionData = None
    EitherT.fromEither[Future](deleteSEAnnualSummariesResult)
  }

  def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino, requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] =
    requestBody match {
      case Some(body) =>
        val requestData = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, body)
        EitherT(createAmendSEAnnualSubmission(requestData))
      case None => deleteSEAnnualSummaries(ctx)
    }

  def getDisclosuresSubmission(
      ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[SuccessResponseAPI1639]] =
    EitherT.fromEither[Future](getDisclosuresSubmissionResult)

  def upsertDisclosuresSubmission(ctx: JourneyContextWithNino, data: RequestSchemaAPI1638)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] = {
    if (upsertDisclosuresSubmissionResult.isRight) upsertDisclosuresSubmissionData = Some(data)
    EitherT.fromEither[Future](upsertDisclosuresSubmissionResult)
  }

  def deleteDisclosuresSubmission(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    if (deleteDisclosuresSubmissionResult.isRight) upsertDisclosuresSubmissionData = None
    EitherT.fromEither[Future](deleteDisclosuresSubmissionResult)
  }

  def createLossClaim(ctx: JourneyContextWithNino, requestBody: CreateLossClaimRequestBody)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[ClaimId] = EitherT.fromEither[Future](createLossClaimResult)

  def getLossClaim(ctx: JourneyContextWithNino, claimId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[GetLossClaimSuccessResponse] = EitherT.fromEither[Future](getLossClaimResult)

}

object StubIFSConnector {

  val citizenDetailsResponse: citizen_details.SuccessResponseSchema =
    citizen_details.SuccessResponseSchema(
      name = LegalNames(current = Name(firstName = "Mike", lastName = "Wazowski"), previous = List(Name(firstName = "Jess", lastName = "Smith"))),
      ids = Ids(nino.value),
      dateOfBirth = citizenDetailsDateOfBirth
    )

  val api1171EmptyResponse: api_1171.SuccessResponseSchema =
    api_1171.SuccessResponseSchema(
      OffsetDateTime.now().toString,
      api_1171.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, None))

  def api1171SingleBusinessResponse(businessId: BusinessId): api_1171.SuccessResponseSchema =
    api_1171.SuccessResponseSchema(
      OffsetDateTime.now().toString,
      api_1171.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, Some(List(BusinessDataDetailsTestData.mkExample(businessId))))
    )

  def api1171MultipleBusinessResponse(businessIds: List[BusinessId]): api_1171.SuccessResponseSchema = {
    val businessData: List[BusinessDataDetails] = businessIds.map(BusinessDataDetailsTestData.mkExample)
    api_1171.SuccessResponseSchema(
      OffsetDateTime.now().toString,
      api_1171.ResponseType("safeId", "nino", "mtdid", None, propertyIncome = false, Some(businessData))
    )
  }

  val api1803SuccessResponse: SuccessResponseSchema = SuccessResponseSchema(
    None,
    Some(
      AnnualAllowancesType.emptyAnnualAllowancesType.copy(
        zeroEmissionsCarAllowance = Some(5000.00),
        zeroEmissionGoodsVehicleAllowance = Some(5000.00)
      )),
    None
  )

  val api1803EmptyResponse: SuccessResponseSchema = SuccessResponseSchema.empty

  val api1965MatchedResponse: Option[ListSEPeriodSummariesResponse] = Some(
    ListSEPeriodSummariesResponse(Some(List(PeriodDetails(None, Some(s"${currTaxYear.endYear - 1}-04-06"), Some(s"${currTaxYear.endYear}-04-05"))))))

  val api1786EmptySuccessResponse: api_1786.SuccessResponseSchema =
    api_1786.SuccessResponseSchema(currTaxYearStart, currTaxYearEnd, api_1786.FinancialsType(None, None))

  val api1786DeductionsSuccessResponse: api_1786.SuccessResponseSchema =
    api_1786.SuccessResponseSchema(
      currTaxYearStart,
      currTaxYearEnd,
      api_1786.FinancialsType(
        Some(
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

  val api1508SuccessResponse: api_1508.GetLossClaimSuccessResponse =
    api_1508.GetLossClaimSuccessResponse(
      incomeSourceId = "012345678912345",
      reliefClaimed = CF,
      claimId = models.connector.ClaimId("AAZZ1234567890A"),
      sequence = Option(2),
      submissionDate = testDateTime)
  val api1870EmptyResponse: api_1870.SuccessResponseSchema = api_1870.SuccessResponseSchema(List.empty)
  val api2085EmptyResponse: api_2085.ListOfIncomeSources   = api_2085.ListOfIncomeSources(List.empty)
  val api1870SuccessResponse: api_1870.SuccessResponseSchema = api_1870.SuccessResponseSchema(
    List(
      LossData("5678", "SJPR05893938418", LossType.SelfEmployment, 400, "2018-19", testDateTime),
      LossData("5689", "1245", LossType.SelfEmployment, 500, "2021-22", testDateTime)
    )
  )
}
