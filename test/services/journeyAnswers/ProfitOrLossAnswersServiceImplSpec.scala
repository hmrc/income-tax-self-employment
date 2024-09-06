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
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.connectors.StubIFSConnector
import stubs.connectors.StubIFSConnector.{api1803EmptyResponse, api1803SuccessResponse}
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec.{hc, journeyCtxWithNino}

import scala.concurrent.ExecutionContext.Implicits.global

class ProfitOrLossAnswersServiceImplSpec extends AnyWordSpecLike with TableDrivenPropertyChecks {

  "Saving ProfitOrLoss answers" must {
    "successfully save data when answers are true" in new StubbedService {
      override val connector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )
      val answers            = ProfitOrLossJourneyAnswers(true, Some(200), true, Some(400), Some(WhichYearIsLossReported.Year2018to2019))
      val expectedAPIAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(connector.upsertAnnualSummariesSubmissionData === Some(expectedAPIAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = true))
      }
    }
    "successfully save data when answers are false" in new StubbedService {
      override val connector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803SuccessResponse.asRight
        )
      val answers            = ProfitOrLossJourneyAnswers(false, None, false, None, None)
      val expectedAPIAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(connector.upsertAnnualSummariesSubmissionData === Some(expectedAPIAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = false, previousUnusedLosses = false))
      }
    }
    "successfully create and save answers if no existing answers" in new StubbedService {
      override val connector: StubIFSConnector =
        StubIFSConnector(
          getAnnualSummariesResult = api1803EmptyResponse.asRight
        )
      val answers            = ProfitOrLossJourneyAnswers(true, Some(200), false, None, None)
      val expectedAPIAnswers = answers.toAnnualSummariesData(journeyCtxWithNino, api1803SuccessResponse)
      service.saveProfitOrLoss(journeyCtxWithNino, answers).value.map { result =>
        assert(result == ().asRight)
        assert(connector.upsertAnnualSummariesSubmissionData === Some(expectedAPIAnswers))
        assert(repository.lastUpsertedAnswer === ProfitOrLossDb(goodsAndServicesForYourOwnUse = true, previousUnusedLosses = false))
      }
    }
    "return left when getAnnualSummaries returns left" in new StubbedService {
      val downstreamError = SingleDownstreamError(INTERNAL_SERVER_ERROR, SingleDownstreamErrorBody.parsingError)
      override val connector: StubIFSConnector =
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
      override val connector: StubIFSConnector =
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
  }
}

trait StubbedService {
  val connector  = StubIFSConnector()
  val repository = StubJourneyAnswersRepository()

  def service: ProfitOrLossAnswersServiceImpl = new ProfitOrLossAnswersServiceImpl(connector, StubJourneyAnswersRepository())
}
