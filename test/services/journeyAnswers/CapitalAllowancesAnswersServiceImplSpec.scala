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

import cats.implicits.catsSyntaxEitherId
import gens.CapitalAllowancesAnswersGen._
import gens.{bigDecimalGen, genOne}
import models.common.JourneyName.CapitalAllowancesTailoring
import models.common.{JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.frontend.capitalAllowances.CapitalAllowances.{ZeroEmissionCar, ZeroEmissionGoodsVehicle}
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.electricVehicleChargePoints.ElectricVehicleChargePointsAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.{ZeroEmissionCarsAnswers, ZeroEmissionCarsJourneyAnswers}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import stubs.connectors.StubSelfEmploymentConnector
import stubs.connectors.StubSelfEmploymentConnector.{api1802SuccessResponse, api1803SuccessResponse}
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec._
import utils.EitherTTestOps._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CapitalAllowancesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers {
  val connector: StubSelfEmploymentConnector =
    StubSelfEmploymentConnector(
      createAmendSEAnnualSubmissionResult = Future.successful(api1802SuccessResponse.asRight),
      getAnnualSummaries = Future.successful(api1803SuccessResponse.asRight)
    )
  val service = new CapitalAllowancesAnswersServiceImpl(connector, StubJourneyAnswersRepository())

  "saveAnswers" should {
    "store data successfully" in {
      val someCapitalAllowancesAnswers = ZeroEmissionCarsJourneyAnswers(genOne(bigDecimalGen))
      val result                       = service.saveAnswers(journeyCtxWithNino, someCapitalAllowancesAnswers).value.futureValue
      result shouldBe ().asRight
    }
  }

  "persistAnswers" should {
    "persist answers successfully" in {
      val answers = genOne(capitalAllowancesTailoringAnswersGen)
      val result  = service.persistAnswers(businessId, currTaxYear, mtditid, CapitalAllowancesTailoring, answers).value.futureValue
      assert(result === ().asRight)
    }
  }

  "getCapitalAllowancesTailoring" should {
    "return empty if no answers" in {
      val result = service.getCapitalAllowancesTailoring(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val tailoringAnswers =
        CapitalAllowancesTailoringAnswers(claimCapitalAllowances = true, selectCapitalAllowances = List(ZeroEmissionCar, ZeroEmissionGoodsVehicle))
      val journeyAnswers: JourneyAnswers =
        mkJourneyAnswers(JourneyName.CapitalAllowancesTailoring, JourneyStatus.Completed, Json.toJsObject(tailoringAnswers))

      val service = new CapitalAllowancesAnswersServiceImpl(
        connector,
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result = service.getCapitalAllowancesTailoring(journeyCtxWithNino).rightValue
      assert(result === Some(tailoringAnswers))
    }
  }

  "getZeroEmissionCars" should {
    "return empty if no answers" in {
      val result = service.getZeroEmissionCars(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val dbAnswers = genOne(zeroEmissionCarsDbAnswersGen)
      val journeyAnswers: JourneyAnswers =
        mkJourneyAnswers(JourneyName.ZeroEmissionCars, JourneyStatus.Completed, Json.toJsObject(dbAnswers))
      val services = new CapitalAllowancesAnswersServiceImpl(
        connector,
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result          = services.getZeroEmissionCars(journeyCtxWithNino).rightValue
      val expectedAnswers = ZeroEmissionCarsAnswers(dbAnswers, api1803SuccessResponse)

      result shouldBe Some(expectedAnswers)
    }
  }

  "getElectricVehicleChargePoints" should {
    "return empty if no answers" in {
      val result = service.getElectricVehicleChargePoints(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val dbAnswers = genOne(electricVehicleChargePointsDbAnswersGen)
      val journeyAnswers: JourneyAnswers =
        mkJourneyAnswers(JourneyName.ElectricVehicleChargePoints, JourneyStatus.Completed, Json.toJsObject(dbAnswers))
      val services = new CapitalAllowancesAnswersServiceImpl(
        connector,
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result          = services.getElectricVehicleChargePoints(journeyCtxWithNino).rightValue
      val expectedAnswers = ElectricVehicleChargePointsAnswers(dbAnswers, api1803SuccessResponse)

      result shouldBe Some(expectedAnswers)
    }
  }

  "getAnnualInvestmentAllowance" should {
    "return empty if no answers" in {
      val result = service.getAnnualInvestmentAllowance(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val dbAnswers = genOne(annualInvestmentAllowanceDbAnswersGen)
      val journeyAnswers: JourneyAnswers =
        mkJourneyAnswers(JourneyName.AnnualInvestmentAllowance, JourneyStatus.Completed, Json.toJsObject(dbAnswers))
      val services = new CapitalAllowancesAnswersServiceImpl(
        connector,
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result = services.getAnnualInvestmentAllowance(journeyCtxWithNino).rightValue
      val expectedAnswers = AnnualInvestmentAllowanceAnswers(
        dbAnswers.annualInvestmentAllowance,
        api1803SuccessResponse.annualAllowances.flatMap(_.annualInvestmentAllowance))

      result shouldBe Some(expectedAnswers)
    }
  }
}
