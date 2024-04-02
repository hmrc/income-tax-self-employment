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

package stubs.services

import cats.data.EitherT
import models.common._
import models.connector.Api1802AnnualAllowancesBuilder
import models.connector.api_1802.request.AnnualAllowances
import models.connector.api_1803.SuccessResponseSchema
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.balancingAllowance.BalancingAllowanceAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.ElectricVehicleChargePointsAnswers
import models.frontend.capitalAllowances.specialTaxSites.SpecialTaxSitesAnswers
import models.frontend.capitalAllowances.structuresBuildings.StructuresBuildingsAnswers
import models.frontend.capitalAllowances.writingDownAllowance.WritingDownAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehicleAnswers
import play.api.libs.json.Writes
import services.journeyAnswers.CapitalAllowancesAnswersService
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubCapitalAllowancesAnswersAnswersService(
    saveAnswers: ApiResultT[Unit] = serviceUnitT,
    saveAnnualAllowances: ApiResultT[Unit] = serviceUnitT,
    getAnnualSummaries: Either[ServiceError, Option[SuccessResponseSchema]] = Right(None),
    persistCapitalAllowancesTailoring: ApiResultT[Unit] = serviceUnitT,
    getCapitalAllowancesTailoring: Either[ServiceError, Option[CapitalAllowancesTailoringAnswers]] = Right(None),
    getZeroEmissionCars: Either[ServiceError, Option[ZeroEmissionCarsAnswers]] = Right(None),
    getZeroEmissionGoodsVehicleCars: Either[ServiceError, Option[ZeroEmissionGoodsVehicleAnswers]] = Right(None),
    getElectricVehicleChargePoints: Either[ServiceError, Option[ElectricVehicleChargePointsAnswers]] = Right(None),
    getBalancingAllowance: Either[ServiceError, Option[BalancingAllowanceAnswers]] = Right(None),
    getAnnualInvestmentAllowance: Either[ServiceError, Option[AnnualInvestmentAllowanceAnswers]] = Right(None),
    getWritingDownAllowance: Either[ServiceError, Option[WritingDownAllowanceAnswers]] = Right(None),
    getSpecialTaxSites: Either[ServiceError, Option[SpecialTaxSitesAnswers]] = Right(None),
    getStructuresBuildings: Either[ServiceError, Option[StructuresBuildingsAnswers]] = Right(None)
) extends CapitalAllowancesAnswersService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def saveAnswers[A: Api1802AnnualAllowancesBuilder: Writes](ctx: JourneyContextWithNino, answers: A)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    persistCapitalAllowancesTailoring

  def persistAnswers[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
      writes: Writes[A]): ApiResultT[Unit] = persistCapitalAllowancesTailoring

  def getCapitalAllowancesTailoring(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[CapitalAllowancesTailoringAnswers]] =
    EitherT.fromEither[Future](getCapitalAllowancesTailoring)

  def getZeroEmissionCars(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionCarsAnswers]] =
    EitherT.fromEither[Future](getZeroEmissionCars)

  def getElectricVehicleChargePoints(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[ElectricVehicleChargePointsAnswers]] =
    EitherT.fromEither[Future](getElectricVehicleChargePoints)

  def getBalancingAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[BalancingAllowanceAnswers]] =
    EitherT.fromEither[Future](getBalancingAllowance)

  def getZeroEmissionGoodsVehicle(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ZeroEmissionGoodsVehicleAnswers]] =
    EitherT.fromEither[Future](getZeroEmissionGoodsVehicleCars)

  def getAnnualInvestmentAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualInvestmentAllowanceAnswers]] =
    EitherT.fromEither[Future](getAnnualInvestmentAllowance)

  def saveAnnualAllowances(ctx: JourneyContextWithNino, updatedAnnualAllowances: AnnualAllowances)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    saveAnnualAllowances

  def getAnnualSummaries(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SuccessResponseSchema]] =
    EitherT.fromEither[Future](getAnnualSummaries)

  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]] =
    EitherT.fromEither[Future](getWritingDownAllowance)

  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]] =
    EitherT.fromEither[Future](getSpecialTaxSites)

  def getStructuresBuildings(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[StructuresBuildingsAnswers]] =
    EitherT.fromEither[Future](getStructuresBuildings)

}
