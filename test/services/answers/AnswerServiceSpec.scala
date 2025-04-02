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

package services.answers

import data.CommonTestData
import mocks.repositories.MockJourneyAnswersRepository
import models.common.JourneyName.{TravelExpenses, VehicleDetails}
import models.common.{JourneyContext, JourneyContextWithNino}
import models.database.JourneyAnswers
import models.database.expenses.travel.{CarOrGoodsVehicle, FlatRate, OwnVehicles, TravelExpensesDb, VehicleDetailsDb}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AnswerServiceSpec extends AnyWordSpec with Matchers with CommonTestData with DefaultAwaitTimeout {

  val service                                   = new AnswerService(MockJourneyAnswersRepository.mockInstance)
  val testContext: JourneyContextWithNino       = JourneyContextWithNino(testTaxYear, testBusinessId, testMtdId, testNino)
  val testTravelExpensesContext: JourneyContext = testContext.toJourneyContext(TravelExpenses)
  val testVehicleDetailsContext: JourneyContext = testContext.toJourneyContext(VehicleDetails)

  val testTravelExpenses: TravelExpensesDb = TravelExpensesDb(
    expensesToClaim = Some(Seq(OwnVehicles)),
    allowablePublicTransportExpenses = Some(BigDecimal("100.00")),
    disallowablePublicTransportExpenses = Some(BigDecimal("50.00"))
  )

  val testVehicleDetails: VehicleDetailsDb = VehicleDetailsDb(
    description = Some("test"),
    vehicleType = Some(CarOrGoodsVehicle),
    usedSimplifiedExpenses = Some(true),
    calculateFlatRate = Some(true),
    workMileage = Some(100000),
    expenseMethod = Some(FlatRate),
    costsOutsideFlatRate = Some(BigDecimal("100.00"))
  )

  val testTravelExpensesJson: JsValue = Json.toJson(testTravelExpenses)

  val testJourneyAnswers: JourneyAnswers = testJourneyAnswers(TravelExpenses, testTravelExpensesJson)

  val testVehicleDetailsAnswers: JourneyAnswers = testJourneyAnswers(VehicleDetails, Json.toJson(CollectionSection(Seq(testVehicleDetails))))

  "getJourneyAnswers" should {
    "return None if the journey answers don't exist" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testTravelExpensesContext)(Future.successful(None))

      val result = await(service.getJourneyAnswers[TravelExpensesDb](testContext, TravelExpenses))

      result mustBe None
    }

    "return data in the correct format where journey answers exist" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testTravelExpensesContext)(Future.successful(Some(testJourneyAnswers)))

      val result = await(service.getJourneyAnswers[TravelExpensesDb](testContext, TravelExpenses))

      result mustBe Some(testTravelExpenses)
    }
  }

  "upsertJourneyAnswers" should {
    "create a record if one doesn't exist and return the data" in {
      MockJourneyAnswersRepository.upsertJourneyAnswers(testTravelExpensesContext, testTravelExpensesJson)(
        Future.successful(Some(testTravelExpensesJson)))

      val result = await(service.upsertJourneyAnswers(testContext, TravelExpenses, testTravelExpenses))

      result mustBe Some(testTravelExpenses)
    }

    "update an existing record and return the data" in {
      val update     = testTravelExpenses.copy(allowablePublicTransportExpenses = None)
      val updateJson = Json.toJson(update)
      MockJourneyAnswersRepository.upsertJourneyAnswers(testTravelExpensesContext, updateJson)(Future.successful(Some(updateJson)))

      val result = await(service.upsertJourneyAnswers(testContext, TravelExpenses, update))

      result mustBe Some(update)
    }
  }

  "deleteJourneyAnswers" should {
    "return true if the journey answers are deleted" in {
      MockJourneyAnswersRepository.deleteJourneyAnswers(testTravelExpensesContext)(wasDeleted = true)

      val result = await(service.deleteJourneyAnswers(testContext, TravelExpenses))

      result mustBe true
    }

    "return false if the journey answers are not deleted" in {
      MockJourneyAnswersRepository.deleteJourneyAnswers(testTravelExpensesContext)(wasDeleted = false)

      val result = await(service.deleteJourneyAnswers(testContext, TravelExpenses))

      result mustBe false
    }
  }

  "getCollectionAnswer" should {
    "return None if the journey answers don't exist" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(Future.successful(None))

      val result = await(service.getCollectionAnswer[VehicleDetailsDb](testContext, VehicleDetails, index = 1))

      result mustBe None
    }

    "return data in the correct format where journey answers exist" in {
      val answers = testJourneyAnswers.copy(data = Json.toJson(CollectionSection(Seq(testVehicleDetails))).as[JsObject])
      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(Future.successful(Some(answers)))

      val result = await(service.getCollectionAnswer[VehicleDetailsDb](testContext, VehicleDetails, index = 1))

      result mustBe Some(testVehicleDetails)
    }
  }

  "upsertCollectionAnswer" should {
    "create a new index" in {
      val answers = CollectionSection(Seq(testVehicleDetails))
      val json    = Json.toJson(answers)

      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(Future.successful(None))
      MockJourneyAnswersRepository.upsertJourneyAnswers(testVehicleDetailsContext, json)(Future.successful(Some(json)))

      val result = await(service.upsertCollectionAnswer(testContext, VehicleDetails, testVehicleDetails, index = 1))

      result mustBe Some(testVehicleDetails)
    }

    "update an existing index" in {
      val update           = testVehicleDetails.copy(description = Some("updated"))
      val updatedIndex     = CollectionSection(Seq(update))
      val updatedIndexJson = Json.toJson(updatedIndex)

      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(Future.successful(Some(testVehicleDetailsAnswers)))
      MockJourneyAnswersRepository.upsertJourneyAnswers(testVehicleDetailsContext, updatedIndexJson)(Future.successful(Some(updatedIndexJson)))

      val result = await(service.upsertCollectionAnswer(testContext, VehicleDetails, update, index = 1))

      result mustBe Some(update)
    }
  }

  "deleteCollectionAnswer" should {
    "delete the whole section if the last index was removed" in {
      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(Future.successful(Some(testVehicleDetailsAnswers)))
      MockJourneyAnswersRepository.deleteJourneyAnswers(testVehicleDetailsContext)(wasDeleted = true)

      val result = await(service.deleteCollectionAnswer(testContext, VehicleDetails, index = 1))

      result mustBe None
    }

    "delete the correct index and return JSON with that index removed" in {
      val originalAnswers = CollectionSection(
        Seq(
          testVehicleDetails.copy(description = Some("1")),
          testVehicleDetails.copy(description = Some("2")),
          testVehicleDetails.copy(description = Some("3"))
        ))

      val updatedAnswersJson = Json.toJson(
        CollectionSection(
          Seq(
            testVehicleDetails.copy(description = Some("1")),
            testVehicleDetails.copy(description = Some("3"))
          )))

      MockJourneyAnswersRepository.getJourneyAnswers(testVehicleDetailsContext)(
        response = Future.successful(Some(testVehicleDetailsAnswers.copy(data = Json.toJson(originalAnswers).as[JsObject])))
      )
      MockJourneyAnswersRepository.upsertJourneyAnswers(testVehicleDetailsContext, updatedAnswersJson)(
        response = Future.successful(Some(updatedAnswersJson))
      )

      val result = await(service.deleteCollectionAnswer(testContext, VehicleDetails, index = 2))

      result mustBe Some(updatedAnswersJson)
    }
  }

}
