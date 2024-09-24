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

import cats.implicits.catsSyntaxEitherId
import models.database.adjustments.ProfitOrLossDb
import models.error.DownstreamError.SingleDownstreamError
import models.error.DownstreamErrorBody.SingleDownstreamErrorBody
import models.frontend.adjustments.{ProfitOrLossJourneyAnswers, WhichYearIsLossReported}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import stubs.connectors.{StubIFSBusinessDetailsConnector, StubIFSConnector}
import stubs.connectors.StubIFSConnector._
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec.{hc, journeyCtxWithNino}

import scala.concurrent.ExecutionContext.Implicits.global

class ProfitOrLossAnswersServiceImplSpec extends AnyWordSpecLike {

  "Saving ProfitOrLoss answers" must {
    "successfully save data when answers are true" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )
      val answers                        = ProfitOrLossJourneyAnswers(true, Some(200), true, Some(400), Some(WhichYearIsLossReported.Year2018to2019))
      val expectedAnnualSummariesAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "successfully save data when answers are false" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )
      val answers                        = ProfitOrLossJourneyAnswers(false, None, false, None, None)
      val expectedAnnualSummariesAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, previousUnusedLosses = false))
      }
    }
    "successfully create and save answers if no existing answers" in new StubbedService {
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803EmptyResponse.asRight
        )
      val answers                        = ProfitOrLossJourneyAnswers(true, Some(200), false, None, None)
      val expectedAnnualSummariesAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsConnector.upsertAnnualSummariesSubmissionData === Some(expectedAnnualSummariesAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = false))
      }
    }
    "return left when getAnnualSummaries returns left" in new StubbedService {
      val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = downstreamError.asLeft
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), false, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == downstreamError.asLeft)
      }
    }
    "return left when createAmendSEAnnualSubmission returns left" in new StubbedService {
      val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
      override val ifsConnector: StubIFSConnector =
        StubIFSConnector(
          createAmendSEAnnualSubmissionResult = downstreamError.asLeft
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), false, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == downstreamError.asLeft)
      }
    }
    "return left when upsertAnswers returns left" in new StubbedService {
      val downstreamError                                   = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
      override val repository: StubJourneyAnswersRepository = StubJourneyAnswersRepository(upsertDataField = downstreamError.asLeft)
      val answers                                           = ProfitOrLossJourneyAnswers(true, Some(200), false, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == downstreamError.asLeft)
      }
    }
    "successfully update brought forward loss answers when provided by user and API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = api1502SuccessResponse.asRight,
          updateBroughtForwardLossResult = api1501SuccessResponse.asRight
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), true, Some(400), Some(WhichYearIsLossReported.Year2018to2019))
      val expectedBroughtForwardLossAnswers = ProfitOrLossJourneyAnswers.toUpdateBroughtForwardLossData(journeyCtxWithNino, 400)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.updateBroughtForwardLossResult === Some(expectedBroughtForwardLossAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "successfully create brought forward loss answers when provided by user and get returns NOT_FOUND from API" in new StubbedService {
      val downstreamError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = downstreamError.asLeft,
          createBroughtForwardLossResult = api1500SuccessResponse.asRight
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), true, Some(400), Some(WhichYearIsLossReported.Year2018to2019))
      val expectedBroughtForwardLossAnswers =
        ProfitOrLossJourneyAnswers.toCreateBroughtForwardLossData(journeyCtxWithNino, 200, WhichYearIsLossReported.Year2018to2019)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.createBroughtForwardLossResult === Some(expectedBroughtForwardLossAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "successfully delete brought forward loss answers when none provided by user and some from API" in new StubbedService {
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = api1502SuccessResponse.asRight,
          deleteBroughtForwardLossResult = Right(())
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), true, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(ifsBusinessDetailsConnector.deleteBroughtForwardLossResult === Right(()))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "do nothing when none provided by user and get returns NOT_FOUND from API" in new StubbedService {
      val downstreamError = SingleDownstreamError(NOT_FOUND, SingleDownstreamErrorBody.notFound)
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = downstreamError.asLeft
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), true, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "return error when none provided by user and get returns SERVICE_ERROR from API" in new StubbedService {
      val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
      override val ifsBusinessDetailsConnector: StubIFSBusinessDetailsConnector =
        StubIFSBusinessDetailsConnector(
          getBroughtForwardLossResult = downstreamError.asLeft
        )
      val answers = ProfitOrLossJourneyAnswers(true, Some(200), true, None, None)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == downstreamError.asLeft)
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
  }
}

trait StubbedService {
  val ifsConnector                = StubIFSConnector()
  val ifsBusinessDetailsConnector = StubIFSBusinessDetailsConnector()
  val repository                  = StubJourneyAnswersRepository()

  def service: ProfitOrLossAnswersServiceImpl =
    new ProfitOrLossAnswersServiceImpl(ifsConnector, ifsBusinessDetailsConnector, StubJourneyAnswersRepository())
}
