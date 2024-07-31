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
import models.connector.api_1638.{RequestSchemaAPI1638, RequestSchemaAPI1638Class2Nics, RequestSchemaAPI1638TaxAvoidanceInner}
import models.connector.api_1639.{SuccessResponseAPI1639, SuccessResponseAPI1639Class2Nics, SuccessResponseAPI1639TaxAvoidanceInner}
import models.database.nics.NICsStorageAnswers
import models.frontend.nics.NICsAnswers
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import stubs.connectors.StubSelfEmploymentConnector
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec.{currTaxYearEnd, hc, journeyCtxWithNino}
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global

class NICsAnswersServiceImplSpec extends AnyWordSpecLike {

  "save answers" should {
    "create a new answers if nothing already exist" in new StubbedService {
      val answers = NICsAnswers(true)

      val result = service.saveAnswers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === Some(RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    val disclosuresWithOtherFields =
      SuccessResponseAPI1639(Some(List(SuccessResponseAPI1639TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase))), None)

    "setting class2 does not override other fields and true is not stored in DB" in new StubbedService {
      override val connector = StubSelfEmploymentConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers            = NICsAnswers(true)

      val result = service.saveAnswers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(
        connector.upsertDisclosuresSubmissionData === Some(
          RequestSchemaAPI1638(
            Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
            Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    "settings class2 to None when class2 is false and there are other fields in the object" in new StubbedService {
      override val connector = StubSelfEmploymentConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers            = NICsAnswers(false)

      val result = service.saveAnswers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(
        connector.upsertDisclosuresSubmissionData === Some(
          RequestSchemaAPI1638(
            Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
            None
          )
        )
      )
      assert(repository.lastUpsertedAnswer === Some(Json.toJson(NICsStorageAnswers(Some(false)))))
    }

    "call DELETE if setting to false and the object is empty" in new StubbedService {
      override val connector = StubSelfEmploymentConnector(getDisclosuresSubmissionResult =
        Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
      val answers = NICsAnswers(false)

      val result = service.saveAnswers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === None)
      assert(repository.lastUpsertedAnswer === Some(Json.toJson(answers)))
    }

  }

  "get answers" should {
    "return None if there are no answers" in new StubbedService {
      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value
      assert(result === None)
    }

    "return API version even if database exist" in new StubbedService {
      override val connector = StubSelfEmploymentConnector(getDisclosuresSubmissionResult =
        Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
      override val repository = StubJourneyAnswersRepository(
        getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false)))))
      )

      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value

      assert(result === Some(NICsAnswers(true)))
    }

    "return database version if no API data exist" in new StubbedService {
      override val repository = StubJourneyAnswersRepository(
        getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false)))))
      )

      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value

      assert(result === Some(NICsAnswers(false)))
    }
  }

  trait StubbedService {
    val connector  = StubSelfEmploymentConnector()
    val repository = StubJourneyAnswersRepository()

    def service = new NICsAnswersServiceImpl(connector, repository)
  }
}
