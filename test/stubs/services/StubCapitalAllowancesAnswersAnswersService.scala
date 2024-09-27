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
import controllers.actions.AuthorisedAction
import models.common._
import models.connector.api_1802.request.AnnualAllowances
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
import play.api.libs.json.{Reads, Writes}
import play.api.mvc.Results.NoContent
import play.api.mvc.{AnyContent, Result}
import services.journeyAnswers.CapitalAllowancesAnswersService
import stubs.serviceUnitT
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

case class StubCapitalAllowancesAnswersAnswersService(
    saveAnswers: Future[Result] = Future.successful(NoContent),
    saveAnnualAllowances: ApiResultT[Unit] = serviceUnitT,
    persistCapitalAllowancesTailoring: ApiResultT[Unit] = serviceUnitT,
    getCapitalAllowancesTailoring: Either[ServiceError, Option[CapitalAllowancesTailoringAnswers]] = Right(None),
    getZeroEmissionCars: Either[ServiceError, Option[ZeroEmissionCarsAnswers]] = Right(None),
    getZeroEmissionGoodsVehicleCars: Either[ServiceError, Option[ZeroEmissionGoodsVehicleAnswers]] = Right(None),
    getElectricVehicleChargePoints: Either[ServiceError, Option[ElectricVehicleChargePointsAnswers]] = Right(None),
    getBalancingAllowance: Either[ServiceError, Option[BalancingAllowanceAnswers]] = Right(None),
    getAnnualInvestmentAllowance: Either[ServiceError, Option[AnnualInvestmentAllowanceAnswers]] = Right(None),
    getWritingDownAllowance: Either[ServiceError, Option[WritingDownAllowanceAnswers]] = Right(None),
    getSpecialTaxSites: Either[ServiceError, Option[SpecialTaxSitesAnswers]] = Right(None),
    getStructuresBuildings: Either[ServiceError, Option[NewStructuresBuildingsAnswers]] = Right(None)
) extends CapitalAllowancesAnswersService {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def saveAnswers[DbAnswers: Writes, A <: FrontendAnswers[DbAnswers]: Reads](
      journeyName: JourneyName,
      taxYear: TaxYear,
      businessId: BusinessId,
      nino: Nino)(implicit hc: HeaderCarrier, user: AuthorisedAction.User[AnyContent], logger: Logger): Future[Result] =
    saveAnswers

  def persistAnswersInDatabase[A](businessId: BusinessId, taxYear: TaxYear, mtditid: Mtditid, journey: JourneyName, answers: A)(implicit
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

  def submitAnnualAllowancesToApi(ctx: JourneyContextWithNino, updatedAnnualAllowances: AnnualAllowances)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    saveAnnualAllowances

  def getWritingDownAllowance(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[WritingDownAllowanceAnswers]] =
    EitherT.fromEither[Future](getWritingDownAllowance)

  def getSpecialTaxSites(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[SpecialTaxSitesAnswers]] =
    EitherT.fromEither[Future](getSpecialTaxSites)

  def getStructuresBuildings(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[NewStructuresBuildingsAnswers]] =
    EitherT.fromEither[Future](getStructuresBuildings)

}
