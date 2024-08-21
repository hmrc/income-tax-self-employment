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

import bulders.NICsAnswersBuilder.{class4NoAnswer, class4SingleBusinessAnswers}
import cats.implicits.catsSyntaxEitherId
import models.connector.api_1638.{RequestSchemaAPI1638, RequestSchemaAPI1638Class2Nics, RequestSchemaAPI1638TaxAvoidanceInner}
import models.connector.api_1639.{SuccessResponseAPI1639, SuccessResponseAPI1639Class2Nics, SuccessResponseAPI1639TaxAvoidanceInner}
import models.connector.api_1802.request.{AnnualNonFinancials, CreateAmendSEAnnualSubmissionRequestBody, CreateAmendSEAnnualSubmissionRequestData}
import models.connector.api_1803
import models.connector.api_1803.AnnualNonFinancialsType
import models.database.nics.NICsStorageAnswers
import models.frontend.nics.{NICsAnswers, NICsClass2Answers}
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import stubs.connectors.StubIFSConnector.api1171SingleBusinessResponse
import stubs.connectors.{StubIFSBusinessDetailsConnector, StubIFSConnector}
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec.{businessId, currTaxYearEnd, hc, journeyCtxWithNino, nino, taxYear}
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global

class NICsAnswersServiceImplSpec extends AnyWordSpecLike {

  "saveClass2Answers" should {
    "create a new answers model if nothing already exist" in new StubbedService {
      val answers = NICsClass2Answers(true)

      val result = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === Some(RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    val disclosuresWithOtherFields =
      SuccessResponseAPI1639(Some(List(SuccessResponseAPI1639TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase))), None)

    "setting class2 does not override other fields and true is not stored in DB" in new StubbedService {
      override val connector = StubIFSConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers            = NICsClass2Answers(true)

      val result = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(
        connector.upsertDisclosuresSubmissionData === Some(
          RequestSchemaAPI1638(
            Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
            Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    "settings class2 to None when class2 is false and there are other fields in the object" in new StubbedService {
      override val connector = StubIFSConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers            = NICsClass2Answers(false)

      val result = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

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
      override val connector = StubIFSConnector(getDisclosuresSubmissionResult =
        Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
      val answers = NICsClass2Answers(false)

      val result = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === None)
      assert(repository.lastUpsertedAnswer === Some(Json.toJson(answers)))
    }

  }

  def buildCreateAmendSEAnnualSubmissionRequestData(annualNonFinancials: Option[AnnualNonFinancials]): CreateAmendSEAnnualSubmissionRequestData =
    CreateAmendSEAnnualSubmissionRequestData(taxYear, nino, businessId, CreateAmendSEAnnualSubmissionRequestBody(None, None, annualNonFinancials))

  "saveClass4SingleBusiness" should {
    "create a new answers model if nothing already exist" in new StubbedService {
      val answers            = class4NoAnswer
      override val connector = StubIFSConnector(getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None)))

      val result = service.saveClass4SingleBusiness(journeyCtxWithNino, answers).value.futureValue
      val expectedApiData = AnnualNonFinancials(
        businessDetailsChangedRecently = Some(true),
        Some(false),
        None
      ) // TODO SASS-8728 businessDetailsChangedRecently should be None

      assert(result.isRight)
      assert(connector.upsertAnnualSummariesSubmissionData === Some(buildCreateAmendSEAnnualSubmissionRequestData(Some(expectedApiData))))
    }

    "override existing details" in new StubbedService {
      val answers = class4SingleBusinessAnswers
      override val connector = StubIFSConnector(getAnnualSummariesResult =
        Right(api_1803.SuccessResponseSchema(None, None, Some(AnnualNonFinancialsType(None, Some(false), None)))))

      val result = service.saveClass4SingleBusiness(journeyCtxWithNino, answers).value.futureValue
      val expectedApiData = AnnualNonFinancials(
        businessDetailsChangedRecently = Some(true),
        Some(true),
        Some("002")
      ) // TODO SASS-8728 businessDetailsChangedRecently should be None

      assert(result.isRight)
      assert(connector.upsertAnnualSummariesSubmissionData === Some(buildCreateAmendSEAnnualSubmissionRequestData(Some(expectedApiData))))
    }
  }

  "get answers" should {
    "return None if there are no answers" in new StubbedService {
      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value
      assert(result === None)
    }

    "return API version even if database exist" in new StubbedService {
      override val connector = StubIFSConnector(getDisclosuresSubmissionResult =
        Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
      override val repository = StubJourneyAnswersRepository(
        getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false)))))
      )

      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value

      assert(result === Some(NICsAnswers(Some(NICsClass2Answers(true)), None)))
    }

    "return database version if no API data exist" in new StubbedService {
      override val repository = StubJourneyAnswersRepository(
        getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false)))))
      )

      val result = service.getAnswers(journeyCtxWithNino).value.futureValue.value

      assert(result === Some(NICsAnswers(Some(NICsClass2Answers(false)), None)))
    }
  }

  trait StubbedService {
    val connector         = StubIFSConnector()
    val businessConnector = StubIFSBusinessDetailsConnector(getBusinessesResult = api1171SingleBusinessResponse(businessId).asRight)
    val repository        = StubJourneyAnswersRepository()

    def service = new NICsAnswersServiceImpl(connector, businessConnector, repository)
  }
}
