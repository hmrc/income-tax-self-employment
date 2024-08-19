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

import cats.implicits.{toFunctorOps, toTraverseOps}
import connectors.IFSConnector.Api1803Response
import connectors.{IFSBusinessDetailsConnector, IFSConnector}
import models.common._
import models.connector.api_1638.RequestSchemaAPI1638
import models.connector.api_1802.request.{AnnualNonFinancials, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803.AnnualNonFinancialsType
import models.database.nics.NICsStorageAnswers
import models.domain.ApiResultT
import models.frontend.nics.NICsAnswers
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait NICsAnswersService {
  def saveAnswers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]]
}

@Singleton
class NICsAnswersServiceImpl @Inject() (connector: IFSConnector, businessConnector: IFSBusinessDetailsConnector, repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends NICsAnswersService {

  def saveAnswers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    if (answers.isClass2) saveClass2Answers(ctx, answers) else saveClass4Answers(ctx, answers)

  def saveClass2Answers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
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





  def saveClass4Answers(ctx: JourneyContextWithNino, answers: NICsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val ids: ApiResultT[List[String]] = businessConnector.getBusinesses(ctx.nino).map(_.taxPayerDisplayResponse.businessData.map(_.map(_.incomeSourceId)))
    val multipleBusinesses: ApiResultT[Boolean] = ids.map(_.length != 1)
    val summaries: ApiResultT[List[Api1803Response]] = ids.traverse(idlist => idlist.flatMap(id => connector.getAnnualSummaries(ctx.copy(businessId = BusinessId(id)))))
    for {
      // 1. Get all businessIds
      ids <- businessConnector.getBusinesses(ctx.nino).map(_.taxPayerDisplayResponse.businessData.map(_.map(_.incomeSourceId)))
      // 2. Check if single or multiple multipleBusinesses = ids.map(_.length != 1)
      // 3. If Single -> get and upsert single (delete extras)
      getUpsertDelete <- saveClassFourSingleBusiness(ids)
      // 2. Get summary for each businessId
      summaries <- ids.traverse(id => connector.getAnnualSummaries(ctx.copy(businessId = id)))
      // 3. Edit request body for each BID with FE answers
      // 4. Create/Update for FE answers BIDs, Delete any which no longer have FE answers
      existingAnswers <- connector.getAnnualSummaries(ctx)
      upsertRequest = CreateAmendSEAnnualSubmissionRequestData.mkNicsClassFourRequestBody(answers, existingAnswers)
      _ <- upsertOrDeleteClass4Data(upsertRequest, ctx)
      storageAnswers = NICsStorageAnswers.fromJourneyAnswers(answers)
      _ <- repository.upsertAnswers(ctx.toJourneyContext(JourneyName.NationalInsuranceContributions), Json.toJson(storageAnswers))
    } yield ()
  }

  private def saveClassFourSingleBusiness(ctx: JourneyContextWithNino, answers: NICsAnswers, userBusinessIds: List[String])(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    // 1. get ids to delete
    val idsToClearData = userBusinessIds.filterNot(_ == ctx.businessId.value)
    for {
      getExistingAnswersForAllIds <- userBusinessIds.traverse(id => connector.getAnnualSummaries(ctx(newId = BusinessId(id))))
         upsertRequest = CreateAmendSEAnnualSubmissionRequestData.mkNicsClassFourRequestBody(answers, existingAnswers)
         _ <- getExistingAnswersForAllIds.traverse(upsertRequest => upsertOrDeleteClass4Data(upsertRequest, ctx))
      existingAnswers <- userBusinessIds.traverse(id => connector.getAnnualSummaries(ctx.copy(businessId = BusinessId(id))))
    } yield ()
  }

  private def getDataUpsertOrDelete(ctx: JourneyContextWithNino, answers: NICsAnswers, businessId: String)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val result: Future[Api1803Response] = for {
    // 1. get annual summaries for id
      maybePreviousData <- connector.getAnnualSummaries(ctx(newId = BusinessId(businessId)))
updatedAnnualNonFinancials = AnnualNonFinancials
      // 2. if id == ctx, make body with new answers, make request
      // 3. if id != ctx, make body with empty answers, make request
      // 4. Upsert/Delete
    } yield maybePreviousData
  }

  private def upsertOrDeleteClass4Data(maybeClass2Nics: Option[RequestSchemaAPI1638], ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    maybeClass2Nics match {
      case Some(data) => connector.createAmendSEAnnualSubmission(ctx, data).void
      case None       => connector.deleteDisclosuresSubmission(ctx)
    }






  // TODO this is only for class 2 answers
  def getAnswers(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NICsAnswers]] =
    for {
      apiAnswers <- connector.getDisclosuresSubmission(ctx)
      dbAnswers  <- repository.getAnswers[NICsStorageAnswers](ctx.toJourneyContext(JourneyName.NationalInsuranceContributions))
    } yield NICsAnswers.mkPriorData(apiAnswers, dbAnswers)

}
