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
import models.common.JourneyName.{AnnualInvestmentAllowance, BalancingAllowance, CapitalAllowancesTailoring, ElectricVehicleChargePoints, SpecialTaxSites, WritingDownAllowance, ZeroEmissionCars, ZeroEmissionGoodsVehicle}
import models.common._
import models.connector.api_1802.request.{AnnualAllowances, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.{Api1802AnnualAllowancesBuilder, api_1803}
import models.database.JourneyAnswers
import models.database.capitalAllowances._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.{AnnualInvestmentAllowanceAnswers, AnnualInvestmentAllowanceDb}
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.ElectricVehicleChargePointsAnswers
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehicleAnswers
import play.api.libs.json.{Json, Writes}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait CapitalAllowancesAnswersService {
  def saveAnnualAllowances(ctx: JourneyContextWithNino, updatedAnnualAllowances: AnnualAllowances)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit]
  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[api_1803.SuccessResponseSchema]]
  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]]
  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]]
  def getZeroEmissionGoodsVehicle(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]]
  def getElectricVehicleChargePoints(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]]
  def getBalancingAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]]
  def getAnnualInvestmentAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]]
  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]]
  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]]

}

@Singleton
class CapitalAllowancesAnswersServiceImpl @Inject() (connector: SelfEmploymentConnector, repository: JourneyAnswersRepository)(implicit
    ec: ExecutionContext)
    extends CapitalAllowancesAnswersService {

  def saveAnnualAllowances(ctx: JourneyContextWithNino, updatedAnnualAllowances: AnnualAllowances)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val upsertBody  = CreateAmendSEAnnualSubmissionRequestBody(None, Some(updatedAnnualAllowances), None)
    val requestData = CreateAmendSEAnnualSubmissionRequestData(ctx.taxYear, ctx.nino, ctx.businessId, upsertBody)
    val result      = connector.createAmendSEAnnualSubmission(requestData).map(_ => ())
    EitherT.right[ServiceError](result)
  }

  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val updatedAnnualAllowances = AnnualAllowances.fromFrontendModel(answers)
    saveAnnualAllowances(ctx, updatedAnnualAllowances)
  }

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] =
    repository.upsertAnswers(JourneyContext(taxYear, businessId, mtditid, journey), Json.toJson(answers))

  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[api_1803.SuccessResponseSchema]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => Some(annualSummaries)
      case Left(_)                => None
    }
    EitherT.liftF(result)
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
      fullAnswers <- getZeroEmissionCarsWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getZeroEmissionCarsWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[ZeroEmissionCarsDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(answers => ZeroEmissionCarsAnswers(answers, annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  def getZeroEmissionGoodsVehicle(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, ZeroEmissionGoodsVehicle)
      dbAnswers   <- getPersistedAnswers[ZeroEmissionGoodsVehicleDb](maybeData)
      fullAnswers <- getZeroEmissionGoodsVehicleWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getZeroEmissionGoodsVehicleWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[ZeroEmissionGoodsVehicleDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(answers => ZeroEmissionGoodsVehicleAnswers(answers, annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  def getElectricVehicleChargePoints(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, ElectricVehicleChargePoints)
      dbAnswers   <- getPersistedAnswers[ElectricVehicleChargePointsDb](maybeData)
      fullAnswers <- getElectricVehicleChargePointsWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getElectricVehicleChargePointsWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[ElectricVehicleChargePointsDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(answers => ElectricVehicleChargePointsAnswers(answers, annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  def getBalancingAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, BalancingAllowance)
      dbAnswers   <- getPersistedAnswers[BalancingAllowanceDb](maybeData)
      fullAnswers <- getBalancingAllowanceWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getBalancingAllowanceWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[BalancingAllowanceDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(answers => BalancingAllowanceAnswers(answers, annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  def getAnnualInvestmentAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, AnnualInvestmentAllowance)
      dbAnswers   <- getPersistedAnswers[AnnualInvestmentAllowanceDb](maybeData)
      fullAnswers <- getAnnualInvestmentAllowanceWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getAnnualInvestmentAllowanceWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[AnnualInvestmentAllowanceDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Left(_) => None
      case Right(annualSummaries) =>
        dbAnswers.map(answers =>
          AnnualInvestmentAllowanceAnswers(
            answers.annualInvestmentAllowance,
            annualSummaries.annualAllowances.flatMap(_.annualInvestmentAllowance)
          ))
    }
    EitherT.liftF(result)
  }

  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, WritingDownAllowance)
      dbAnswers   <- getPersistedAnswers[WritingDownAllowanceDb](maybeData)
      fullAnswers <- getWritingDownAllowanceWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getWritingDownAllowanceWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[WritingDownAllowanceDb])(implicit
      hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(_ => WritingDownAllowanceAnswers(annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }

  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]] =
    for {
      maybeData   <- getDbAnswers(ctx, SpecialTaxSites)
      dbAnswers   <- getPersistedAnswers[SpecialTaxSitesDb](maybeData)
      fullAnswers <- getSpecialTaxSitesWithApiAnswers(ctx, dbAnswers)
    } yield fullAnswers

  private def getSpecialTaxSitesWithApiAnswers(ctx: JourneyContextWithNino, dbAnswers: Option[SpecialTaxSitesDb])(implicit
                                                                                                                            hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]] = {
    val result = connector.getAnnualSummaries(ctx).map {
      case Right(annualSummaries) => dbAnswers.map(_ => SpecialTaxSitesAnswers(annualSummaries))
      case Left(_)                => None
    }
    EitherT.liftF(result)
  }
}
