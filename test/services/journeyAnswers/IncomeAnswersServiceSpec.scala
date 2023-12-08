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
import gens.IncomeJourneyAnswersGen.incomeJourneyAnswersGen
import models.common.JourneyAnswersContext.JourneyContextWithNino
import models.common.JourneyName.Income
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.connectors.StubSelfEmploymentBusinessConnector
import stubs.repositories.StubJourneyAnswersRepository
import utils.BaseSpec
import utils.BaseSpec._

class IncomeAnswersServiceSpec extends BaseSpec {

  private val connector  = StubSelfEmploymentBusinessConnector()
  private val repository = StubJourneyAnswersRepository()

  private val service = new IncomeAnswersServiceImpl(repository, connector)

  "saving income answers" must {
    "store data successfully" in {
      val answers = incomeJourneyAnswersGen.sample.get
      val ctx     = JourneyContextWithNino(currTaxYear, businessId, mtditid, nino, Income)

      await(service.saveAnswers(ctx, answers).value) shouldBe ().asRight
    }
  }

}
