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
import connectors.IFS.IFSConnector
import models.common.JourneyName.NationalInsuranceContributions
import models.common._
import models.connector._
import models.connector.api_1638.RequestSchemaAPI1638
import models.database.nics.NICsStorageAnswers
import models.database.nics.NICsStorageAnswers.journeyIsYesButNoneAreExemptStorageAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
import models.frontend.nics.{NICsAnswers, NICsClass2Answers, NICsClass4Answers}
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import services.BusinessService
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait NICsAnswersService {
  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)
                       (implicit hc: HeaderCarrier): ApiResultT[Unit]

  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)
                              (implicit hc: HeaderCarrier): ApiResultT[Unit]

  def saveClass4MultipleBusinessOrNoExemptionJourneys(ctx: JourneyContextWithNino, answers: NICsClass4Answers)
                                                     (implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getAnswers(ctx: JourneyContextWithNino)
                (implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]]
}

@Singleton
class NICsAnswersServiceImpl @Inject()(connector: IFSConnector,
                                       repository: JourneyAnswersRepository,
                                       businessService: BusinessService)(implicit ec: ExecutionContext) extends NICsAnswersService {

  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsClass2Answers)
                       (implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      existingAnswers <- connector.getDisclosuresSubmission(ctx)
      upsertRequest = RequestSchemaAPI1638.mkRequestBody(answers, existingAnswers)
      _ <- upsertOrDeleteClass2Data(upsertRequest, ctx)
      storageAnswers = NICsStorageAnswers.fromJourneyAnswers(answers)
      result <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.NationalInsuranceContributions), Json.toJson(storageAnswers))
    } yield result

  private def upsertOrDeleteClass2Data(maybeClass2Nics: Option[RequestSchemaAPI1638], ctx: JourneyContextWithNino)
                                      (implicit hc: HeaderCarrier): ApiResultT[Unit] =
    maybeClass2Nics match {
      case Some(data) => connector.upsertDisclosuresSubmission(ctx, data).void
      case None => connector.deleteDisclosuresSubmission(ctx)
    }

  def saveClass4SingleBusiness(ctx: JourneyContextWithNino, answers: NICsClass4Answers)
                              (implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      _ <- businessService.getBusiness(ctx.businessId, ctx.mtditid, ctx.nino)
      exemptionAnswer = Class4ExemptionAnswers(ctx.businessId, answers.class4NICs, answers.class4ExemptionReason)
      result <- saveClass4BusinessData(ctx, exemptionAnswer)
    } yield result

  def saveClass4MultipleBusinessOrNoExemptionJourneys(ctx: JourneyContextWithNino, answers: NICsClass4Answers)
                                                     (implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val multipleBusinessExemptionAnswers = answers.cleanUpExemptionListsFromFE.toMultipleBusinessesAnswers
    val idsWithExemption = multipleBusinessExemptionAnswers.map(_.businessId)
    val dbAnswers = if (answers.journeyIsYesButNoneAreExempt) journeyIsYesButNoneAreExemptStorageAnswers else NICsStorageAnswers(None, None)

    val saveAllNewExemptionAnswers: EitherT[Future, ServiceError, Unit] = multipleBusinessExemptionAnswers
      .traverse { answer =>
        val businessContext = ctx(newId = answer.businessId)
        saveClass4BusinessData(businessContext, answer)
      }
      .map(_ => ())

    for {
      _ <- repository.upsertAnswers(
        ctx = JourneyContext(ctx.taxYear, ctx.businessId, ctx.mtditid, NationalInsuranceContributions),
        newData = Json.toJson(dbAnswers)
      )
      _ <- updateIdsToNoClass4Exemption(ctx, idsWithExemption)
      result <- saveAllNewExemptionAnswers
    } yield result
  }

  private def updateIdsToNoClass4Exemption(ctx: JourneyContextWithNino, idsWithExemption: List[BusinessId])
                                          (implicit hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      allUserBusinessIds <- businessService.getUserBusinessIds(ctx.mtditid, ctx.nino)
      idsWithNoExemption = allUserBusinessIds.filterNot(idsWithExemption.contains(_))
      noClass4ExemptionAnswer = Class4ExemptionAnswers(ctx.businessId, class4Exempt = false, None)
      result <- idsWithNoExemption
        .traverse { id =>
          val updatedCtx = ctx(newId = BusinessId(id.value))
          saveClass4BusinessData(updatedCtx, noClass4ExemptionAnswer)
        }
        .map(_ => ())
    } yield result

  def saveClass4BusinessData(ctx: JourneyContextWithNino, businessExemptionAnswer: Class4ExemptionAnswers)
                            (implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val submissionBody = for {
      maybeAnnualSummaries <- connector.getAnnualSummaries(ctx)
      updatedAnnualSubmissionBody = handleAnnualSummariesForResubmission[Unit](maybeAnnualSummaries, businessExemptionAnswer)
    } yield updatedAnnualSubmissionBody

    EitherT(submissionBody).flatMap(connector.createUpdateOrDeleteApiAnnualSummaries(ctx, _))
  }

  def getAnswers(ctx: JourneyContextWithNino)
                (implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] = {
    def getAnnualSummariesWithIds(businessIds: List[BusinessId]): ApiResultT[List[(api_1803.SuccessResponseSchema, BusinessId)]] =
      businessIds.traverse { id =>
        val updatedJourneyContext = ctx(newId = id)
        EitherT(connector.getAnnualSummaries(updatedJourneyContext).map(_.map((_, id))))
      }

    for {
      allUserBusinessIds <- businessService.getUserBusinessIds(ctx.mtditid, ctx.nino)
      allAnnualSummariesWithIds <- getAnnualSummariesWithIds(allUserBusinessIds)
      disclosures <- connector.getDisclosuresSubmission(ctx)
      dbAnswers <- repository.getAnswers[NICsStorageAnswers](ctx.toJourneyContext(JourneyName.NationalInsuranceContributions))
    } yield NICsAnswers.mkPriorData(disclosures, allAnnualSummariesWithIds, dbAnswers)
  }

}
