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
import models.common.JourneyContextWithNino
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1639.SuccessResponseAPI1639
import models.connector.api_1786.{DeductionsType, SelfEmploymentDeductionsDetailTypePosNeg}
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestData
import models.connector.api_1803.{AnnualAllowancesType, SuccessResponseSchema}
import models.connector.api_1894.request.CreateSEPeriodSummaryRequestData
import models.connector.api_1895.request.AmendSEPeriodSummaryRequestData
import models.connector.api_1965.{ListSEPeriodSummariesResponse, PeriodDetails}
import models.connector.citizen_details.{Ids, LegalNames, Name}
import models.connector.{api_1171, api_1786, api_1871, citizen_details}
import models.domain.ApiResultT
import models.error.ServiceError
import stubs.connectors.StubIFSConnector._
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec._

import java.time.OffsetDateTime
import scala.concurrent.{ExecutionContext, Future}

case class StubIFSConnector(
    createSEPeriodSummaryResult: Future[Api1894Response] = Future.successful(().asRight),
    amendSEPeriodSummaryResult: Future[Api1895Response] = Future.successful(().asRight),
    createAmendSEAnnualSubmissionResult: Future[Api1802Response] = Future.successful(().asRight),
    getAnnualSummariesResult: Future[Api1803Response] = Future.successful(api1803SuccessResponse.asRight),
    listSEPeriodSummariesResult: Future[Api1965Response] = Future.successful(api1965MatchedResponse.asRight),
    getPeriodicSummaryDetailResult: Future[Api1786Response] = Future.successful(api1786EmptySuccessResponse.asRight),
    getDisclosuresSubmissionResult: Either[ServiceError, Option[SuccessResponseAPI1639]] = Right(None),
    upsertDisclosuresSubmissionResult: Either[ServiceError, Unit] = Right(()),
    deleteDisclosuresSubmissionResult: Either[ServiceError, Unit] = Right(())
) extends IFSConnector {
  var upsertDisclosuresSubmissionData: Option[RequestSchemaAPI1638] = None

  override def createSEPeriodSummary(
      data: CreateSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1894Response] =
    createSEPeriodSummaryResult

  override def amendSEPeriodSummary(
      data: AmendSEPeriodSummaryRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1895Response] =
    amendSEPeriodSummaryResult

  override def listSEPeriodSummary(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1965Response] =
    listSEPeriodSummariesResult

  override def createAmendSEAnnualSubmission(
      data: CreateAmendSEAnnualSubmissionRequestData)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1802Response] =
    createAmendSEAnnualSubmissionResult

  def getPeriodicSummaryDetail(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1786Response] =
    getPeriodicSummaryDetailResult

  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Api1803Response] =
    getAnnualSummariesResult

  def getDisclosuresSubmission(
      ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Option[SuccessResponseAPI1639]] =
    EitherT.fromEither[Future](getDisclosuresSubmissionResult)

  def upsertDisclosuresSubmission(ctx: JourneyContextWithNino, data: RequestSchemaAPI1638)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] = {
    upsertDisclosuresSubmissionData = Some(data)
    EitherT.fromEither[Future](upsertDisclosuresSubmissionResult)
  }

  def deleteDisclosuresSubmission(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] = {
    upsertDisclosuresSubmissionData = None
    EitherT.fromEither[Future](deleteDisclosuresSubmissionResult)
  }

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

  val api1803SuccessResponse: SuccessResponseSchema = SuccessResponseSchema(
    None,
    Some(
      AnnualAllowancesType.emptyAnnualAllowancesType.copy(
        zeroEmissionsCarAllowance = Some(5000.00),
        electricChargePointAllowance = Some(4000.00)
      )),
    None
  )

  val api1965MatchedResponse: ListSEPeriodSummariesResponse = ListSEPeriodSummariesResponse(
    Some(List(PeriodDetails(None, Some("2023-04-06"), Some("2024-04-05")))))

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

  val api1965EmptyResponse: ListSEPeriodSummariesResponse = ListSEPeriodSummariesResponse(Some(List.empty))

  val api1871EmptyResponse: api_1871.BusinessIncomeSourcesSummaryResponse = api_1871.BusinessIncomeSourcesSummaryResponse.empty

}
