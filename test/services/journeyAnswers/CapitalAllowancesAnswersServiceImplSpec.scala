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
import controllers.ControllerBehaviours.buildRequest
import controllers.actions.AuthorisedAction
import controllers.actions.AuthorisedAction.User
import gens.CapitalAllowancesAnswersGen._
import gens.genOne
import models.common.JourneyName.ZeroEmissionCars
import models.common.{JourneyName, JourneyStatus}
import models.database.JourneyAnswers
import models.database.capitalAllowances.ZeroEmissionCarsDb
import models.frontend.capitalAllowances.CapitalAllowances.{ZeroEmissionCar, ZeroEmissionGoodsVehicle}
import models.frontend.capitalAllowances.CapitalAllowancesTailoringAnswers
import models.frontend.capitalAllowances.annualInvestmentAllowance.AnnualInvestmentAllowanceAnswers
import models.frontend.capitalAllowances.zeroEmissionCars.ZeroEmissionCarsAnswers
import models.frontend.capitalAllowances.zeroEmissionGoodsVehicle.ZeroEmissionGoodsVehicleAnswers
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector.api1803SuccessResponse
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec._
import utils.EitherTTestOps._
import utils.Logging

import scala.concurrent.ExecutionContext.Implicits.global

class CapitalAllowancesAnswersServiceImplSpec extends AnyWordSpecLike with Matchers with Logging {
  val connector: StubIFSConnector =
    StubIFSConnector(
      createAmendSEAnnualSubmissionResult = ().asRight,
      getAnnualSummariesResult = api1803SuccessResponse.asRight
    )
  val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository()
  val service                                  = new CapitalAllowancesAnswersServiceImpl(connector, repository)

  "saveAnswers" should {
    "store data successfully" in {
      val answers                                          = genOne(zeroEmissionCarsAnswersGen)
      val request                                          = buildRequest[ZeroEmissionCarsAnswers](answers)
      implicit val user: AuthorisedAction.User[AnyContent] = User(mtditid.value, None)(request)

      service.saveAnswers[ZeroEmissionCarsDb, ZeroEmissionCarsAnswers](ZeroEmissionCars, taxYear, businessId, nino).map { result =>
        assert(result === ().asRight)
        assert(connector.upsertAnnualSummariesSubmissionData === Some(answers))
        assert(repository.lastUpsertedAnswer === Some(Json.toJson(answers.toDbModel)))
      }
    }
  }

  "persistAnswers" should {
    "persist answers successfully" in {
      val answers = genOne(zeroEmissionCarsAnswersGen).toDbModel.get
      service.persistAnswers[ZeroEmissionCarsDb](businessId, currTaxYear, mtditid, ZeroEmissionCars, answers).value.map { result =>
        assert(result === ().asRight)
        assert(repository.lastUpsertedAnswer === Some(Json.toJson(answers)))
      }
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

  "getZeroEmissionGoodsVehicle" should {
    "return empty if no answers" in {
      val result = service.getZeroEmissionGoodsVehicle(journeyCtxWithNino).rightValue
      assert(result === None)
    }

    "return answers if they exist" in {
      val dbAnswers = genOne(zeroEmissionGoodsVehicleDbAnswersGen)
      val journeyAnswers: JourneyAnswers =
        mkJourneyAnswers(JourneyName.ZeroEmissionGoodsVehicle, JourneyStatus.Completed, Json.toJsObject(dbAnswers))
      val services = new CapitalAllowancesAnswersServiceImpl(
        connector,
        StubJourneyAnswersRepository(
          getAnswer = Some(journeyAnswers)
        ))
      val result          = services.getZeroEmissionGoodsVehicle(journeyCtxWithNino).rightValue
      val expectedAnswers = ZeroEmissionGoodsVehicleAnswers(dbAnswers, api1803SuccessResponse)

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
