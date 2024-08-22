/*
 * Copyright 2024 HM Revenue & Customs
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

package services.journeyAnswers

import cats.data.EitherT
import cats.implicits.toFunctorOps
import connectors.{IFSBusinessDetailsConnector, IFSConnector}
import models.common._
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1802.request.CreateAmendSEAnnualSubmissionRequestData
import models.connector.{api_1171, api_1803}
import models.database.nics.NICsStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import models.frontend.nics.{NICsAnswers, NICsClass2Answers, NICsClass4Answers}
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait NICsAnswersService {
  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]]
}

@Singleton
class NICsAnswersServiceImpl @Inject() (connector: IFSConnector,
                                        businessConnector: IFSBusinessDetailsConnector,
                                        repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends NICsAnswersService {

  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      existingAnswers <- connector.getDisclosuresSubmission(ctx)
      upsertRequest = RequestSchemaAPI1638.mkRequestBody(answers, existingAnswers)
      _ <- upsertOrDeleteClass2Data(upsertRequest, ctx)
      storageAnswers = NICsStorageAnswers.fromJourneyAnswers(answers)
      _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.NationalInsuranceContributions), Json.toJson(storageAnswers))
    } yield ()

  private def upsertOrDeleteClass2Data(maybeClass2Nics: Option[RequestSchemaAPI1638], ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    maybeClass2Nics match {
      case Some(data) => connector.upsertDisclosuresSubmission(ctx, data).void
      case None       => connector.deleteDisclosuresSubmission(ctx)
    }

  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      updatedContext  <- updateJourneyContextWithSingleBusinessId(ctx)
      existingAnswers <- EitherT(connector.getAnnualSummaries(updatedContext))
      requestBody   = CreateAmendSEAnnualSubmissionRequestData.mkNicsClassFourSingleBusinessRequestBody(answers, existingAnswers)
      upsertRequest = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, updatedContext.businessId, requestBody)
      _ <- EitherT[Future, ServiceError, Unit](connector.createAmendSEAnnualSubmission(upsertRequest))
    } yield ()

  private def updateJourneyContextWithSingleBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[JourneyContextWithNino] =
    EitherT(businessConnector.getBusinesses(ctx.nino).value.map {
      case Right(res: api_1171.SuccessResponseSchema) =>
        res.taxPayerDisplayResponse.getMaybeSingleBusinessId match {
          case Some(id) => Right(ctx(newId = id))
          case None     => Left(BusinessNotFoundError(ctx.businessId))
        }
      case Left(error) => Left(error)
    })

  // TODO SASS-9573 save multiple businesses
  // Class 4 MULTIPLE - Save data and Clear changed data
//  def saveClass4MultipleBusinesses(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
//    val clearAnyChangedData = clearOtherExistingClass4Data(ctx, idsToNotClear = List(ctx.businessId.value)).void
//    val saveNewData = for {
//      existingAnswers <- connector.getAnnualSummaries(ctx).map(_.getOrElse(api_1803.SuccessResponseSchema.empty))
//      requestBody   = CreateAmendSEAnnualSubmissionRequestData.mkNicsClassFourSingleBusinessRequestBody(answers, existingAnswers)
//      upsertRequest = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, requestBody)
//      _ <- connector.createAmendSEAnnualSubmission(upsertRequest)
//    } yield ()
//    EitherT.rightT[Future, ServiceError](clearAnyChangedData.map(_ => saveNewData))
//  }
//  private def clearOtherExistingClass4Data(ctx: JourneyContextWithNino, idsToNotClear: List[String])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
//    for {
//      allUserBusinessIds <- businessConnector.getBusinesses(ctx.nino).map(_.taxPayerDisplayResponse.businessData.map(_.map(_.incomeSourceId)))
//      idsToClearData = allUserBusinessIds.filterNot(id => idsToNotClear.contains(id))
//      _              = idsToClearData.traverse(_.map(id => clearClass4Data(ctx.copy(businessId = BusinessId(id)))))
//    } yield ()
//
//  private def clearClass4Data(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
//    val result = connector.getAnnualSummaries(ctx) flatMap {
//      case Right(summary) if summary.hasNICsClassFourData =>
//        val optionalRequestBody = CreateAmendSEAnnualSubmissionRequestData.mkEmptyNicsClassFourSingleBusinessRequestBody(summary)
//        upsertOrDeleteClass4Data(optionalRequestBody, ctx).value // Clear data: upsert if other data, delete if now empty
//      case Right(_)                                 => EitherT.rightT[Future, ServiceError](()).value // No AnnualSummary data to clear
//      case Left(error) if error.status == NOT_FOUND => EitherT.rightT[Future, ServiceError](()).value // No AnnualSummary data to clear
//      case leftResult                               => Future(leftResult.void)                        // Error
//    }
//    EitherT(result)
//  }
//
//  private def upsertOrDeleteClass4Data(maybeClass4RequestBody: Option[CreateAmendSEAnnualSubmissionRequestBody], ctx: JourneyContextWithNino)(implicit
//      hc: HeaderCarrier): ApiResultT[Unit] =
//    maybeClass4RequestBody match {
//      case Some(requestBody) =>
//        val requestData = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, requestBody)
//        EitherT.rightT[Future, ServiceError](connector.createAmendSEAnnualSubmission(requestData))
//      case None => connector.deleteDisclosuresSubmission(ctx)
//    }
  //

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] =
    for {
      apiAnswers <- connector.getDisclosuresSubmission(ctx)
      dbAnswers  <- repository.getAnswers[NICsStorageAnswers](ctx.toJourneyContext(JourneyName.NationalInsuranceContributions))
    } yield NICsAnswers.mkPriorClass2Data(apiAnswers, dbAnswers)

}
