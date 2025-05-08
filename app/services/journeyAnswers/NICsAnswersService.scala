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

package services.journeyAnswers

import cats.data.EitherT
import cats.implicits.{toFunctorOps, toTraverseOps}
import config.AppConfig
import connectors.HIP.BusinessDetailsConnector
import connectors.IFS.{IFSBusinessDetailsConnector, IFSConnector}
import models.common.JourneyName.NationalInsuranceContributions
import models.common._
import models.connector._
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.businessDetailsConnector.BusinessDetailsSuccessResponseSchema
import models.database.nics.NICsStorageAnswers
import models.database.nics.NICsStorageAnswers.journeyIsYesButNoneAreExemptStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.error.ServiceError.BusinessNotFoundError
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
import models.frontend.nics.{NICsAnswers, NICsClass2Answers, NICsClass4Answers}
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait NICsAnswersService {
  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def saveClass4MultipleBusinessOrNoExemptionJourneys(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]]
}

@Singleton
class NICsAnswersServiceImpl @Inject() (connector: IFSConnector,
                                        ifsBusinessDetailsConnector: IFSBusinessDetailsConnector,
                                        hipBusinessDetailsConnector: BusinessDetailsConnector,
                                        repository: JourneyAnswersRepository,
                                        businessService: BusinessService,
                                        appConfig: AppConfig)(implicit ec: ExecutionContext)
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

  def saveClass4MultipleBusinessOrNoExemptionJourneys(ctx: JourneyContextWithNino, answers: NICsClass4Answers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val journeyIsYesButNoneAreExempt     = answers.journeyIsYesButNoneAreExempt
    val multipleBusinessExemptionAnswers = answers.cleanUpExemptionListsFromFE.toMultipleBusinessesAnswers
    val idsWithExemption                 = multipleBusinessExemptionAnswers.map(_.businessId.value)
    val updateOtherIdsToNotExempt        = updateIdsToNoClass4Exemption(ctx, idsWithExemption = idsWithExemption)

    val saveAllNewExemptionAnswers: EitherT[Future, ServiceError, Unit] = multipleBusinessExemptionAnswers
      .traverse { answer =>
        val businessContext = ctx(newId = answer.businessId)
        saveClass4BusinessData(businessContext, answer)
      }
      .map(_ => ())

    for {
      _      <- updateDatabaseAnswers(ctx, journeyIsYesButNoneAreExempt)
      _      <- updateOtherIdsToNotExempt
      result <- saveAllNewExemptionAnswers
    } yield result
  }

  private def updateDatabaseAnswers(ctx: JourneyContextWithNino, journeyIsYesButNoneAreExempt: Boolean): ApiResultT[Unit] = {
    val dbAnswers = if (journeyIsYesButNoneAreExempt) journeyIsYesButNoneAreExemptStorageAnswers else NICsStorageAnswers(None, None)
    repository.upsertAnswers(JourneyContext(ctx.taxYear, ctx.businessId, ctx.mtditid, NationalInsuranceContributions), Json.toJson(dbAnswers))
  }

  private def getBusinessDetails(businessId: Option[BusinessId], mtditid: Mtditid, nino: Nino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[BusinessDetailsSuccessResponseSchema] =
    if (appConfig.hipMigration1171Enabled) {
      hipBusinessDetailsConnector.getBusinessDetails(businessId, mtditid, nino)
    } else {
      ifsBusinessDetailsConnector.getBusinesses(nino)
    }

  private def updateIdsToNoClass4Exemption(ctx: JourneyContextWithNino, idsWithExemption: List[String])(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      allUserBusinessIds <- getBusinessDetails(Some(ctx.businessId), ctx.mtditid, ctx.nino)
        .map(_.taxPayerDisplayResponse.businessData.map(_.map(_.incomeSourceId)))
      idsWithNoExemption      = allUserBusinessIds.map(_.filterNot(idsWithExemption.contains(_)))
      noClass4ExemptionAnswer = Class4ExemptionAnswers(ctx.businessId, class4Exempt = false, None)
      result <- idsWithNoExemption
        .traverse(ids =>
          ids.traverse { id =>
            val updatedCtx = ctx(newId = BusinessId(id))
            saveClass4BusinessData(updatedCtx, noClass4ExemptionAnswer)
          })
        .map(_ => ())
    } yield result

  def saveClass4BusinessData(ctx: JourneyContextWithNino, businessExemptionAnswer: Class4ExemptionAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- connector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[Unit](maybeAnnualSummaries, businessExemptionAnswer)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(connector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  private def updateJourneyContextWithSingleBusinessId(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[JourneyContextWithNino] =
    EitherT(getBusinessDetails(Some(ctx.businessId), ctx.mtditid, ctx.nino).value.map {
      case Right(res: BusinessDetailsSuccessResponseSchema) =>
        res.taxPayerDisplayResponse.getMaybeSingleBusinessId match {
          case Some(id) => Right(ctx(newId = id))
          case None     => Left(BusinessNotFoundError(ctx.businessId))
        }
      case Left(error) => Left(error)
    })

  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] = {

    def getAnnualSummariesWithIds(businessIds: List[BusinessId]): ApiResultT[List[(api_1803.SuccessResponseSchema, BusinessId)]] =
      businessIds.traverse { id =>
        val updatedJourneyContext = ctx(newId = id)
        EitherT(connector.getAnnualSummaries(updatedJourneyContext).map(_.map((_, id))))
      }

    for {
      allUserBusinessIds        <- businessService.getUserBusinessIds(ctx.businessId, ctx.mtditid, ctx.nino)
      allAnnualSummariesWithIds <- getAnnualSummariesWithIds(allUserBusinessIds)
      disclosures               <- connector.getDisclosuresSubmission(ctx)
      dbAnswers                 <- repository.getAnswers[NICsStorageAnswers](ctx.toJourneyContext(JourneyName.NationalInsuranceContributions))
    } yield NICsAnswers.mkPriorData(disclosures, allAnnualSummariesWithIds, dbAnswers)
  }
}
