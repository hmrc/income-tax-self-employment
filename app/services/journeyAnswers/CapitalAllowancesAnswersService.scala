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
import cats.implicits._
import connectors.IFSConnector
import controllers.actions.AuthorisedAction
import controllers.getCapitalAllowanceBodyWithCtx
import models.common.JourneyName.{
  AnnualInvestmentAllowance,
  BalancingAllowance,
  CapitalAllowancesTailoring,
  ElectricVehicleChargePoints,
  SpecialTaxSites,
  StructuresBuildings,
  WritingDownAllowance,
  ZeroEmissionCars,
  ZeroEmissionGoodsVehicle
}
import models.common._
import models.connector.api_1802.request.{CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803
import models.connector.api_1803.SuccessResponseSchema
import models.database.JourneyAnswers
import models.database.capitalAllowances._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.FrontendAnswers
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.ElectricVehicleChargePointsAnswers
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers
import models.frontend.capitalAllowances.structuresBuildings.NewStructuresBuildingsAnswers
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehicleAnswers
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.Results.NoContent
import play.api.mvc.{AnyContent, Result}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait CapitalAllowancesAnswersService {
  def saveAnswers[A: Writes, B <: FrontendAnswers[A]: Reads](journeyName: JourneyName, taxYear: TaxYear, businessId: BusinessId, nino: Nino)(implicit
      hc: HeaderCarrier,
      user: AuthorisedAction.User[AnyContent],
      logger: Logger): Future[Result]
  def persistAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A): ApiResultT[Unit]
  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]]
  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]]
  def getZeroEmissionGoodsVehicle(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]]
  def getElectricVehicleChargePoints(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]]
  def getBalancingAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]]
  def getAnnualInvestmentAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]]
  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]]
  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]]
  def getStructuresBuildings(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NewStructuresBuildingsAnswers]]
}

@Singleton
class CapitalAllowancesAnswersServiceImpl @Inject() (connector: IFSConnector, repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends CapitalAllowancesAnswersService {

  def saveAnswers[A: Writes, B <: FrontendAnswers[A]: Reads](journeyName: JourneyName, taxYear: TaxYear, businessId: BusinessId, nino: Nino)(implicit
      hc: HeaderCarrier,
      user: AuthorisedAction.User[AnyContent],
      logger: Logger): Future[Result] =
    getCapitalAllowanceBodyWithCtx[A, B](taxYear, businessId, nino) { (ctx, answers) =>
      for {
        maybeCurrent <- getAnnualSummaries(JourneyContextWithNino(ctx.taxYear, ctx.businessId, ctx.mtditid, ctx.nino))
        existingData          = maybeCurrent.getOrElse(api_1803.SuccessResponseSchema.empty)
        updatedSubmissionBody = createUpdatedRequestBody(existingData, answers)
        _ <- createUpdateOrDeleteApiAnnualSummaries(ctx, updatedSubmissionBody)
        _ <- answers.toDbModel.traverse(dbAnswers => persistAnswers(ctx.businessId, ctx.taxYear, ctx.mtditid, journeyName, dbAnswers))
      } yield NoContent
    }

  private def createUpdatedRequestBody[A](existingData: api_1803.SuccessResponseSchema,
                                          answers: FrontendAnswers[A]): Option[CreateAmendSEAnnualSubmissionRequestBody] = {
    val requestBody             = existingData.toRequestBody
    val updatedAnnualAllowances = answers.toDownStreamAnnualAllowances(requestBody.annualAllowances).returnNoneIfEmpty
    CreateAmendSEAnnualSubmissionRequestBody.mkRequest(requestBody.annualAdjustments, updatedAnnualAllowances, requestBody.annualNonFinancials)
  }

  private def createUpdateOrDeleteApiAnnualSummaries(ctx: JourneyContextWithNino, requestBody: Option[CreateAmendSEAnnualSubmissionRequestBody])(
      implicit hc: HeaderCarrier): ApiResultT[Unit] =
    requestBody match {
      case Some(body) =>
        val requestData = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, body)
        EitherT(connector.createAmendSEAnnualSubmission(requestData))
      case None => EitherT.fromEither(().asRight[ServiceError]) // TODO Delete when API endpoint is set up
    }

  def persistAnswers[A: Writes](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A): ApiResultT[Unit] =
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))

  private def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[api_1803.SuccessResponseSchema]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(data)                              => Right(Some(data))
      case Left(error) if error.status == NOT_FOUND => Right(None)
      case Left(error)                              => Left(error)
    }
    EitherT(result)
  }

  private def getDbAnswers(ctx: JourneyContextWithNino, journey: JourneyName): ApiResultT[Option[JourneyAnswers]] =
    repository.get(ctx.toJourneyContext(journey))

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    for {
      maybeDbAnswers           <- repository.get(ctx.toJourneyContext(CapitalAllowancesTailoring))
      existingTailoringAnswers <- getPersistedAnswers[CapitalAllowancesTailoringAnswers](maybeDbAnswers)
    } yield existingTailoringAnswers

  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, ZeroEmissionCars)
      dbAnswers   <- getPersistedAnswers[ZeroEmissionCarsDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[ZeroEmissionCarsAnswers]]

  def getZeroEmissionGoodsVehicle(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, ZeroEmissionGoodsVehicle)
      dbAnswers   <- getPersistedAnswers[ZeroEmissionGoodsVehicleDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[ZeroEmissionGoodsVehicleAnswers]]

  def getElectricVehicleChargePoints(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, ElectricVehicleChargePoints)
      dbAnswers   <- getPersistedAnswers[ElectricVehicleChargePointsDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[ElectricVehicleChargePointsAnswers]]

  def getBalancingAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, BalancingAllowance)
      dbAnswers   <- getPersistedAnswers[BalancingAllowanceDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[BalancingAllowanceAnswers]]

  def getAnnualInvestmentAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, AnnualInvestmentAllowance)
      dbAnswers   <- getPersistedAnswers[AnnualInvestmentAllowanceDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[AnnualInvestmentAllowanceAnswers]]

  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, WritingDownAllowance)
      dbAnswers   <- getPersistedAnswers[WritingDownAllowanceDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[WritingDownAllowanceAnswers]]

  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, SpecialTaxSites)
      dbAnswers   <- getPersistedAnswers[SpecialTaxSitesDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[SpecialTaxSitesAnswers]]

  def getStructuresBuildings(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NewStructuresBuildingsAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, StructuresBuildings)
      dbAnswers   <- getPersistedAnswers[NewStructuresBuildingsDb](maybeData)
      fullAnswers <- createFullJourneyAnswersWithApiData(ctx, dbAnswers)
    } yield fullAnswers.asInstanceOf[Option[NewStructuresBuildingsAnswers]]

  private def createFullJourneyAnswersWithApiData[A](ctx: JourneyContextWithNino, dbAnswers: Option[A])(implicit
      hc: HeaderCarrier): ApiResultT[Option[FrontendAnswers[A]]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => buildJourneyAnswers(dbAnswers, annualSummaries)
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  private def buildJourneyAnswers[A](dbAnswers: Option[A], annualSummaries: SuccessResponseSchema): Option[FrontendAnswers[A]] =
    dbAnswers
      .collect {
        case answers: ZeroEmissionCarsDb            => ZeroEmissionCarsAnswers(answers, annualSummaries)
        case answers: ZeroEmissionGoodsVehicleDb    => ZeroEmissionGoodsVehicleAnswers(answers, annualSummaries)
        case answers: ElectricVehicleChargePointsDb => ElectricVehicleChargePointsAnswers(answers, annualSummaries)
        case answers: BalancingAllowanceDb          => BalancingAllowanceAnswers(answers, annualSummaries)
        case answers: AnnualInvestmentAllowanceDb   => AnnualInvestmentAllowanceAnswers(answers, annualSummaries)
        case _: WritingDownAllowanceDb              => WritingDownAllowanceAnswers(annualSummaries)
        case answers: SpecialTaxSitesDb             => SpecialTaxSitesAnswers(answers, annualSummaries)
        case answers: NewStructuresBuildingsDb      => NewStructuresBuildingsAnswers(answers, annualSummaries)
      }
      .map(_.asInstanceOf[FrontendAnswers[A]])

}
