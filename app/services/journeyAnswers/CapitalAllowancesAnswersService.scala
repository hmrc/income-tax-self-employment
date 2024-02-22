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

package services.journeyAnswers

import cats.data.EitherT
import connectors.SelfEmploymentConnector
import models.common.JourneyName.{CapitalAllowancesTailoring, ZeroEmissionCars}
import models.common._
import models.connector.Api1802AnnualAllowancesBuilder
import models.connector.api_1802.request.{AnnualAllowances, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.database.JourneyAnswers
import models.database.capitalAllowances.ZeroEmissionCarsDb
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait CapitalAllowancesAnswersService {
  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit]
  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]]
  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]]
}

@Singleton
class CapitalAllowancesAnswersServiceImpl @Inject() (connector: SelfEmploymentConnector, repository: JourneyAnswersRepository)(implicit
    ec: ExecutionContext)
    extends CapitalAllowancesAnswersService {

  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {

    val updatedAnnualAllowances = AnnualAllowances.fromFrontendModel(answers)
    val upsertBody              = CreateAmendSEAnnualSubmissionRequestBody(None, Some(updatedAnnualAllowances), None)
    val requestData             = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, upsertBody)

    val result = connector.createAmendSEAnnualSubmission(requestData).map(_ => ())

    EitherT.right[ServiceError](result)
  }

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] =
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))

  private def getDbAnswers(ctx: JourneyContextWithNino, journey: JourneyName): ApiResultT[Option[JourneyAnswers]] =
    repository.get(ctx.toJourneyContext(journey))

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    for {
      maybeDbAnswers           <- repository.get(ctx.toJourneyContext(CapitalAllowancesTailoring))
      existingTailoringAnswers <- getPersistedAnswers[CapitalAllowancesTailoringAnswers](maybeDbAnswers)
    } yield existingTailoringAnswers

  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]] =
    for {
      dbAnswers   <- getZeroEmissionCarsDbAnswers(ctx)
      fullAnswers <- getFullZeroEmissionCarsAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getZeroEmissionCarsDbAnswers(ctx: JourneyContextWithNino): ApiResultT[Option[ZeroEmissionCarsDb]] =
    for {
      maybeData <- getDbAnswers(ctx, ZeroEmissionCars)
      dbAnswers <- getPersistedAnswers[ZeroEmissionCarsDb](maybeData)
    } yield dbAnswers

  private def getFullZeroEmissionCarsAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[ZeroEmissionCarsDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) =>
        println("--- ann " + annualSummaries)
        dbAnswers.map(answers => ZeroEmissionCarsAnswers(answers, annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

}
