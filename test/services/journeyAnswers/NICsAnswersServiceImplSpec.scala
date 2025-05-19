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

import builders.BusinessDataBuilder.aBusiness
import builders.NICsAnswersBuilder.{class4DiverAndTrusteeMultipleBusinessesAnswers, class4SingleBusinessAnswers}
import config.AppConfig
import mocks.connectors.MockIFSConnector
import mocks.repositories.MockJourneyAnswersRepository
import mocks.services.MockBusinessService
import models.common.BusinessId
import models.common.JourneyName.NationalInsuranceContributions
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
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor3}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsValue, Json}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.await
import utils.BaseSpec.{businessId, currTaxYearEnd, hc, journeyCtxWithNino, mtditid, nicsCtx, nino}

import scala.concurrent.ExecutionContext.Implicits.global

class NICsAnswersServiceImplSpec extends TableDrivenPropertyChecks with AnyWordSpecLike with Matchers with DefaultAwaitTimeout {

  val mockAppConfig: AppConfig = mock[AppConfig]

  val testService: NICsAnswersServiceImpl = new NICsAnswersServiceImpl(
    MockIFSConnector.mockInstance,
    MockJourneyAnswersRepository.mockInstance,
    MockBusinessService.mockInstance
  )

  def buildExpectedRequestResult(annualNonFinancials: AnnualNonFinancials): CreateAmendSEAnnualSubmissionRequestBody =
    CreateAmendSEAnnualSubmissionRequestBody(
      annualAdjustments = None,
      annualAllowances = None,
      annualNonFinancials = Some(annualNonFinancials)
    )

  "saveClass2Answers" should {
    val disclosuresWithOtherFields = SuccessResponseAPI1639(
      taxAvoidance = Some(List(SuccessResponseAPI1639TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase))),
      class2Nics = None
    )

    "create a new answers model if nothing already exist" in new StubbedService {
      val answers: NICsClass2Answers = NICsClass2Answers(true)
      val newAnswers: JsValue = Json.toJson(NICsStorageAnswers.fromJourneyAnswers(answers))

      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(None)
      MockIFSConnector.upsertDisclosuresSubmission(journeyCtxWithNino, RequestSchemaAPI1638(None, Some(RequestSchemaAPI1638Class2Nics(Some(true)))))
      MockJourneyAnswersRepository.upsertAnswers(
        ctx = journeyCtxWithNino.toJourneyContext(NationalInsuranceContributions),
        newData = newAnswers
      )

      val result: Either[ServiceError, Unit] = await(testService.saveClass2Answers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "setting class2 does not override other fields and true is not stored in DB" in new StubbedService {
      val answers: NICsClass2Answers = NICsClass2Answers(true)
      val newAnswers: JsValue = Json.toJson(NICsStorageAnswers.fromJourneyAnswers(answers))
      val newDisclosureSubmission = RequestSchemaAPI1638(
        Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
        Some(RequestSchemaAPI1638Class2Nics(Some(true))))

      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(Some(disclosuresWithOtherFields))
      MockIFSConnector.upsertDisclosuresSubmission(journeyCtxWithNino, newDisclosureSubmission)
      MockJourneyAnswersRepository.upsertAnswers(
        ctx = journeyCtxWithNino.toJourneyContext(NationalInsuranceContributions),
        newData = newAnswers
      )

      val result: Either[ServiceError, Unit] = await(testService.saveClass2Answers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "settings class2 to None when class2 is false and there are other fields in the object" in new StubbedService {
      val answers: NICsClass2Answers = NICsClass2Answers(false)
      val newAnswers: JsValue = Json.toJson(NICsStorageAnswers.fromJourneyAnswers(answers))
      val newDisclosureSubmission = RequestSchemaAPI1638(
        Some(List(RequestSchemaAPI1638TaxAvoidanceInner("srn", currTaxYearEnd.toUpperCase()))),
        None
      )

      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(Some(disclosuresWithOtherFields))
      MockIFSConnector.upsertDisclosuresSubmission(journeyCtxWithNino, newDisclosureSubmission)
      MockJourneyAnswersRepository.upsertAnswers(
        ctx = journeyCtxWithNino.toJourneyContext(NationalInsuranceContributions),
        newData = newAnswers
      )

      val result: Either[ServiceError, Unit] = await(testService.saveClass2Answers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
    }

    "call DELETE if setting to false and the object is empty" in new StubbedService {
      val answers: NICsClass2Answers = NICsClass2Answers(false)
      val newAnswers: JsValue = Json.toJson(NICsStorageAnswers.fromJourneyAnswers(answers))

      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))))
      MockIFSConnector.deleteDisclosuresSubmission(journeyCtxWithNino)
      MockJourneyAnswersRepository.upsertAnswers(
        ctx = journeyCtxWithNino.toJourneyContext(NationalInsuranceContributions),
        newData = newAnswers
      )

      val result: Either[ServiceError, Unit] = await(testService.saveClass2Answers(journeyCtxWithNino, answers).value)

      result shouldBe Right(())
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
          val upsertBody = CreateAmendSEAnnualSubmissionRequestBody.mkRequest(None, None, Some(expectedApiData))

          MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
          MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, upsertBody)

          val result: Either[ServiceError, Unit] = await(testService.saveClass4BusinessData(journeyCtxWithNino, answer).value)

          result shouldBe Right(())
        }

        "overriding existing details" in new StubbedService {
          val getAnnualSummariesResponse = api_1803.SuccessResponseSchema(
            None,
            None,
            Some(AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None))
          )
          val upsertBody = CreateAmendSEAnnualSubmissionRequestBody.mkRequest(None, None, Some(expectedApiData))

          MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(getAnnualSummariesResponse))
          MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, upsertBody)

          val result: Either[ServiceError, Unit] = await(testService.saveClass4BusinessData(journeyCtxWithNino, answer).value)

          result shouldBe Right(())
        }
      }
    }
  }

  "saveClass4SingleBusiness" should {
    "save Class 4 journey answers when user has a single business" in new StubbedService {
      val expectedApiData: AnnualNonFinancials = AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(TrusteeExecutorAdmin.exemptionCode))
      val upsertBody = CreateAmendSEAnnualSubmissionRequestBody.mkRequest(None, None, Some(expectedApiData))

      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, upsertBody)
      MockBusinessService.getBusiness(businessId, mtditid, nino)(aBusiness)

      val result: Either[ServiceError, Unit] = await(testService.saveClass4SingleBusiness(journeyCtxWithNino, class4SingleBusinessAnswers).value)

      result shouldBe Right(())
    }

    "save Class 4 journey answers when user has a single business when hipMigration1171Enabled is true" in new StubbedService {
      val expectedApiData: AnnualNonFinancials = AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(TrusteeExecutorAdmin.exemptionCode))
      val upsertBody = CreateAmendSEAnnualSubmissionRequestBody.mkRequest(None, None, Some(expectedApiData))

      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino, upsertBody)
      MockBusinessService.getBusiness(businessId, mtditid, nino)(aBusiness)

      val result: Either[ServiceError, Unit] = await(testService.saveClass4SingleBusiness(journeyCtxWithNino, class4SingleBusinessAnswers).value)

      result shouldBe Right(())
    }
  }

  "saveClass4MultipleBusinesses" should {
    "save journey answers - creating, replacing or clearing Class 4 exemptions data of any user business IDs" in new StubbedService {
      val businessId1: BusinessId = BusinessId("BusinessId1")
      val businessId2: BusinessId = BusinessId("BusinessId2")
      val businessId3: BusinessId = BusinessId("BusinessId3")
      val businessId4: BusinessId = BusinessId("BusinessId4")

      val existingDataId1: AnnualNonFinancialsType = AnnualNonFinancialsType(None, None, None)
      val existingDataId2: AnnualNonFinancialsType = AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._003), None)
      val existingDataId3: AnnualNonFinancialsType = AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None)
      val existingDataId4: AnnualNonFinancialsType = AnnualNonFinancialsType(Some(true), Some(AnnualNonFinancialsType.Class4NicsExemptionReason._006), None)

      // Should replace empty answers
      val expectedResultId1: CreateAmendSEAnnualSubmissionRequestBody =
        buildExpectedRequestResult(AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(DiverDivingInstructor.exemptionCode)))

      // Should persist original answers
      val expectedResultId2: CreateAmendSEAnnualSubmissionRequestBody =
        buildExpectedRequestResult(AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(DiverDivingInstructor.exemptionCode)))

      // Should replace existing answers
      val expectedResultId3: CreateAmendSEAnnualSubmissionRequestBody =
        buildExpectedRequestResult(AnnualNonFinancials(exemptFromPayingClass4Nics = true, Some(TrusteeExecutorAdmin.exemptionCode)))

      // Should clear existing answers
      val expectedResultId4: CreateAmendSEAnnualSubmissionRequestBody =
        buildExpectedRequestResult(AnnualNonFinancials(exemptFromPayingClass4Nics = false, None))

      MockBusinessService.getUserBusinessIds(mtditid, nino)(List(businessId1, businessId2, businessId3, businessId4))

      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino(businessId1))(Right(api_1803.SuccessResponseSchema(None, None, Some(existingDataId1))))
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino(businessId2))(Right(api_1803.SuccessResponseSchema(None, None, Some(existingDataId2))))
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino(businessId3))(Right(api_1803.SuccessResponseSchema(None, None, Some(existingDataId3))))
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino(businessId4))(Right(api_1803.SuccessResponseSchema(None, None, Some(existingDataId4))))

      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino(businessId1), Some(expectedResultId1))
      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino(businessId2), Some(expectedResultId2))
      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino(businessId3), Some(expectedResultId3))
      MockIFSConnector.createUpdateOrDeleteApiAnnualSummaries(journeyCtxWithNino(businessId4), Some(expectedResultId4))

      MockJourneyAnswersRepository.upsertAnswers(
        journeyCtxWithNino(businessId1).toJourneyContext(NationalInsuranceContributions),
        Json.toJson(NICsStorageAnswers(None, None))
      )

      val result: Either[ServiceError, Unit] = await(testService.saveClass4MultipleBusinessOrNoExemptionJourneys(
        ctx = journeyCtxWithNino(businessId1),
        answers = class4DiverAndTrusteeMultipleBusinessesAnswers).value
      )

      result shouldBe Right(())
    }
  }

  "get answers" should {
    "return None if there are no answers" in new StubbedService {
      MockBusinessService.getUserBusinessIds(mtditid, nino)(Nil)
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(None)
      MockJourneyAnswersRepository.getAnswers[NICsStorageAnswers](nicsCtx)(None)

      val result: Either[ServiceError, Option[NICsAnswers]] = await(testService.getAnswers(journeyCtxWithNino).value)

      result shouldBe Right(None)
    }

    "return Class 2 Answers" when {
      "API answers are returned even if database exist" in new StubbedService {
        MockBusinessService.getUserBusinessIds(mtditid, nino)(List(journeyCtxWithNino.businessId))
        MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
        MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(Some(SuccessResponseAPI1639(None, Some(SuccessResponseAPI1639Class2Nics(Some(true))))))
        MockJourneyAnswersRepository.getAnswers(nicsCtx)(Some(NICsStorageAnswers(Some(false), None)))

        val result: Either[ServiceError, Option[NICsAnswers]] = await(testService.getAnswers(journeyCtxWithNino).value)

        result shouldBe Right(Some(NICsAnswers(Some(NICsClass2Answers(true)), None)))
      }

      "valid database answer exist and there is no API data" in new StubbedService {
        MockBusinessService.getUserBusinessIds(mtditid, nino)(List(journeyCtxWithNino.businessId))
        MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(api_1803.SuccessResponseSchema(None, None, None)))
        MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(None)
        MockJourneyAnswersRepository.getAnswers(nicsCtx)(Some(NICsStorageAnswers(Some(false), None)))

        val result: Either[ServiceError, Option[NICsAnswers]] = await(testService.getAnswers(journeyCtxWithNino).value)

        result shouldBe Right(Some(NICsAnswers(Some(NICsClass2Answers(false)), None)))
      }
    }

    "return Class 4 Answers from API data" in new StubbedService {
      val annualNonFinancialsData: SuccessResponseSchema = SuccessResponseSchema(
        annualAdjustments = None,
        annualAllowances = None,
        annualNonFinancials = Some(AnnualNonFinancialsType(Some(true), Some(Class4NicsExemptionReason._002), None))
      )

      MockBusinessService.getUserBusinessIds(mtditid, nino)(List(journeyCtxWithNino.businessId))
      MockIFSConnector.getAnnualSummaries(journeyCtxWithNino)(Right(annualNonFinancialsData))
      MockIFSConnector.getDisclosuresSubmission(journeyCtxWithNino)(None)
      MockJourneyAnswersRepository.getAnswers(nicsCtx)(Some(NICsStorageAnswers(Some(false), None)))

      val result: Either[ServiceError, Option[NICsAnswers]] = await(testService.getAnswers(journeyCtxWithNino).value)

      result shouldBe Right(Some(NICsAnswers(None, Some(NICsClass4Answers(class4NICs = true, Some(ExemptionReason.TrusteeExecutorAdmin), None, None)))))
    }
  }

}
