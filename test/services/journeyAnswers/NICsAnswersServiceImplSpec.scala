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

package services.journeyAnswers

import builders.NICsAnswersBuilder.{class4DiverAndTrusteeMultipleBusinessesAnswers, class4SingleBusinessAnswers}
import cats.implicits.catsSyntaxEitherId
import config.AppConfig
import models.common.BusinessId
import models.connector.api_1638._
import models.connector.api_1639._
import models.connector.api_1802.request._
import models.connector.api_1803
import models.connector.api_1803.AnnualNonFinancialsType.Class4NicsExemptionReason
import models.connector.api_1803.{AnnualNonFinancialsType, SuccessResponseSchema}
import models.database.nics.NICsStorageAnswers
import models.error.ServiceError
import models.frontend.nics.ExemptionReason.{DiverDivingInstructor, TrusteeExecutorAdmin}
import models.frontend.nics.NICsClass4Answers.Class4ExemptionAnswers
import models.frontend.nics.{ExemptionReason, NICsAnswers, NICsClass2Answers, NICsClass4Answers}
import org.scalatest.EitherValues._
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor3}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import stubs.connectors.StubIFSConnector.{api1171MultipleBusinessResponse, api1171SingleBusinessResponse}
import stubs.connectors.{StubBusinessDetailsConnector, StubIFSBusinessDetailsConnector, StubIFSConnector}
import stubs.repositories.StubJourneyAnswersRepository
import stubs.services.StubBusinessService
import uk.gov.hmrc.http.HttpClient
import utils.BaseSpec.{businessId, currTaxYear, currTaxYearEnd, hc, journeyCtxWithNino, nino}
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global

class NICsAnswersServiceImplSpec extends TableDrivenPropertyChecks with AnyWordSpecLike {

  "saveClass2Answers" should {
    val disclosuresWithOtherFields =
      SuccessResponseAPI1639(Some(List(SuccessResponseAPI1639TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase))), None)

    "create a new answers model if nothing already exist" in new StubbedService {
      val answers: NICsClass2Answers         = NICsClass2Answers(true)
      val result: Either[ServiceError, Unit] = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === Some(RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    "setting class2 does not override other fields and true is not stored in DB" in new StubbedService {
      override val connector         = new StubIFSConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers: NICsClass2Answers = NICsClass2Answers(true)

      val result: Either[ServiceError, Unit] = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(
        connector.upsertDisclosuresSubmissionData === Some(
          RequestSchemaAPI1638(
            Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
            Some(RequestSchemaAPI1638Class2Nics(Some(true))))))
      assert(repository.lastUpsertedAnswer === Some(JsObject.empty))
    }

    "settings class2 to None when class2 is false and there are other fields in the object" in new StubbedService {
      override val connector: StubIFSConnector = new StubIFSConnector(getDisclosuresSubmissionResult = Some(disclosuresWithOtherFields).asRight)
      val answers: NICsClass2Answers           = NICsClass2Answers(false)

      val result: Either[ServiceError, Unit] = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(
        connector.upsertDisclosuresSubmissionData === Some(
          RequestSchemaAPI1638(
            Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
            None
          )
        )
      )
      assert(repository.lastUpsertedAnswer === Some(Json.toJson(NICsStorageAnswers(Some(false), None))))
    }

    "call DELETE if setting to false and the object is empty" in new StubbedService {
      override val connector = new StubIFSConnector(getDisclosuresSubmissionResult =
        Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
      val answers: NICsClass2Answers = NICsClass2Answers(false)

      val result: Either[ServiceError, Unit] = service.saveClass2Answers(journeyCtxWithNino, answers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertDisclosuresSubmissionData === None)
      assert(repository.lastUpsertedAnswer === Some(Json.toJson(answers)))
    }
  }

  val saveClass4DataCases: TableFor3[String, Class4ExemptionAnswers, AnnualNonFinancials] = Table(
    ("testDescription", "answer", "expectedApiData"),
    (
      "save 'No Class 4 exemption'",
      Class4ExemptionAnswers(businessId, class4Exempt = false, None),
      AnnualNonFinancials(exemptFromPayingClass4Nics = false, None)),
    (
      "save a Trustee related exemption",
      Class4ExemptionAnswers(businessId, class4Exempt = true, Some(TrusteeExecutorAdmin)),
      AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some("002"))),
    (
      "save a Diver related exemption",
      Class4ExemptionAnswers(businessId, class4Exempt = true, Some(DiverDivingInstructor)),
      AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some("003")))
  )

  "saveClass4BusinessData" should {
    forAll(saveClass4DataCases) { (testDescription, answer, expectedApiData) =>
      testDescription when {
        "creating a new answers model when there is no existing Class 4 data" in new StubbedService {
          override val connector: StubIFSConnector =
            StubIFSConnector(getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None)))

          val expectedResult: Option[CreateAmendSEAnnualSubmissionRequestData] = buildExpectedRequestResult(expectedApiData)

          val result: Either[ServiceError, Unit] = service.saveClass4BusinessData(journeyCtxWithNino, answer).value.futureValue

          assert(result.isRight)
          assert(connector.upsertAnnualSummariesSubmissionData === expectedResult)
        }

        "overriding existing details" in new StubbedService {
          override val connector: StubIFSConnector = StubIFSConnector(getAnnualSummariesResult = Right(
            api_1803.SuccessResponseSchema(
              None,
              None,
              Some(AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None)))))

          val expectedResult: Option[CreateAmendSEAnnualSubmissionRequestData] = buildExpectedRequestResult(expectedApiData)

          val result: Either[ServiceError, Unit] = service.saveClass4BusinessData(journeyCtxWithNino, answer).value.futureValue

          assert(result.isRight)
          assert(connector.upsertAnnualSummariesSubmissionData === expectedResult)
        }
      }
    }
  }

  "saveClass4SingleBusiness" should {
    "save Class 4 journey answers when user has a single business" in new StubbedService {
      override val ifsBusinessConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(getBusinessesResult = api1171MultipleBusinessResponse(List(businessId)).asRight)
      override val connector: StubIFSConnector = StubIFSConnector(getAnnualSummariesResult = Right(api_1803.SuccessResponseSchema(None, None, None)))

      val expectedApiData: AnnualNonFinancials = AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(TrusteeExecutorAdmin.exemptionCode))
      val expectedResult: Option[CreateAmendSEAnnualSubmissionRequestData] = buildExpectedRequestResult(expectedApiData)

      val result: Either[ServiceError, Unit] = service.saveClass4SingleBusiness(journeyCtxWithNino, class4SingleBusinessAnswers).value.futureValue

      assert(result.isRight)
      assert(connector.upsertAnnualSummariesSubmissionData === expectedResult)
    }
  }

  "saveClass4MultipleBusinesses" should {
    def buildDataResponse(annualNonFinancialsData: AnnualNonFinancialsType): Right[Nothing, SuccessResponseSchema] =
      Right(api_1803.SuccessResponseSchema(None, None, Some(annualNonFinancialsData)))

    "save journey answers - creating, replacing or clearing Class 4 exemptions data of any user business IDs" in new StubbedService {
      val existingDataId1: AnnualNonFinancialsType = AnnualNonFinancialsType(None, None, None)
      val existingDataId2: AnnualNonFinancialsType =
        AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._003), None)
      val existingDataId3: AnnualNonFinancialsType =
        AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None)
      val existingDataId4: AnnualNonFinancialsType =
        AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None)

      override val ifsBusinessConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(getBusinessesResult = api1171MultipleBusinessResponse(
          List(BusinessId("BusinessId1"), BusinessId("BusinessId2"), BusinessId("BusinessId3"), BusinessId("BusinessId4"))).asRight)
      override val connector: StubIFSConnector = StubIFSConnector(
        getAnnualSummariesResultTest1 = this.buildDataResponse(existingDataId1),
        getAnnualSummariesResultTest2 = this.buildDataResponse(existingDataId2),
        getAnnualSummariesResultTest3 = this.buildDataResponse(existingDataId3),
        getAnnualSummariesResultTest4 = this.buildDataResponse(existingDataId4)
      )

      val expectedResultId1: Option[CreateAmendSEAnnualSubmissionRequestData] = // Replace empty answers
        buildExpectedRequestResult(
          AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(DiverDivingInstructor.exemptionCode)),
          BusinessId("BusinessId1"))
      val expectedResultId2: Option[CreateAmendSEAnnualSubmissionRequestData] = // Persist original answers
        buildExpectedRequestResult(
          AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(DiverDivingInstructor.exemptionCode)),
          BusinessId("BusinessId2"))
      val expectedResultId3: Option[CreateAmendSEAnnualSubmissionRequestData] = // Replace existing answers
        buildExpectedRequestResult(
          AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(TrusteeExecutorAdmin.exemptionCode)),
          BusinessId("BusinessId3"))
      val expectedResultId4: Option[CreateAmendSEAnnualSubmissionRequestData] = // Clear existing answers
        buildExpectedRequestResult(AnnualNonFinancials(exemptFromPayingClass4Nics = false, None), BusinessId("BusinessId4"))

      val result: Either[ServiceError, Unit] =
        service.saveClass4MultipleBusinessOrNoExemptionJourneys(journeyCtxWithNino, class4DiverAndTrusteeMultipleBusinessesAnswers).value.futureValue
      assert(result.isRight)
      assert(connector.upsertAnnualSummariesSubmissionDataTest1 === expectedResultId1)
      assert(connector.upsertAnnualSummariesSubmissionDataTest2 === expectedResultId2)
      assert(connector.upsertAnnualSummariesSubmissionDataTest3 === expectedResultId3)
      assert(connector.upsertAnnualSummariesSubmissionDataTest4 === expectedResultId4)
    }
  }

  "get answers" should {
    "return None if there are no answers" in new StubbedService {
      val result: Option[NICsAnswers] = service.getAnswers(journeyCtxWithNino).value.futureValue.value
      assert(result === None)
    }

    "return Class 2 Answers" when {

      "API answers are returned even if database exist" in new StubbedService {
        override val connector: StubIFSConnector = StubIFSConnector(getDisclosuresSubmissionResult =
          Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))).asRight)
        override val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository(
          getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false), None))))
        )

        val result: Option[NICsAnswers] = service.getAnswers(journeyCtxWithNino).value.futureValue.value

        assert(result === Some(NICsAnswers(Some(NICsClass2Answers(true)), None)))
      }

      "valid database answer exist and there is no API data" in new StubbedService {
        override val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository(
          getAnswers = Right(Some(Json.toJson(NICsStorageAnswers(Some(false), None))))
        )

        val result: Option[NICsAnswers] = service.getAnswers(journeyCtxWithNino).value.futureValue.value

        assert(result === Some(NICsAnswers(Some(NICsClass2Answers(false)), None)))
      }
    }

    "return Class 4 Answers from API data" in new StubbedService {

      val annualNonFinancialsData: SuccessResponseSchema = SuccessResponseSchema(
        None,
        None,
        Some(AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._002), None))
      )
      override val connector: StubIFSConnector = StubIFSConnector(getAnnualSummariesResult = annualNonFinancialsData.asRight)

      override val businessService: StubBusinessService = StubBusinessService(getUserBusinessIdsResult = Right(List(journeyCtxWithNino.businessId)))

      val result: Option[NICsAnswers] = service.getAnswers(journeyCtxWithNino).value.futureValue.value

      assert(result === Some(NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None)))))
    }

  }

  trait StubbedService {
    val connector                  = new StubIFSConnector()
    val mockAppConfig: AppConfig   = mock[AppConfig]
    val mockhttpClient: HttpClient = mock[HttpClient]
    val ifsBusinessConnector: StubIFSBusinessDetailsConnector = StubIFSBusinessDetailsConnector(
      getBusinessesResult = api1171SingleBusinessResponse(businessId).asRight)
    val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository()
    val businessService: StubBusinessService     = StubBusinessService()

    val hipBusinessConnector: StubBusinessDetailsConnector = StubBusinessDetailsConnector(
      httpClient = mockhttpClient,
      appConfig = mockAppConfig,
      getBusinessDetailsRes = api1171SingleBusinessResponse(businessId).asRight
    )

    def service: NICsAnswersServiceImpl =
      new NICsAnswersServiceImpl(connector, ifsBusinessConnector, hipBusinessConnector, repository, businessService, mockAppConfig)

    def buildDataResponse(annualNonFinancialsData: AnnualNonFinancialsType): Right[Nothing, SuccessResponseSchema] =
      Right(api_1803.SuccessResponseSchema(None, None, Some(annualNonFinancialsData)))

    def buildExpectedRequestResult(annualNonFinancials: AnnualNonFinancials,
                                   id: BusinessId = businessId): Option[CreateAmendSEAnnualSubmissionRequestData] =
      Some(
        CreateAmendSEAnnualSubmissionRequestData(
          currTaxYear,
          nino,
          id,
          CreateAmendSEAnnualSubmissionRequestBody(None, None, Some(annualNonFinancials))))

  }
}
