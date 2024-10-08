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
import cats.implicits.{toFunctorOps, toTraverseOps}
import connectors.{IFSBusinessDetailsConnector, IFSConnector}
import models.common._
import models.connector._
import models.connector.api_1638.RequestSchemaAPI1638
import models.database.nics.NICsStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
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
  def saveClass4MultipleBusinesses(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
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
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.NationalInsuranceContributions), Json.toJson(storageAnswers))
    } yield result

  private def upsertOrDeleteClass2Data(maybeClass2Nics: Option[RequestSchemaAPI1638], ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    maybeClass2Nics match {
      case Some(data) => connector.upsertDisclosuresSubmission(ctx, data).void
      case None       => connector.deleteDisclosuresSubmission(ctx)
    }

  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      updatedContext <- updateJourneyContextWithSingleBusinessId(ctx)
      exemptionAnswer = Class4ExemptionAnswers(updatedContext.businessId, answers.class4NICs, answers.class4ExemptionReason)
      result <- saveClass4BusinessData(updatedContext, exemptionAnswer)
    } yield result

  def saveClass4MultipleBusinesses(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val multipleBusinessExemptionAnswers = answers.cleanUpExemptionListsFromFE.toMultipleBusinessesAnswers
    val idsWithExemption                 = multipleBusinessExemptionAnswers.map(_.businessId.value)
    val updateOtherIdsToNotExempt        = clearOtherExistingClass4Data(ctx, idsToNotClear = idsWithExemption)

    val saveAllNewExemptionAnswers: EitherT[Future, ServiceError, Unit] = multipleBusinessExemptionAnswers
      .traverse { answer =>
        val businessContext = ctx(newId = answer.businessId)
        saveClass4BusinessData(businessContext, answer)
      }
      .map(_ => ())

    for {
      _      <- updateOtherIdsToNotExempt
      result <- saveAllNewExemptionAnswers
    } yield result
  }

  private def saveClass4BusinessData(ctx: JourneyContextWithNino, businessExemptionAnswer: Class4ExemptionAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- connector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[Unit](maybeAnnualSummaries, businessExemptionAnswer)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(connector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  private def clearOtherExistingClass4Data(ctx: JourneyContextWithNino, idsToNotClear: List[String])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      allUserBusinessIds <- businessConnector.getBusinesses(ctx.nino).map(_.taxPayerDisplayResponse.businessData.map(_.map(_.incomeSourceId)))
      idsToClearData = allUserBusinessIds.map(_.filterNot(idsToNotClear.contains(_)))
      result <- idsToClearData.traverse(ids => ids.traverse(id => setExistingClass4DataToNotExempt(ctx(newId = BusinessId(id))))).map(_ => ())
    } yield result

  private def setExistingClass4DataToNotExempt(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val result = connector.getAnnualSummaries(ctx) flatMap {
      case Right(summary) if summary.hasNICsClassFourData =>
        val noExemptionAnswer  = Class4ExemptionAnswers(ctx.businessId, class4Exempt = false, None)
        val updatedRequestBody = createUpdatedAnnualSummariesRequestBody(summary, noExemptionAnswer)
        connector.createUpdateOrDeleteApiAnnualSummaries(ctx, updatedRequestBody).value
      case Right(_)                                 => Future.successful(Right[ServiceError, Unit](()))
      case Left(error) if error.status == NOT_FOUND => Future.successful(Right[ServiceError, Unit](()))
      case leftResult                               => Future(leftResult.void)
    }
    EitherT(result)
  }

  private def updateJourneyContextWithSingleBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[JourneyContextWithNino] =
    EitherT(businessConnector.getBusinesses(ctx.nino).value.map {
      case Right(res: api_1171.SuccessResponseSchema) =>
        res.taxPayerDisplayResponse.getMaybeSingleBusinessId match {
          case Some(id) => Right(ctx(newId = id))
          case None     => Left(BusinessNotFoundError(ctx.businessId))
        }
      case Left(error) => Left(error)
    })

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] =
    for {
      apiAnswers <- connector.getDisclosuresSubmission(ctx)
      dbAnswers  <- repository.getAnswers[NICsStorageAnswers](ctx.toJourneyContext(JourneyName.NationalInsuranceContributions))
    } yield NICsAnswers.mkPriorClass2Data(apiAnswers, dbAnswers)

}
